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

package es.csic.iiia.dcop.vp;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.mp.AbstractNode;
import es.csic.iiia.dcop.up.UPNode;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value Propagation algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VPNode extends AbstractNode<VPEdge, VPResult> {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);
    
    private CostFunction belief;
    private VariableAssignment mapping;
    private UPNode node;

    public VPNode(UPNode n) {
        node = n;
        belief = n.getBelief();
    }

    public void initialize() {
        setMode(Modes.TREE_DOWN);
    }

    public long run() {
        long cc = 0;

        // Receive incoming messages
        mapping = new VariableAssignment();
        for(VPEdge e : getEdges()) {
            VPMessage msg = e.getMessage(this);
            if (msg != null) {
                mapping.putAll(msg.getMapping());
                cc += msg.getMapping().size();
            }
        }

        // Reduce our belief with the given mapping
        belief = belief.reduce(mapping);

        // Instantiate remaining variables
        if (belief != null) {
            cc += belief.getSize();
            mapping = belief.getOptimalConfiguration(mapping);
        }

        // Send messages
        for(VPEdge e : getEdges()) {
            if (!readyToSend(e))
                continue;

            VPMessage msg = new VPMessage(mapping);
            e.sendMessage(this, msg);
        }

        setUpdated(false);
        return cc;
    }


    public boolean isConverged() {
        // As convergence is checked after the first iteration and this is a
        // tree, it is granted.
        return true;
    }

    public VPResult end() {
        return new VPResult(mapping);
    }

    public String getName() {
        return node.getName();
    }

    @Override
    public String toString() {
        return node.getName();
    }

    @Override
    protected boolean sentOrReceivedAnyEdge() {
        boolean res = super.sentOrReceivedAnyEdge();
        if (res && log.isTraceEnabled()) {
            log.trace(this + " done.");
        }
        return res;
    }
    
    public double getGlobalValue() {
        double value = 0;
        final CostFunction.Combine op = node.getFactory().getCombineOperation();
        Collection<CostFunction> fs = node.getRelations();
        for (CostFunction f : fs) {
            value = op.eval(value, f.getValue(mapping));
        }
        return value;
    }

    public double getOptimalValue() {
        return node.getOptimalValue();
    }

    public CostFunctionFactory getFactory() {
        return node.getFactory();
    }

}
