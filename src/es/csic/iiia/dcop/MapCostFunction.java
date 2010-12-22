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

import es.csic.iiia.dcop.util.CostFunctionStats;
import java.io.Serializable;
import java.util.HashMap;
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
public final class MapCostFunction extends AbstractCostFunction implements Serializable {

    /**
     * Configuration -> value mapping.
     */
    private HashMap<Integer, Double> map;

    /**
     * "Zero" value (value of the missing elements)
     */
    private double zero;

    /**
     * Counter of zeros
     */
    private int nZeros;

    /**
     * Creates a new CostFunction, initialized to the zero value.
     *
     * @param variables involved in this factor.
     */
    protected MapCostFunction(Variable[] variables, double zeroValue) {
        super(variables);
        
        map = new HashMap<Integer,Double>();
        zero = zeroValue;
        nZeros = 0;
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    protected MapCostFunction(CostFunction factor) {
        super(factor);
        
        if (factor instanceof MapCostFunction) {
            final MapCostFunction f = (MapCostFunction)factor;
            map = new HashMap<Integer,Double>(f.map);
            zero = f.zero;
        } else {
            map = new HashMap<Integer,Double>(factor.getSize() - factor.getNumberOfNoGoods());
            zero = getFactory().getSummarizeOperation().getNoGood();
            setValues(factor.getValues());
        }
        nZeros = factor.getNumberOfZeros();
    }

    /**
     * Resets the costFunction to "zero" values
     */
    private void reset() {
        map = new HashMap<Integer, Double>();
        nZeros = 0;
    }

    /** {@inheritDoc} */
    public double[] getValues() {
        double[] v = new double[size];
        for (int i=0; i<size; i++) {
            v[i] = getValue(i);
        }
        return v;
    }

    /** {@inheritDoc} */
    public void setValues(double[] values) {
        reset();

        if (values.length != size) {
            throw new IllegalArgumentException("Invalid index specification");
        }

        for (int i=0; i<size; i++) {
            setValue(i, values[i]);
        }
    }

    /** {@inheritDoc} */
    public Iterator<Integer> iterator() {
        return map.keySet().iterator();
    }

    /** {@inheritDoc} */
    public double getValue(int index) {
        if (index < 0 || index >= size) 
            throw new IndexOutOfBoundsException(Integer.toString(index));

        final Double v = map.get(index);
        return v == null ? zero : v;
    }

    /** {@inheritDoc} */
    public void setValue(int index, double value) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(Integer.toString(index) + " out of "
                    + size);

        final double prev = map.containsKey(index) ? map.get(index) : zero;

        if (prev == 0) {
            if (value == 0) {
                return;
            } else if (value == zero) {
                map.remove(index);
                nZeros--;
            } else {
                map.put(index, value);
                nZeros--;
            }
        } else {
            if (value == 0) {
                map.put(index, value);
                nZeros++;
            } else if (value == zero) {
                map.remove(index);
            } else {
                map.put(index, value);
            }
        }

    }

    /** {@inheritDoc} */
    public int getNumberOfNoGoods() {
        return size - map.size();
    }

    /** {@inheritDoc} */
    public int getNumberOfZeros() {
        return nZeros;
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return "S" + super.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append(" {");
        for(int i : map.keySet()) {
            buf.append(i);
            buf.append(":");
            buf.append(CostFunctionStats.formatValue(map.get(i)));
            buf.append(", ");
        }
        if (map.size()>0)
            buf.delete(buf.length()-2, buf.length());
        buf.append("}");

        return buf.toString();
    }

}
