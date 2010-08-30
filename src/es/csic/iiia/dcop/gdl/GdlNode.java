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

package es.csic.iiia.dcop.gdl;

import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPNode;
import java.util.ArrayList;

/**
 * GDL algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class GdlNode extends UPNode<UPEdge<GdlNode, GdlMessage>, UPResult> {

    /**
     * Tolerance to use when comparing the previous and current beliefs.
     */
    private double tolerance = 0.0001;

    /**
     * Potential of this clique.
     */
    private CostFunction potential;

    /**
     * Belief of this clique.
     */
    private CostFunction belief;

    /**
     * Belief of this clique.
     */
    private CostFunction previousBelief;

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public GdlNode(Variable variable) {
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
    public GdlNode(CostFunction potential) {
        super(potential);
    }

    /**
     * Constructs a new empty clique.
     */
    public GdlNode() {
        super();
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    @Override
    public void initialize() {
        super.initialize();

        // Calculate our potential
        previousBelief = null;
        potential = factory.buildNeutralCostFunction(new Variable[0]);
        potential = potential.combine(relations);
        
        // And our belief
        //belief = getFactory().buildCostFunction(variables.toArray(new Variable[0]));
        //belief = potential.combine(belief);
        belief = potential;

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

        // Combine incoming messages
        CostFunction combi = getFactory().buildNeutralCostFunction(new Variable[0]);

        ArrayList<CostFunction> fns = new ArrayList<CostFunction>();
        fns.add(potential);
        for (UPEdge<GdlNode, GdlMessage> e : getEdges()) {
            GdlMessage m = e.getMessage(this);
            if (m != null) {
                CostFunction msg = m.getFactor();
                fns.add(msg);
                cc += combi.getSize();
            }
        }

        // Compute our belief
        previousBelief = belief;
        this.belief = combi.combine(fns);
        cc += belief.getSize();
        this.belief = this.belief.normalize();
        cc += belief.getSize();

        // Send updated messages
        cc += sendMessages();

        setUpdated(false);
        return cc;
    }

    public UPResult end() {
        return new UPResult(this);
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public CostFunction getPotential() {
        return potential;
    }

    /**
     * Retrieves the belief of this clique.
     *
     * @return belief of this clique.
     */
    public CostFunction getBelief() {
        return belief;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(" P:");
        buf.append(potential == null ? "null" : potential.toString());
        buf.append(" - B:");
        buf.append(belief == null ? "null" : belief.toString());
        return buf.toString();
    }

    /**
     * Send messages to our neighboors (if applicable).
     */
    private long sendMessages() {
        long cc = 0;

        for (UPEdge<GdlNode, GdlMessage> e : getEdges()) {
            // Check if we are ready to send through this edge
            if (!readyToSend(e)) continue;

            // Instead of multiplying all incoming messages except the one
            // from e, we "substract" the e message from the belief, which
            // has the same result but with fewer operations.
            CostFunction msg;
            GdlMessage im = e.getMessage(this);
            if (im != null) {
                msg = belief.combine(im.getFactor().negate());
                cc += msg.getSize();
            } else {
                msg = belief;
            }

            // Summarize to the separator and send the resulting message
            msg = msg.summarize(e.getVariables());
            cc += msg.getSize();
            e.sendMessage(this, new GdlMessage(msg));
        }
        
        return cc;
    }

    @Override
    public boolean isConverged() {
        return belief.equals(previousBelief);
    }

    @Override
    public double getOptimalValue() {
        return belief.getValue(belief.getOptimalConfiguration(null));
    }

    @Override
    public VariableAssignment getOptimalConfiguration(VariableAssignment map) {
        if (belief == null) {
            return map;
        }
        return belief.reduce(map).getOptimalConfiguration(map);
    }

}
