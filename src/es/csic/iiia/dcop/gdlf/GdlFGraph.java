/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2011, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * 
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 * 
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * 
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package es.csic.iiia.dcop.gdlf;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunction.Summarize;
import es.csic.iiia.dcop.bb.UBGraph;
import es.csic.iiia.dcop.bb.UBResults;
import es.csic.iiia.dcop.gdlf.strategies.control.ControlStrategy;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.util.BytesSent;
import es.csic.iiia.dcop.util.ConstraintChecks;
import es.csic.iiia.dcop.util.FunctionCounter;
import es.csic.iiia.dcop.util.MemoryTracker;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.VPResults;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class GdlFGraph extends UPGraph<GdlFNode,UPEdge<GdlFNode, GdlFMessage>,UPResults> {

    private static final Logger log = LoggerFactory.getLogger(UPGraph.class);
    
    /**
     * Control parameters
     */
    private final ControlStrategy strategy;
    
    private final double constant;
    private final boolean inverted;
    
    private static VPStrategy solutionStrategy;
    
    private UBResults ubResults;

    private final GdlFIteration iteration = new GdlFIteration();
    
    public GdlFGraph(CostFunction constant, boolean inverted, ControlStrategy strategy) {
        super();
        this.constant = constant.getValue(0);
        this.inverted = inverted;
        this.strategy = strategy;
    }

    @Override
    public void addNode(GdlFNode clique) {
        super.addNode(clique);
        iteration.addNode(clique);
    }

    @Override
    public void addEdge(UPEdge<GdlFNode, GdlFMessage> edge) {
        super.addEdge(edge);
        iteration.addEdge(edge);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }


    @Override
    public UPResults run(int maxIterations) {
        reportStart();

        UPResults globalResults = (UPResults)getResults();
        double bestCost = Double.NaN, bestBound = Double.NaN, realBestCost = Double.NaN;

        while (strategy.hasMoreElements()) {
            Limits limits = strategy.nextElement();
            System.out.println("Now limits = " + limits);

            // Value propagation
            iteration.setLimits(limits);
            
            boolean exit = false;
            for (int j=0; j<1; j++) {

                ConstraintChecks.addTracker(this);
                BytesSent.addTracker(this);

                UPResults iterResults = iteration.run(maxIterations);
                if (iterResults == null) {
                    // Early termination, use results from the previous iteration
                    exit = true;
                    break;
                }
                globalResults.mergeResults(iterResults);

                Summarize summarize = null;
                for(GdlFNode n : getNodes()) {
                    if (n.getRelations().size() > 0) {
                        summarize = n.getRelations().get(0).getFactory().getSummarizeOperation();
                        break;
                    }
                }
                if (summarize == null) {
                    throw new RuntimeException("Unable to fetch summarization operation.");
                }


                // Solution extraction
                VPGraph vp = new VPGraph(this, solutionStrategy);
                VPResults res = vp.run(1000);
                globalResults.mergeResults(res);

                // Bound calculation
                UBGraph ub = new UBGraph(vp);
                ubResults = ub.run(1000);
                globalResults.mergeResults(ubResults);
                for (Object result : iterResults.getResults()) {
                    globalResults.add((UPResult)result);
                }

                long iterCCs   = ConstraintChecks.removeTracker(this);
                long iterBytes = BytesSent.removeTracker(this);
                System.out.println("ITERBYTES " + iterBytes);
                System.out.println("ITERCCS " + iterCCs);
                System.out.println("ITERSPARSITY " + FunctionCounter.getRatio());
                System.out.println("ITERMAX_NODE_MEMORY " + 
                        MemoryTracker.toString(iterResults.getMaximalMemoryc()) + " Mb");

                double newCost = ubResults.getCost()+constant;
                double newBound = ubResults.getBound()+constant;
                
                if (inverted) {
                    double tmp = newCost;
                    newCost = -newBound;
                    newBound = -tmp;
                }
                    
                if (Double.isNaN(bestCost) || summarize.isBetter(newCost, bestCost)) {
                    bestCost = newCost;
                    realBestCost = ubResults.getCost();
                }
                if (Double.isNaN(bestBound) || !summarize.isBetter(newBound, bestBound)) {
                    bestBound = newBound;
                }
                
                System.out.println("THIS_ITER_LB " + newBound);
                System.out.println("THIS_ITER_UB " + newCost);
                System.out.println("ITER_LB " + bestBound);
                System.out.println("ITER_UB " + bestCost);

                if (Math.abs(newCost - bestBound) < 0.0005) {
                    exit = true;
                    break;
                }

                if (summarize.isBetter(bestCost, bestBound)) {
                    exit = true;
                    break;
                }

                // Build the new iteration
                iteration.prepareNextIteration(realBestCost);
            }

            if (exit) break;
        }

        return globalResults;
    }

    @Override
    protected UPResults buildResults() {
        return new UPResults();
    }

    public UBResults getUBResults() {
        return ubResults;
    }
    
    public static void setSolutionStrategy(VPStrategy st) {
        solutionStrategy = st;
    }

}
