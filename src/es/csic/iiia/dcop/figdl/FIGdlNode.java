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

import es.csic.iiia.dcop.igdl.strategy.IGdlPartitionStrategy;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.igdl.IGdlMessage;
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
public class FIGdlNode extends IUPNode<UPEdge<FIGdlNode, IGdlMessage>, UPResult> {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    /**
     * Cost Functions known by this node.
     */
    private ArrayList<CostFunction> costFunctions;

    /**
     * Partitioning strategy to use
     */
    private IGdlPartitionStrategy strategy;

    /**
     * FIGdlNode from previous iteration
     */
    private ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges;

    /**
     * Bounds from previous iteration
     */
    private double bound = Double.NaN;

    /**
     * (Experimental) Number of broken links in received functions as an
     * heuristic of information accuracy.
     */
    private int nBrokenLinks = 0;
    private int localBrokenLinks = 0;
    private int maxBrokenLinks = 0;

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
        previousEdges = new ArrayList<UPEdge<FIGdlNode, IGdlMessage>>();
        for(UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
            if (e.getMessage(this) == null) {
                e.tick();
            }

            UPEdge<FIGdlNode, IGdlMessage> newe = new UPEdge<FIGdlNode, IGdlMessage>(e);
            previousEdges.add(newe);
        }

        if (Double.isNaN(this.getBound()) || factory.getSummarizeOperation().isBetter(bound, this.getBound())) {
            this.bound = bound;
        }

        strategy.setFilteringOptions(bound, previousEdges);
        localBrokenLinks = 0;
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    @Override
    public void initialize() {
        super.initialize();
        localBrokenLinks = 0;

        // Tree-based operation
        setMode(Modes.TREE_UP);
        costFunctions = new ArrayList<CostFunction>(relations);
        getPartitionStrategy().initialize(this);

        // Send initial messages
        sendMessages();
    }

    /**
     * Performs one "step" of the GDL algorithm, updating the clique's belief
     * and sending new messages to it's neighboors.
     *
     * @return number of constraint checks consumed.
     */
    public long run() {

        // Rebuild cost function list
        costFunctions = new ArrayList<CostFunction>();
        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }

        // And the received messages
        nBrokenLinks = 0; maxBrokenLinks = 0;
        Collection<UPEdge<FIGdlNode, IGdlMessage>> edges = getEdges();
        for (UPEdge<FIGdlNode, IGdlMessage> e : edges) {
            IGdlMessage msg = e.getMessage(this);
            if (msg != null) {
                costFunctions.addAll(msg.getFactors());
                if (isParent(e)) {
                    nBrokenLinks += msg.getnBrokenLinks();
                    maxBrokenLinks = Math.max(maxBrokenLinks, msg.getMaxBrokenLinks());
                }
            }
        }

        // Send updated messages
        long cc = sendMessages();
        setUpdated(false);
        return cc;
    }

    public UPResult end() {
        return new UPResult(this);
    }

    @Override
    public ArrayList<CostFunction> getBelief() {
        return new ArrayList<CostFunction>(costFunctions);
    }

    private long sendMessages() {
        long cc = 0;

        CostFunction belief = null;
        if (log.isTraceEnabled()) {
            belief = factory.buildNeutralCostFunction(new Variable[0]);
            belief = belief.combine(costFunctions);
        }

        for (UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
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
                for (UPEdge<FIGdlNode, IGdlMessage> e2 : getEdges()) {
                    if (e == e2) continue;
                    fs.addAll(e2.getMessage(this).getFactors());
                }
            }

            // Obtain the partition
            IGdlMessage msg = getPartitionStrategy().getPartition(fs, e);
            if (log.isTraceEnabled()) {
                CostFunction lb = belief.summarize(e.getVariables());
                if (e.getMessage(this) != null) {
                    for (CostFunction f : e.getMessage(this).getFactors()) {
                        lb.combine(f.negate());
                    }
                }
                msg.setBelief(lb);
            }
            cc += msg.cc;

            if (isParent(e)) {
                localBrokenLinks = msg.getnBrokenLinks();
                msg.setMaxBrokenLinks(maxBrokenLinks + localBrokenLinks);
            }

            e.sendMessage(this, msg);
        }

        return cc;
    }

    /* Never called because we never operate in graph mode */
    @Override
    public boolean isConverged() {
        return false;
    }

    /**
     * @return the strategy
     */
    public IGdlPartitionStrategy getPartitionStrategy() {
        return strategy;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setPartitionStrategy(IGdlPartitionStrategy strategy) {
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

    public int getnBrokenLinks() {
        return nBrokenLinks;
    }

    public int getMaxBrokenLinks() {
        return maxBrokenLinks + localBrokenLinks;
    }

}
