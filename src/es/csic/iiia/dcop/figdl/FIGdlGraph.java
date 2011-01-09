/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
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

package es.csic.iiia.dcop.figdl;

import es.csic.iiia.dcop.CostFunction.Summarize;
import es.csic.iiia.dcop.bb.UBGraph;
import es.csic.iiia.dcop.bb.UBResults;
import es.csic.iiia.dcop.mp.Result;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.util.BytesSent;
import es.csic.iiia.dcop.util.ConstraintChecks;
import es.csic.iiia.dcop.util.FunctionCounter;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.VPResults;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class FIGdlGraph extends UPGraph<FIGdlNode,UPEdge<FIGdlNode, FIGdlMessage>,UPResults> {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    private static int minR = 2;
    private static int maxS = Integer.MAX_VALUE;
    private static double optimumValue = Double.NaN;
    private static VPStrategy solutionStrategy;

    public static void setMaxS(int max_s) {
        maxS = max_s;
    }

    public static void setOptimalValue(double optimum) {
        optimumValue = optimum;
    }

    private UBResults ubResults;

    public static void setMinR(int min_r) {
        minR = min_r;
    }

    public static void setSolutionStrategy(VPStrategy strategy) {
        solutionStrategy = strategy;
    }

    private FIGdlIteration iteration = new FIGdlIteration();
    private int maxR;

    @Override
    public void addNode(FIGdlNode clique) {
        super.addNode(clique);
        iteration.addNode(clique);
    }

    @Override
    public void addEdge(UPEdge<FIGdlNode, FIGdlMessage> edge) {
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

        if (!Double.isNaN(optimumValue)) {
            iteration.setOptimumValue(optimumValue);
        }

        UPResults globalResults = (UPResults)getResults();
        double bestCost = Double.NaN, bestBound = Double.NaN;
        iteration.setS(maxS);

        for (int i=minR; i<=maxR; i++) {
            System.out.println("Now r=" + i);

            // Value propagation
            if (i > maxS) {
                iteration.setS(i);
            }
            iteration.setR(i);
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
                for(FIGdlNode n : getNodes()) {
                    if (n.getRelations().size() > 0) {
                        summarize = n.getRelations().get(0).getFactory().getSummarizeOperation();
                        break;
                    }
                }


                // Solution extraction
                VPGraph vp = new VPGraph(this, solutionStrategy);
                VPResults res = vp.run(1000);
                ArrayList<Result> rs = iterResults.getResults();
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

                System.out.println("THIS_ITER_LB " + ubResults.getBound());
                System.out.println("THIS_ITER_UB " + ubResults.getCost());

                final double newCost = ubResults.getCost();
                if (Double.isNaN(bestCost) || summarize.isBetter(newCost, bestCost)) {
                    bestCost = newCost;
                }
                final double newBound = ubResults.getBound();
                if (Double.isNaN(bestBound) || !summarize.isBetter(newBound, bestBound)) {
                    bestBound = newBound;
                }
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
                iteration.prepareNextIteration(bestCost);
            }

            if (exit) break;
        }

        return globalResults;
    }

    public void setMaxR(int maxR) {
        this.maxR = maxR;
    }

    @Override
    protected UPResults buildResults() {
        return new UPResults();
    }

    public UBResults getUBResults() {
        return ubResults;
    }

}
