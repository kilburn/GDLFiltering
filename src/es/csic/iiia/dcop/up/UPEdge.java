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

package es.csic.iiia.dcop.up;

import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.mp.AbstractEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.applet.resources.MsgAppletViewer;

/**
 * Utility Propagation edge.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class UPEdge<N extends UPNode, M extends UPMessage> extends AbstractEdge<N, M> {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    
    private Variable[] variables;

    /**
     * Constructs a new edge connecting the specified nodes through the given
     * variable.
     *
     * @param c1 clique on one side of the edge.
     * @param c2 clique on the other side of the edge.
     * @param variable member variable of the new edge.
     */
    public UPEdge(N c1, N c2, Variable variable) {
        super(c1,c2);
        this.variables = new Variable[]{variable};
    }

    /**
     * Constructs a new edge connecting the specified nodes.
     *
     * @param c1 clique on one side of the edge.
     * @param c2 clique on the other side of the edge.
     */
    public UPEdge(N c1, N c2) {
        super(c1,c2);
        this.variables = new Variable[]{};
    }

    public UPEdge(UPEdge<N, M> e) {
        super(e);
        this.variables = e.variables;
    }

    public Variable[] getVariables() {
        return this.variables;
    }

    public void setVariables(Variable[] variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(" [label=\"");
        int i=0;
        for (Variable v : variables) {
            if (i++>0) buf.append(",");
            buf.append(v.getName());
        }
        buf.append("\"];");
        return buf.toString();
    }

    /*
     * Code to make the stupid java compiler realize that these methods return
     * UP* objects, not *.
     */
    @Override public N getNode1() {return super.getNode1();}
    @Override public N getNode2() {return super.getNode2();}
    @Override public N getDestination(N node) {return super.getDestination(node);}

    @Override public boolean sendMessage(N sender, M message) {
        N recipient = sender == getNode1() ? getNode2() : getNode1();
        message.setDirection(
            haveSentMessage(recipient) ? UPMessage.DIR_DOWN : UPMessage.DIR_UP
        );

        boolean res = super.sendMessage(sender, message);
        if (res) {
            sender.addSentBytes(message.getBytes());
        }

        if (res && log.isTraceEnabled()) {
            log.trace(sender.getName() + " -> " + getDestination(sender).getName() + " : " + message);
        }

        return res;
    }

}
