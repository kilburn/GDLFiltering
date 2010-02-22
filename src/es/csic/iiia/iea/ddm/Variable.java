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

package es.csic.iiia.iea.ddm;

import es.csic.iiia.iea.ddm.util.IdGenerator;
import java.io.Serializable;

/**
 * Immutable discrete variable, represented by a name and it's domain (number
 * of possible states).
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public final class Variable implements Serializable, Comparable<Variable> {

    private final String name;

    private final int domain;

    private int id;

    /**
     * Constructs a new discrete variable.
     *
     * @param name name identifying this variable.
     * @param domain number of possible states.
     */
    public Variable(String name, int domain) {
        this.name = name;
        this.domain = domain;
        this.id = IdGenerator.getInstance().nextId();
    }

    /**
     * Constructs a new discrete variable.
     *
     * @param domain number of possible states.
     */
    public Variable(int domain) {
        this.domain = domain;
        this.id = IdGenerator.getInstance().nextId();
        this.name = String.valueOf(id);
    }

    /**
     * @return variable's name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return number of possible states.
     */
    public int getDomain() {
        return domain;
    }
    
    /**
     * @return identifier of this variable.
     */
    public int getId() {
        return id;
    }

    /**
     * Indicates whether some other variable is "equal to" this one.
     *
     * Variables are considered "equal" if, and only if, both variables have
     * the same name and domain (number of possible states).
     *
     * @param obj the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variable other = (Variable) obj;
        
        return other.getId() == id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("V(");
        buf.append(id);
        buf.append(",");
        buf.append(name);
        buf.append(",");
        buf.append(domain);
        buf.append(")");

        return buf.toString();
    }

    public int compareTo(Variable o) {
        return id - o.id;
    }

}
