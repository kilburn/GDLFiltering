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
import es.csic.iiia.dcop.mp.AbstractEdge;
import es.csic.iiia.dcop.up.UPEdge;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class JTEdge extends AbstractEdge<JTNode,JTMessage> {

    private static Logger log = LoggerFactory.getLogger(JunctionTree.class);

    private UPEdge edge;

    public JTEdge(JTNode n1, JTNode n2, UPEdge edge) {
        super(n1,n2);
        this.edge = edge;
    }

    protected void updateVariables() {
        
        HashSet<Variable> variables = new HashSet<Variable>();
        HashSet<Variable> v2 = getNode2().getNode().getVariables();
        HashSet<Variable> v3 = getNode1().getNode().getVariables();
        for(Variable v : v3) {
            if (v2.contains(v)) {
                variables.add(v);
            }
        }
        
        edge.setVariables(variables.toArray(new Variable[]{}));
    }

    @Override public boolean sendMessage(JTNode sender, JTMessage message) {
        boolean res = super.sendMessage(sender, message);
        if (res && log.isTraceEnabled())
            log.trace(sender.getName() + " -> " + getDestination(sender).getName() + " : " + message);
        return res;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
        buf.append(" [label=\"");
        int i=0;
        for (Variable v : edge.getVariables()) {
            if (i++>0) buf.append(",");
            buf.append(v.getName());
        }
        buf.append("\"];");
        return buf.toString();
    }

}
