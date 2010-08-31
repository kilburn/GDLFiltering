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

package es.csic.iiia.dcop;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VariableAssignment implements Map<Variable, Integer> {
    private HashMap<Variable, Integer> assignment;

    public VariableAssignment() {
        assignment = new HashMap<Variable, Integer>();
    }

    public VariableAssignment(int initialCapacity) {
        assignment = new HashMap<Variable, Integer>(initialCapacity);
    }

    public VariableAssignment(int initialCapacity, float loadFactor) {
        assignment = new HashMap<Variable, Integer>(initialCapacity, loadFactor);
    }

    public VariableAssignment(Map m) {
        assignment = new HashMap<Variable, Integer>(m);
    }

    public int size() {
        return assignment.size();
    }

    public boolean isEmpty() {
        return assignment.isEmpty();
    }

    @SuppressWarnings("element-type-mismatch")
    public boolean containsKey(Object key) {
        return assignment.containsKey(key);
    }

    @SuppressWarnings("element-type-mismatch")
    public boolean containsValue(Object value) {
        return assignment.containsValue(value);
    }

    @SuppressWarnings("element-type-mismatch")
    public Integer get(Object key) {
        return assignment.get(key);
    }

    public Integer put(Variable key, Integer value) {
        return assignment.put(key, value);
    }

    @SuppressWarnings("element-type-mismatch")
    public Integer remove(Object key) {
        return assignment.remove(key);
    }

    public void putAll(Map<? extends Variable, ? extends Integer> m) {
        assignment.putAll(m);
    }

    public void clear() {
        assignment.clear();
    }

    public Set<Variable> keySet() {
        return assignment.keySet();
    }

    public Collection<Integer> values() {
        return assignment.values();
    }

    public Set<Entry<Variable, Integer>> entrySet() {
        return assignment.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VariableAssignment) {
            VariableAssignment v = (VariableAssignment)o;
            return assignment.equals(v.assignment);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.assignment != null ? this.assignment.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        // Sorted for an easier visualization
        TreeMap<Variable, Integer> sorted = new TreeMap(assignment);

        StringBuilder buf = new StringBuilder("{");
        int i = sorted.size();
        for(Variable v : sorted.keySet()) {
            buf.append(v.getName()).append(":").append(sorted.get(v));
            if (--i != 0) buf.append(",");
        }
        return buf.append("}").toString();
    }

}
