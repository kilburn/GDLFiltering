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

package es.csic.iiia.dcop.dsa;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.mp.AbstractNode;
import java.util.ArrayList;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class DSANode extends AbstractNode<DSAEdge, DSAResult> {

    private static Logger log = LoggerFactory.getLogger(DSA.class);

    private Variable variable;
    private ArrayList<CostFunction> factors;
    private boolean converged;
    private VariableAssignment myAssignment;
    private int previousAssignment = Integer.MAX_VALUE;
    private VariableAssignment othersAssignments;
    private Random random;
    private double p;

    public DSANode(Variable variable, Random random, double p) {
        this.variable = variable;
        this.random = random;
        this.factors = new ArrayList<CostFunction>();
        this.p = p;
    }

    public Variable getVariable() {
        return this.variable;
    }

    public void addFactor(CostFunction factor) {
        factors.add(factor);
    }

    @Override
    public void initialize() {
        setMode(Modes.GRAPH);
        othersAssignments = new VariableAssignment();
        myAssignment = new VariableAssignment(1);

        // Randomly choose an initial assignment
        int conf = random.nextInt(variable.getDomain());
        myAssignment.put(variable, conf);

        // Send messages out
        this.sendMessages();
    }

    private void sendMessages() {
        // First check if we do not have to send out new messages
        final int currentAssignment = myAssignment.get(variable);
        if (currentAssignment == previousAssignment) {
            converged = true;
            return;
        }
        converged = false;
        previousAssignment = currentAssignment;

        // Otherwise, send updated messages to all neighs
        for (DSAEdge e : getEdges()) {
            DSAMessage m = new DSAMessage(myAssignment);
            e.sendMessage(this, m);
        }
    }

    public void run() {
        if (log.isTraceEnabled()) {
            log.trace("Node " + this.getName() + " running.");
        }

        // CC count
        long cc = 0;

        // Stochastically choose where to update or not
        if (random.nextDouble() > p) {
            return;
        }


        // Collect received assignments
        for (DSAEdge e : getEdges()) {
            final DSAMessage m = e.getMessage(this);
            othersAssignments.putAll(m.getAssignment());
        }

        // Choose a new assignment for the owned variable
        CostFunction combi = null;
        for (CostFunction f : factors) {
            if (combi == null) {
                combi = f.reduce(othersAssignments);
            } else {
                combi = combi.combine(f.reduce(othersAssignments));
            }
        }
        myAssignment = combi.getOptimalConfiguration(myAssignment);
        if (Double.isInfinite(combi.getValue(myAssignment))) {
            myAssignment.put(variable, random.nextInt(variable.getDomain()));
        }

        if (log.isTraceEnabled()) {
            log.trace("Prev:  " + previousAssignment);
            log.trace("Combi: " + combi);
            log.trace("Next:  " + myAssignment.get(variable));
        }

        // Send updated messages
        this.sendMessages();
        
        // And finish.
        setUpdated(false);
    }

    public boolean isConverged() {
        if (converged && log.isTraceEnabled()) {
            log.trace("Node " + this.getName() + " done.");
        }
        return converged;
    }

    public DSAResult end() {
        return new DSAResult(this);
    }

    public String getName() {
        return variable.getName();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("N:");
        buf.append(variable.getName()).append("\n");
        for (CostFunction f : factors) {
            buf.append("\t" + f);
        }
        return getName();
        //return buf.toString();
    }

    @Override
    public void setUpdated(boolean updated) {
        if (updated == true) {
            converged = false;
        }

        super.setUpdated(updated);
    }

    VariableAssignment getAssignment() {
        return myAssignment;
    }

}
