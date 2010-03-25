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
import es.csic.iiia.dcop.mp.Message;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class JTMessage implements Message {

    private Set<Variable> variables;

    public JTMessage(Collection variables) {
        this.variables = new HashSet<Variable>(variables);
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    public void addVariable(Variable v) {
        variables.add(v);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("JT{");
        int i = variables.size();
        for (Variable v : variables) {
            buf.append(v.getName());
            if (--i != 0) buf.append(",");
        }
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JTMessage)) {
            return false;
        }

        JTMessage msg = (JTMessage)other;
        return (this.variables == null && msg.getVariables() == null )
            || (this.variables.equals(msg.getVariables()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.variables != null ? this.variables.hashCode() : 0);
        return hash;
    }
}