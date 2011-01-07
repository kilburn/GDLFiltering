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

import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.mp.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Value propagation message, carrying the variable/value pairs assigned by
 * the ancestor nodes.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VPMessage implements Message {
    final static double log2 = Math.log(2);

    private ArrayList<VariableAssignment> mappings;

    public VPMessage() {
        mappings = new ArrayList<VariableAssignment>();
    }
    public VPMessage(ArrayList<VariableAssignment> mappings) {
        this.mappings = new ArrayList<VariableAssignment>(mappings);
    }

    public void addMapping(VariableAssignment mapping) {
        mappings.add(mapping);
    }

    public ArrayList<VariableAssignment> getMappings() {
        return mappings;
    }

    public void filter(Set<Variable> vars) {
        ArrayList<VariableAssignment> newMappings =
                new ArrayList<VariableAssignment>(mappings.size());
        for (VariableAssignment map : mappings) {
            newMappings.add(map.filter(vars));
        }
        mappings = newMappings;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("VP:");
        for(VariableAssignment m : mappings) {
            buf.append("\n\t").append(m.toString());
        }
        return buf.toString();
    }

    public long getBytes() {
        double bits = 0;
        for(VariableAssignment m : mappings) {
            if (m == null) continue;
            for (Variable v : m.keySet()) {
                bits += requiredBits(v);
            }
        }
        return (long)Math.ceil(bits/8);
    }

    private static double requiredBits(Variable v) {
        return Math.log(v.getDomain()) / log2;
    }

}
