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

package es.csic.iiia.dcop.bb;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.mp.AbstractNode;
import es.csic.iiia.dcop.vp.VPNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class UBNode extends AbstractNode<UBEdge, UBResult> {

    private static Logger log = LoggerFactory.getLogger(UBGraph.class);

    private double ub;
    private double localUB;
    private double lb;
    private double localLB;
    private VPNode node;

    CostFunction.Summarize summarize;
    CostFunction.Combine combine;

    public UBNode(VPNode node) {
        this.node = node;
         summarize = node.getFactory().getSummarizeOperation();
         combine = node.getFactory().getCombineOperation();
    }

    public String getName() {
        return node.getName();
    }

    @Override
    public boolean isConverged() {
        return true;
    }

    public void initialize() {
        // Operational mode
        setMode(Modes.TREE_UP);

        // Calculate the local optimum acording to the agreed solution
        localUB = node.getGlobalValue();
        localLB = node.getOptimalValue();
        //log.trace(this.getName() + " llb: " + localLB + ", lub: " + localUB);

        // Send initial messages (if applicable)
        ub = localUB;
        lb = localLB;
        sendMessages();
    }

    public long run() {
        long cc = 0;

        // Collect received values
        ub = localUB;
        lb = localLB;
        for (UBEdge e : getEdges()) {
            UBMessage msg = e.getMessage(this);
            if (msg != null) {
                ub = combine.eval(ub, msg.getUB());
                if (summarize.isBetter(msg.getLB(), lb)) {
                    lb = msg.getLB();
                }
            }
        }

        // Send updated messages
        sendMessages();

        // Reset state
        setUpdated(false);
        return cc;
    }

    public UBResult end() {
        return new UBResult(ub,lb);
    }

    private void sendMessages() {
        for (UBEdge e : getEdges()) {
            if (!readyToSend(e))
                continue;

            double mub = ub;
            UBMessage inMsg = e.getMessage(this);
            if (inMsg != null) {
                mub = combine.eval(ub, combine.invert(inMsg.getUB()));
            }
            UBMessage msg = new UBMessage();
            msg.setUB(mub);
            msg.setLB(lb);
            e.sendMessage(this, msg);
        }
    }

    @Override
    public boolean sentAndReceivedAllEdges() {
        boolean res = super.sentAndReceivedAllEdges();
        if (res && log.isTraceEnabled()) {
            log.trace("Node " + this.getName() + " done.");
        }
        return res;
    }

}