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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Cost Function implementation that stores the hypercube of values in a
 * HashMap.
 *
 * Therefore, this implementation is slower than HypercubeCostFunction
 * for algorithms that always work over the complete hypercubes. In contrast,
 * it might be faster for algorithms that use filtering techniques because
 * of its sparse nature.
 *
 * @see HashMap
 * @see HypercubeCostFunction
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class ListCostFunction extends AbstractCostFunction {

    /**
     * Internal storage for the hypercube's values.
     */
    private HashMap<Integer, Double> values;

    /**
     * Creates a new CostFunction, initializing the values to initValue.
     *
     * @param variables involved in this factor.
     * @param initValue initial value.
     */
    protected ListCostFunction(Variable[] variables, double initValue) {
        this(variables);
        for (int i=0; i<size; i++) {
            values.put(i, initValue);
        }
    }

    /**
     * Creates a new CostFunction, initialized to zeros.
     *
     * @param variables involved in this factor.
     */
    private ListCostFunction(Variable[] variables) {
        super(variables);

        if (size>0) {
            values = new HashMap<Integer, Double>(size);
        }
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    protected ListCostFunction(CostFunction factor) {
        super(factor);
        if (factor instanceof ListCostFunction) {
            values = new HashMap<Integer, Double>(((ListCostFunction)factor).getValueMap());
        } else {
            if (factor instanceof AbstractCostFunction) {
                final AbstractCostFunction f = (AbstractCostFunction)factor;
                values = new HashMap<Integer, Double>(size);
                for (int i=0; i<size; i++) {
                    setValue(i, f.getValue(i));
                }
            } else {
                throw new IllegalArgumentException("Unable to copy-construct from this factor");
            }
        }
    }

    private HashMap<Integer, Double> getValueMap() {
        return values;
    }

    @Override
    public double getValue(int index) {
        if (values.containsKey(index)) {
            return values.get(index);
        }
        throw new RuntimeException("Failure");
    }

    @Override
    public void setValue(int index, double value) {
        values.put(index, value);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new ArrayList<Integer>(values.keySet()).iterator();
    }

    public double[] getValues() {
        if (values == null)
            return null;

        Iterator<Integer> it = iterator();
        double[] vs = new double[size];
        while (it.hasNext()) {
            final int i = it.next();
            vs[i] = getValue(i);
        }
        return vs;
    }

    public void setValues(double[] values) {
        if (values.length != size) {
            throw new IllegalArgumentException("Invalid index specification");
        }
        for (int i=0; i<size; i++) {
            setValue(i, values[i]);
        }
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return "L" + super.getName();
    }

}
