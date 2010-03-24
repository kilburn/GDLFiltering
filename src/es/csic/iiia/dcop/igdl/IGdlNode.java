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

package es.csic.iiia.dcop.igdl;

import es.csic.iiia.dcop.igdl.strategy.AllCombStrategy;
import es.csic.iiia.dcop.igdl.strategy.IGdlPartitionStrategy;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPNode;
import java.util.ArrayList;
import java.util.Collection;

/**
 * GDL algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class IGdlNode extends UPNode<UPEdge<IGdlNode, IGdlMessage>, UPResult> {

    /**
     * Maximum cost function arity
     */
    private int r;

    /**
     * Cost Functions known by this node.
     */
    private ArrayList<CostFunction> costFunctions;

    /**
     * Partitioning strategy to use
     */
    private IGdlPartitionStrategy strategy = new AllCombStrategy();

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public IGdlNode(Variable variable) {
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
    public IGdlNode(CostFunction potential) {
        super(potential);
    }

    /**
     * Constructs a new empty clique.
     */
    public IGdlNode() {
        super();
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    public void initialize() {
        // Tree-based operation
        setMode(Modes.TREE_UP);
        costFunctions = new ArrayList<CostFunction>();

        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }       
        strategy.initialize(this);

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

        // CC count
        long cc = 0;

        // Rebuild cost function list
        ArrayList<CostFunction> previousCostFunctions = costFunctions;
        costFunctions = new ArrayList<CostFunction>();
        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }
        // And the received messages
        Collection<UPEdge<IGdlNode, IGdlMessage>> edges = getEdges();
        for (UPEdge<IGdlNode, IGdlMessage> e : edges) {
            IGdlMessage msg = e.getMessage(this);
            if (msg != null)
                costFunctions.addAll(msg.getFactors());
        }

        // Compute our belief
        // ?

        // Send updated messages
        sendMessages();
        setUpdated(false);
        return cc;
    }

    public UPResult end() {
        return new UPResult(this);
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getR() {
        return r;
    }

    @Override
    public CostFunction getBelief() {
        CostFunction belief = null;
        for (CostFunction c : costFunctions) {
            belief = c.combine(belief);
        }
        return belief;
    }

    public ArrayList<CostFunction> getCostFunctions() {
        return costFunctions;
    }

    private void sendMessages() {
        for (UPEdge<IGdlNode, IGdlMessage> e : getEdges()) {
            if (!readyToSend(e)) {
                continue;
            }

            // List of all functions that would be sent (combined)
            ArrayList<CostFunction> fs = new ArrayList<CostFunction>(costFunctions);

            // Remove the factors received through this edge
            if (e.getMessage(this) != null) {
                fs.removeAll(e.getMessage(this).getFactors());
            }

            // For research purposes, calculate the optimal belief
            CostFunction belief = null;
            for (CostFunction f : fs) {
                belief = f.combine(belief);
            }

            // Obtain the partition and send it
            IGdlMessage msg = strategy.getPartition(fs, e);
            msg.setBelief(belief.summarize(e.getVariables()));
            e.sendMessage(this, msg);
        }
    }

    /* Never called because we never operate in graph mode */
    @Override
    public boolean isConverged() {
        return false;
    }

}
