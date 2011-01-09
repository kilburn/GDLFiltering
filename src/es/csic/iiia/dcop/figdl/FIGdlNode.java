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

import es.csic.iiia.dcop.figdl.strategy.ApproximationStrategy;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GDL algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class FIGdlNode extends IUPNode<UPEdge<FIGdlNode, FIGdlMessage>, UPResult> {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    /**
     * Cost Functions known by this node.
     */
    private ArrayList<CostFunction> costFunctions;

    /**
     * Approximation strategy to use
     */
    private ApproximationStrategy strategy;

    /**
     * FIGdlNode from previous iteration
     */
    private ArrayList<UPEdge<FIGdlNode, FIGdlMessage>> previousEdges;

    /**
     * Bounds from previous iteration
     */
    private double bound = Double.NaN;

    /**
     * (Experimental) Amount of information loss in received functions as an
     * heuristic of received cost functions' accuracy.
     */
    private double informationLoss = 0;
    private double localInformationLoss = 0;
    private double maxInformationLoss = 0;

    /**
     * Boolean flag that indicates if the algorithm has started filtering
     * some tuples.
     */
    private boolean startedFiltering;
    
    /**
     * Boolean flag that indicates if the algorithm is filtering more than
     * 90% of the tuples.
     */
    private boolean endingFiltering;

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public FIGdlNode(Variable variable) {
        super(variable);
    }

    /**
     * Constructs a new clique with the specified potential.
     *
     * The potential variables are automatically extracted and added as
     * member variables of this clique.
     *
     * @param potential potential of the clique.
     */
    public FIGdlNode(CostFunction potential) {
        super(potential);
    }

    /**
     * Constructs a new empty clique.
     */
    public FIGdlNode() {
        super();
    }

    /**
     * Prepares this FIGdlNode for a new (different "r") iteration.
     * @param bound 
     */
    public void prepareNextIteration(double bound) {

        if (previousEdges != null) previousEdges.clear();
        previousEdges = new ArrayList<UPEdge<FIGdlNode, FIGdlMessage>>();
        for(UPEdge<FIGdlNode, FIGdlMessage> e : getEdges()) {
            if (e.getMessage(this) == null) {
                e.tick();
            }

            UPEdge<FIGdlNode, FIGdlMessage> newe = new UPEdge<FIGdlNode, FIGdlMessage>(e);
            previousEdges.add(newe);
        }

        if (Double.isNaN(this.getBound()) || factory.getSummarizeOperation().isBetter(bound, this.getBound())) {
            this.bound = bound;
        }

        strategy.setFilteringOptions(bound, previousEdges);
        localInformationLoss = 0;
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    @Override
    public void initialize() {
        super.initialize();
        startedFiltering = false;
        localInformationLoss = 0;

        // Tree-based operation
        setMode(Modes.TREE_UP);
        costFunctions = new ArrayList<CostFunction>(relations);
        getApproximationStrategy().initialize(this);

        // Send initial messages
        sendMessages();
    }

    /**
     * Performs one "step" of the GDL algorithm, updating the clique's belief
     * and sending new messages to it's neighboors.
     *
     * @return number of constraint checks consumed.
     */
    public void run() {

        // Rebuild cost function list
        costFunctions = new ArrayList<CostFunction>();
        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }

        // And the received messages
        informationLoss = 0; maxInformationLoss = 0;
        Collection<UPEdge<FIGdlNode, FIGdlMessage>> edges = getEdges();
        for (UPEdge<FIGdlNode, FIGdlMessage> e : edges) {
            FIGdlMessage msg = e.getMessage(this);
            if (msg != null) {
                costFunctions.addAll(msg.getFactors());
                if (isParent(e)) {
                    informationLoss += msg.getInformationLoss();
                    maxInformationLoss = Math.max(maxInformationLoss, msg.getMaxInformationLoss());
                }
                startedFiltering = startedFiltering || msg.hasStartedFiltering();
                endingFiltering = endingFiltering || msg.isEndingFiltering();
            }
        }

        // Send updated messages
        sendMessages();
        setUpdated(false);
    }

    public UPResult end() {
        return new UPResult(this);
    }

    @Override
    public ArrayList<CostFunction> getBelief() {
        return new ArrayList<CostFunction>(costFunctions);
    }

    private void sendMessages() {
        CostFunction belief = null;
        if (log.isTraceEnabled()) {
            belief = factory.buildNeutralCostFunction(new Variable[0]);
            belief = belief.combine(costFunctions);
        }

        for (UPEdge<FIGdlNode, FIGdlMessage> e : getEdges()) {
            if (!readyToSend(e)) {
                continue;
            }

            // List of all functions that would be sent (combined)
            ArrayList<CostFunction> fs = new ArrayList<CostFunction>(costFunctions);

            // Remove the factors received through this edge
            if (e.getMessage(this) != null) {
                
                // This is equivalent to doing:
                // fs.removeAll(e.getMessage(this).getFactors());
                // ... but removing is an expensive operation, so
                // we build a new list instead which is faster.
                fs = new ArrayList<CostFunction>(relations);
                for (UPEdge<FIGdlNode, FIGdlMessage> e2 : getEdges()) {
                    if (e == e2) continue;
                    fs.addAll(e2.getMessage(this).getFactors());
                }
            }

            // Obtain the approximate
            FIGdlMessage msg = getApproximationStrategy().getApproximation(fs, e);
            if (log.isTraceEnabled()) {
                CostFunction lb = belief.summarize(e.getVariables());
                if (e.getMessage(this) != null) {
                    for (CostFunction f : e.getMessage(this).getFactors()) {
                        lb.combine(f.negate());
                    }
                }
                msg.setBelief(lb);
            }

            if (isParent(e)) {
                localInformationLoss = msg.getInformationLoss();
                msg.setMaxInformationLoss(maxInformationLoss + localInformationLoss);
                if (!startedFiltering && msg.hasFilteredFunctions()) {
                    msg.setStartedFiltering();
                    startedFiltering = true;
                }
                if (!endingFiltering && msg.isEndingFiltering()) {
                    msg.setEndingFiltering();
                    endingFiltering = true;
                }
            }
            if (startedFiltering) msg.setStartedFiltering();
            if (endingFiltering) msg.setEndingFiltering();

            e.sendMessage(this, msg);
        }
    }

    /* Never called because we never operate in graph mode */
    @Override
    public boolean isConverged() {
        return false;
    }

    /**
     * @return the strategy
     */
    public ApproximationStrategy getApproximationStrategy() {
        return strategy;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setApproximationStrategy(ApproximationStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public ArrayList<CostFunction> getReducedBelief(VariableAssignment map) {
        ArrayList<CostFunction> fs = new ArrayList<CostFunction>();
        for (CostFunction f : costFunctions) {
            long time = System.currentTimeMillis();
            final CostFunction f2 = f.reduce(map);
            fs.add(f2);
            time = System.currentTimeMillis() - time;
            if (time > 10) {
                log.info("Reduce f time: " + time +
                        ", vars: " + f.getVariableSet().size() +
                        " -> " + f2.getVariableSet().size());
            }
        }

        return fs;
    }

    @Override
    public double getBound() {
        return bound;
    }

    public double getInformationLoss() {
        return informationLoss;
    }

    public double getMaxInformationLoss() {
        return maxInformationLoss + localInformationLoss;
    }

    void setOptimumValue(double optimum) {
        strategy.setOptimumValue(optimum);
    }

    public boolean isEndingFiltering() {
        return endingFiltering;
    }

    public boolean hasStartedFiltering() {
        return startedFiltering;
    }

}
