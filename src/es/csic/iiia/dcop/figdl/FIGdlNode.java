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

import es.csic.iiia.dcop.igdl.strategy.AllCombStrategy;
import es.csic.iiia.dcop.igdl.strategy.IGdlPartitionStrategy;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.MapCostFunctionFactory;
import es.csic.iiia.dcop.SparseCostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.algo.JunctionTreeAlgo;
import es.csic.iiia.dcop.dfs.DFS;
import es.csic.iiia.dcop.dfs.MCN;
import es.csic.iiia.dcop.gdl.GdlFactory;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.jt.JunctionTree;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.VPResults;
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
    private IGdlPartitionStrategy strategy = new AllCombStrategy();

    /**
     * FIGdlNode from previous iteration
     */
    private ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges;
    private ArrayList<UPEdge<FIGdlNode, IGdlMessage>> bestEdges;

    /**
     * Bounds from previous iteration
     */
    private double bound = Double.NaN;

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

        previousEdges = new ArrayList<UPEdge<FIGdlNode, IGdlMessage>>();
        for(UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
            if (e.getMessage(this) == null) {
                e.tick();
            }

            UPEdge<FIGdlNode, IGdlMessage> newe = new UPEdge<FIGdlNode, IGdlMessage>(e);
            previousEdges.add(newe);
        }

        if (Double.isNaN(this.bound) || factory.getSummarizeOperation().isBetter(bound, this.bound)) {
            this.bound = bound;
            bestEdges = previousEdges;
        }

        strategy.setFilteringOptions(bound, previousEdges);
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    @Override
    public void initialize() {
        super.initialize();

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
        Collection<UPEdge<FIGdlNode, IGdlMessage>> edges = getEdges();
        for (UPEdge<FIGdlNode, IGdlMessage> e : edges) {
            IGdlMessage msg = e.getMessage(this);
            if (msg != null)
                costFunctions.addAll(msg.getFactors());
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
    public CostFunction getBelief() {

        /* Re-compute cost functions */
        costFunctions = new ArrayList<CostFunction>(relations);
        for (UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
            IGdlMessage msg = e.getMessage(this);
            if (msg != null)
                costFunctions.addAll(msg.getFactors());
        }

        CostFunction belief = null;
        for (CostFunction c : costFunctions) {
            belief = c.combine(belief);
        }

        return belief;
    }

    public ArrayList<CostFunction> getCostFunctions() {
        return costFunctions;
    }

    private long sendMessages() {
        long cc = 0;

        CostFunction belief = null;
        if (log.isTraceEnabled()) {
            for (CostFunction f : costFunctions) {
                belief = f.combine(belief);
            }
        }

        for (UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
            if (!readyToSend(e)) {
                continue;
            }

            // List of all functions that would be sent (combined)
            ArrayList<CostFunction> fs = new ArrayList<CostFunction>(costFunctions);

            // Remove the factors received through this edge
            if (e.getMessage(this) != null) {
                fs.removeAll(e.getMessage(this).getFactors());
            }

            // Obtain the partition
            IGdlMessage msg = getPartitionStrategy().getPartition(fs, e);
            if (log.isTraceEnabled()) {
                msg.setBelief(belief.summarize(e.getVariables()));
            }
            cc += msg.cc;

            e.sendMessage(this, msg);
        }

        return cc;
    }

    /* Never called because we never operate in graph mode */
    @Override
    public boolean isConverged() {
        return false;
    }

    @Override
    public double getOptimalValue() {
        /*VariableAssignment map = getOptimalConfigurationByGDL(costFunctions);
        // Evaluate solution
        double cost = 0;
        for (CostFunction f : costFunctions) {
            cost += f.getValue(map);
        }
        return cost;*/

        final CostFunction belief = getBelief();
        return belief.getValue(belief.getOptimalConfiguration(null));
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

    

    void fallbackLastIteration() {
        // Recover messages from best iteration
        int i = 0;
        costFunctions.clear();
        costFunctions.addAll(getRelations());
        for (UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
            UPEdge<FIGdlNode, IGdlMessage> olde = bestEdges.get(i++);
            costFunctions.addAll(olde.getMessage(this).getFactors());
        }
    }

    @Override
    public VariableAssignment getOptimalConfiguration(VariableAssignment map) {

        CostFunction reducedBelief = null;
        for (CostFunction f : relations) {
            reducedBelief = f.reduce(map).combine(reducedBelief);
        }

        for (UPEdge<FIGdlNode, IGdlMessage> e : getEdges()) {
            IGdlMessage msg = e.getMessage(this);
            if (msg != null) {
                for (CostFunction f : msg.getFactors()) {
                    // Constants do not change the optimal configuration
                    if (f.getSize() == 1) {
                        continue;
                    }
                    reducedBelief = f.reduce(map).combine(reducedBelief);
                }
            }
        }

        if (reducedBelief == null) {
            System.err.println("Warning: found empty belief");
            return map;
        }

        return reducedBelief.getOptimalConfiguration(map);

        /*
        // This shoudn't happen...
        if (costFunctions.isEmpty()) {
            System.err.println("Warning: empty cost function list.");
            return map;
        }

        ArrayList<CostFunction> cfs = new ArrayList<CostFunction>(costFunctions.size());
        //log.debug("MAP: " + map);
        for (CostFunction f : costFunctions) {
            //log.debug("F: " + f);
            final CostFunction fr = f.reduce(map);
            //log.debug("R: " + fr);
            // Constants do not alter the optimal configuration
            if (fr.getSize() > 1) {
                cfs.add(fr);
            }
        }

        // Single function -> just fill the map and forget about it
        if (cfs.size() == 1) {
            return cfs.get(0).getOptimalConfiguration(map);
        }

        map.putAll(getOptimalConfigurationByGDL(cfs));
        return map;*/
    }

    private VariableAssignment getOptimalConfigurationByGDL(ArrayList<CostFunction> cfs) {
        /*log.debug("Solving by GDL:");
        for(CostFunction f : cfs) {
            log.debug(f.toString());
        }*/

        GdlFactory gfactory = new GdlFactory();
        gfactory.setMode(Modes.TREE_UP);
        DFS dfs = dfs = new MCN(cfs.toArray(new CostFunction[0]));
        UPGraph cg = JunctionTreeAlgo.buildGraph(gfactory, dfs.getFactorDistribution(), dfs.getAdjacency());

        // Our GDL doesn't work with a single node, so we tackle this shortcomming
        if (cg.getNodes().size() < 2) {
            CostFunction belief = null;
            for(CostFunction f : cfs) {
                belief = f.combine(belief);
            }
            return belief.getOptimalConfiguration(null);
        }


        cg.setRoot(dfs.getRoot());
        JunctionTree jt = new JunctionTree(cg);
        cg.setFactory(getFactory());
        cg.run(1000);
        VPGraph st = new VPGraph(cg);
        VPResults res = st.run(10000);
        return res.getMapping();
    }

}
