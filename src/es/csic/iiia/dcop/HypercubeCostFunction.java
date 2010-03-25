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

import java.io.Serializable;
import java.util.Iterator;

/**
 * Cost Function implementation that stores the whole hypercube of values in
 * a linearized array of doubles. Therefore, this is the fastest implementation
 * for algorithms that always work over the complete hypercubes. In contrast,
 * algorithms that use filtering techniques will prefer to use other (sparse)
 * representations of the hypercubes.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class HypercubeCostFunction extends AbstractCostFunction implements Serializable {

    /**
     * Hypercube values storage array.
     */
    private double[] values;

    /**
     * Creates a new CostFunction, initialized to zeros.
     *
     * @param variables involved in this factor.
     */
    protected HypercubeCostFunction(Variable[] variables) {
        super(variables);
        values = new double[size];
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    protected HypercubeCostFunction(CostFunction factor) {
        super(factor);
        values = factor.getValues();
    }

    /** {@inheritDoc} */
    public double[] getValues() {
        return values.clone();
    }

    /** {@inheritDoc} */
    public void setValues(double[] values) {
        
        if (values.length != this.values.length) {
            throw new IllegalArgumentException("Invalid index specification");
        }

        this.values = values;
    }

    /** {@inheritDoc} */
    public Iterator<Integer> iterator() {
        return new HypercubeIterator();
    }

    /** {@inheritDoc} */
    protected double getValue(int index) {
        return values[index];
    }

    /** {@inheritDoc} */
    public void setValue(int index, double value) {
        values[index] = value;
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return "H" + super.getName();
    }

    /** {@inheritDoc} */
    /*public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(" {");
        if (values != null && values.length>0) {
            buf.append(formatValue(values[0]));
            for(int i=1; i<values.length; i++) {
                buf.append(",");
                buf.append(formatValue(values[i]));
            }
        }
        buf.append("}");

        return buf.toString();
    }*/

    /**
     * Implements the Iterator interface for an hypercube, allowing to iterate
     * over its elements using the common java conventions.
     */
    protected class HypercubeIterator implements Iterator<Integer> {
        private int idx = 0;

        @Override
        public boolean hasNext() {
            return idx < size;
        }

        @Override
        public Integer next() {
            return idx++;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("You can not remove elements from an hypercube.");
        }

    }

}