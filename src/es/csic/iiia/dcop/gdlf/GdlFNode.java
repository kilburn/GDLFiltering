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

import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.gdlf.strategies.filter.FilterStrategy;
import es.csic.iiia.dcop.gdlf.strategies.merge.MergeStrategy;
import es.csic.iiia.dcop.gdlf.strategies.slice.SliceStrategy;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.util.MemoryTracker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GDL algorithm node.
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class GdlFNode extends UPNode<UPEdge<GdlFNode, GdlFMessage>, UPResult> {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    
    /**
     * Computation and Communication limits
     */
    private Limits limits;

    /**
     * Cost Functions known by this node.
     */
    private ArrayList<CostFunction> costFunctions;

    /**
     * Bounds from previous iteration
     */
    private double bound = Double.NaN;
    
    /**
     * Previously sent messages
     */
    private HashMap<UPEdge, List<CostFunction>> receivedFunctions;
    
    private MergeStrategy mergeStrategy;
    private FilterStrategy filterStrategy;
    private SliceStrategy sliceStrategy;

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public GdlFNode(Variable variable) {
        super(variable);
        receivedFunctions = new HashMap<UPEdge, List<CostFunction>>();
    }

    /**
     * Constructs a new clique with the specified potential.
     *
     * The potential variables are automatically extracted and added as
     * member variables of this clique.
     *
     * @param potential potential of the clique.
     */
    public GdlFNode(CostFunction potential) {
        super(potential);
        receivedFunctions = new HashMap<UPEdge, List<CostFunction>>();
    }

    /**
     * Constructs a new empty clique.
     */
    public GdlFNode() {
        super();
        receivedFunctions = new HashMap<UPEdge, List<CostFunction>>();
    }

    /**
     * Prepares this GdlFNode for a new (different "r") iteration.
     * @param bound 
     */
    public void prepareNextIteration(double bound) {

        if (Double.isNaN(this.bound) || factory.getSummarizeOperation().isBetter(bound, this.bound)) {
            this.bound = bound;
        }

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
        MemoryTracker.add(MemoryTracker.getRequiredMemory(costFunctions));

        // Send initial messages
        sendMessages();
    }

    /**
     * Performs one "step" of the GDL algorithm, updating the clique's belief
     * and sending new messages to it's neighboors.
     */
    public void run() {
        // Rebuild cost function list
        costFunctions = new ArrayList<CostFunction>();
        
        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }

        // And the received messages
        Collection<UPEdge<GdlFNode, GdlFMessage>> edges = getEdges();
        for (UPEdge<GdlFNode, GdlFMessage> e : edges) {
            GdlFMessage msg = e.getMessage(this);
            if (msg != null) {
                receivedFunctions.put(e, msg.getFactors());
                costFunctions.addAll(msg.getFactors());
            }
        }
        
        // We need memory to store all the received functions
        for (List<CostFunction> lfs : receivedFunctions.values()) {
            MemoryTracker.add(MemoryTracker.getRequiredMemory(lfs));
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
            final double nv = factory.getCombineOperation().getNeutralValue();
            belief = factory.buildCostFunction(new Variable[0], nv);
            belief = belief.combine(costFunctions);
        }

        for (UPEdge<GdlFNode, GdlFMessage> e : getEdges()) {
            if (!readyToSend(e)) {
                continue;
            }

            // List of all functions that would be sent (combined)
            List<CostFunction> fs = null;
            if (e.getMessage(this) == null) {
                 fs = new ArrayList<CostFunction>(costFunctions);
            } else {
                // we build a new list instead which is faster.
                fs = new ArrayList<CostFunction>(relations);
                for (UPEdge<GdlFNode, GdlFMessage> e2 : getEdges()) {
                    if (e == e2) continue;
                    fs.addAll(e2.getMessage(this).getFactors());
                }
            }

            // Merge
            List<Variable> vs = Arrays.asList(e.getVariables());
            fs = mergeStrategy.merge(fs, vs, limits.getMergeComputation(), limits.getMergeCommunication());
            
            // Summarize
            for (int i=0, len=fs.size(); i<len; i++) {
                final CostFunction f = fs.get(i);
                Variable[] vars = f.getSharedVariables(e.getVariables()).toArray(new Variable[0]);
                final CostFunction summarizedFunction = fs.get(i).summarize(vars);
                fs.set(i, summarizedFunction);
            }
            
            // Filter
            if (!Double.isNaN(bound)) {
                List<CostFunction> pfs = receivedFunctions.get(e);
                fs = filterStrategy.filter(fs, pfs, bound);
            }
            
            // Slice
            fs = sliceStrategy.slice(fs, limits.getSplitCommunication());
            
            GdlFMessage msg = new GdlFMessage(fs);
            
            // Debug stuff
            if (log.isTraceEnabled()) {
                CostFunction lb = belief.summarize(e.getVariables());
                if (e.getMessage(this) != null) {
                    for (CostFunction f : e.getMessage(this).getFactors()) {
                        lb.combine(f.negate());
                    }
                }
                msg.setBelief(lb);
            }

            e.sendMessage(this, msg);
        }
    }

    /* Never called because we never operate in graph mode */
    @Override
    public boolean isConverged() {
        return false;
    }

    @Override
    public ArrayList<CostFunction> getReducedBelief(VariableAssignment map) {
        ArrayList<CostFunction> fs = new ArrayList<CostFunction>();
        for (CostFunction f : costFunctions) {
            final CostFunction f2 = f.reduce(map);
            fs.add(f2);
        }

        return fs;
    }
    
    void setLimits(Limits limits) {
        this.limits = limits;
    }

    public void setMergeStrategy(MergeStrategy mergeStrategy) {
        this.mergeStrategy = mergeStrategy;
    }

    public void setFilterStrategy(FilterStrategy filterStrategy) {
        this.filterStrategy = filterStrategy;
    }

    public void setSliceStrategy(SliceStrategy sliceStrategy) {
        this.sliceStrategy = sliceStrategy;
    }

}
