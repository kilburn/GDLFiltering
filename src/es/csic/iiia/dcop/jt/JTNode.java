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

package es.csic.iiia.dcop.jt;

import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.mp.AbstractNode;
import es.csic.iiia.dcop.mp.Edge;
import es.csic.iiia.dcop.mp.Result;
import es.csic.iiia.dcop.up.UPNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class JTNode extends AbstractNode<JTEdge, Result> {

    private static Logger log = LoggerFactory.getLogger(JunctionTree.class);

    private UPNode node;
    
    private Set<Variable> reachableVariables;
    private HashMap<Edge, HashSet<Variable>> variablesSent;
    private boolean converged;

    public JTNode(UPNode node) {
        this.node = node;
    }

    public UPNode getNode() {
        return this.node;
    }

    @Override
    public void initialize() {
        setMode(Modes.GRAPH);

        variablesSent = new HashMap<Edge, HashSet<Variable>>(getEdges().size());
        Set variables = node.getVariables();
        for (Edge e : getEdges()) {
            HashSet<Variable> variablesSentEdge = new HashSet<Variable>(variables);
            variablesSent.put(e, variablesSentEdge);
            e.sendMessage(this, new JTMessage(variables));
        }

        reachableVariables = new HashSet<Variable>(variables);
    }

    public void run() {
        converged = true;

        // List of new variables received
        ArrayList<Variable> newVariables = new ArrayList<Variable>();

        // Track received variables
        for (JTEdge e : getEdges()) {
            JTMessage msg = e.getMessage(this);
            for (Variable v : msg.getVariables()) {
                if (reachableVariables.contains(v)) {
                    node.addVariable(v);
                } else {
                    converged = false;
                }
                reachableVariables.add(v);
                newVariables.add(v);
            }
        }

        // Send updated messages
        for (JTEdge e : getEdges()) {
            HashSet<Variable> variablesSentEdge = variablesSent.get(e);
            ArrayList<Variable> msg = new ArrayList<Variable>(newVariables);
            for (Variable v : e.getMessage(this).getVariables()) {
                msg.remove(v);
            }
            msg.removeAll(variablesSentEdge);
            variablesSentEdge.addAll(msg);
            e.sendMessage(this, new JTMessage(msg));
        }
        
        // And finish.
        setUpdated(false);
    }

    public boolean isConverged() {
        if (converged && log.isTraceEnabled()) {
            log.trace("Node " + this.getName() + " done.");
        }
        return converged;
    }

    public Result end() {
        // Finally, update edge variables
        for (JTEdge e : getEdges()) {
            e.updateVariables();
        }

        return new JTResult(node);
    }

    public String getName() {
        return node.getName();
    }

    @Override
    public String toString() {
        return node.toString();
    }

}
