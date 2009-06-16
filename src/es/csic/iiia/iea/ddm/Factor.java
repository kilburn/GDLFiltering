/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2009, IIIA-CSIC, Artificial Intelligence Research Institute
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

/**
 * Valuation over a set of discrete variables.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Factor implements Serializable {

    /**
     * Perform the combine operator using product.
     */
    public static final int COMBINE_PRODUCT = 1;

    /**
     * Perform the combine operator using addition.
     */
    public static final int COMBINE_SUM = 0;

    /**
     * Perform the summarize operator using addition.
     */
    public static final int SUMMARIZE_SUM = 0;

    /**
     * Perform the summzarize operator using maximum.
     */
    public static final int SUMMARIZE_MAX = 1;

    /**
     * Perform the summzarize operator using minimum.
     */
    public static final int SUMMARIZE_MIN = 2;

    /**
     * Normalize so that all values add to 1.
     */
    public static final int NORMALIZE_SUM1 = 1;

    /**
     * Normalize so that all values add to 0.
     */
    public static final int NORMALIZE_SUM0 = 0;

    /**
     * Do not normalize.
     */
    public static final int NORMALIZE_NONE = 2;

    private Variable[] variables;
    private HashSet<Variable> variableSet;

    private int size;

    public int getSize() {
        return size;
    }

    private double[] values;

    /**
     * List of aggregated dimensionality up to "i".
     */
    private int[] sizes;

    /**
     * Creates a new Factor, initializing the values to initValue.
     *
     * @param variables involved in this factor.
     * @param initValue initial value.
     */
    public Factor(Variable[] variables, double initValue) {
        this(variables);
        for (int i=0; i<size; i++) {
            values[i] = initValue;
        }
    }

    /**
     * Creates a new Factor, initialized to zeros.
     *
     * @param variables involved in this factor.
     */
    public Factor(Variable[] variables) {
        this.variables = variables;
        this.variableSet = new HashSet<Variable>(Arrays.asList(variables));
        final int len = variables.length;

        size = 1;
        sizes = new int[len];
        boolean overflow = false;
        for (int i=0; i<len; i++) {
            sizes[i] = size;
            size *= variables[len-i-1].getDomain();
            if (size < 0) overflow = true;
        }

        if (!overflow) {
            values = new double[size];
        } else {
            size = -1;
        }
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    public Factor(Factor factor) {
        variables = factor.variables.clone();
        variableSet = new HashSet<Variable>(factor.variableSet);
        values = factor.values.clone();
        size = factor.size;
        sizes = factor.sizes.clone();
    }

    public double[] getValues() {
        return values;
    }

    /**
     * Gets the set of variables of this factor.
     *
     * @return set of variables of this factor.
     */
    public HashSet<Variable> getVariableSet() {
        return variableSet;
    }

    /**
     * Sets the value of this factor for the given variable states.
     *
     * @param index list of variable states.
     * @param value corresponding factor value.
     */
    public void setValue(int[] index, double value) {
        this.values[subindexToIndex(index)] = value;
    }

    /**
     * Gets the value of this factor for the given variable states.
     *
     * @param index list of variable states.
     * @return value corresponding factor value.
     */
    public double getValue(int[] index) {
        return this.values[subindexToIndex(index)];
    }

    /**
     * Gets the value of this factor for the given variable/value mapping.
     *
     * @param mapping variable/value mapping.
     * @return value corresponding factor value.
     */
    public double getValue(Hashtable<Variable,Integer> mapping) {
        return this.values[this.getIndex(mapping)];
    }

    /**
     * Returns the values array index corresponding to the given subindices
     * (ordered list of values for each variable of this factor).
     *
     * @param subindex subincides list.
     * @return corresponding values array index.
     */
    protected int subindexToIndex(int [] subindex) {
        // Check index lengths
        if (subindex.length != sizes.length) {
            throw new IllegalArgumentException("Invalid index specification");
        }

        // Compute subindex -> index offset
        int idx = 0;
        for (int i=0; i<subindex.length; i++) {

            // Check domain limits
            if (variables[i].getDomain() <= subindex[i]) {
                throw new IllegalArgumentException("Invalid index for dimension " + i);
            }

            idx += sizes[subindex.length-i-1] * subindex[i];
        }

        return idx;
    }

    /**
     * Returns the subindices list (ordered list of values for each variable of
     * this factor) corresponding to the given values array index.
     * index.
     *
     * @param index values array index.
     * @return subindices list.
     */
    protected int[] indexToSubindex(int index) {
        int[] idx = new int[variables.length];

        final int len = variables.length;
        for (int i=0; i<len; i++) {
            final int ii = len - 1 - i;
            idx[i] = index / sizes[ii];
            index = index % sizes[ii];
        }

        return idx;
    }

    /**
     * Sets the complete list of values for all possible variable states.
     *
     * @param values list of values for all possible variable states.
     */
    public void setValues(double[] values) {
        
        if (values.length != this.values.length) {
            throw new IllegalArgumentException("Invalid index specification");
        }

        this.values = values;
    }

    /**
     * Returns the variable/value mapping corresponding to the specified
     * index of the values array.
     *
     * @param index of the values array.
     * @param mapping mapping table to fill, instantiated if null.
     * @return variable/value mapping corresponding to the given index.
     */
    public Hashtable<Variable, Integer> getMapping(int index, 
            Hashtable<Variable, Integer> mapping)
    {
        if (mapping == null) {
            mapping = new Hashtable<Variable,Integer>(variables.length);
        } else {
            mapping.clear();
        }

        int[] sub = indexToSubindex(index);
        for (int i=0, len=variables.length; i<len; i++) {
            mapping.put(variables[i], sub[i]);
        }

        return mapping;
    }

    /**
     * Returns the index of the values array corresponding to the specified
     * variables mapping.
     *
     * @param mapping variable/value mapping table.
     * @return index of the values array corresponding to the given mapping.
     */
    public int getIndex(Hashtable<Variable, Integer> mapping) {

        final int len = variables.length;
        int idx = 0;
        for (int i=0; i<len; i++) {
            try {
                idx += sizes[len-i-1] * mapping.get(variables[i]);
            } catch(Exception e) {
                System.err.println("ouch");
            }
        }
        return idx;
    }

    /**
     * Combine this factor with the given one, using the specified operation.
     *
     * @param factor factor to combine with.
     * @param operation operation to use.
     * @return a new Factor which is the result of the combination between this
     * and the given one.
     */
    public Factor combine(Factor factor, int operation) {

        // Combination with null factors gives a copy of ourselves
        if (factor == null) {
            return new Factor(this);
        }

        // Compute the variable set intersection (sets doesn't allow duplicates)
        HashSet<Variable> vars = new HashSet<Variable>(variableSet);
        vars.addAll(factor.variableSet);

        // Perform the combination using the given mode
        final Variable[] v = new Variable[vars.size()];
        Factor result = new Factor(vars.toArray(v), operation%10);
        
        Hashtable<Variable, Integer> map = null;
        for(int i=0, len=result.size; i<len; i++) {
            map = result.getMapping(i, map);

            switch(operation) {
                case COMBINE_PRODUCT:
                    result.values[i] = getValue(map) * factor.getValue(map);
                    break;

                case COMBINE_SUM:
                    result.values[i] = getValue(map) + factor.getValue(map);
                    break;

            }
            
        }

        return result;
    }

    /**
     * Summarize this factor over the specified variables, using the given
     * operation.
     *
     * @param vars variables to summarize.
     * @param operation operation to use.
     * @return a new Factor which is the result of summarizing this one over
     * the specified variables.
     */
    public Factor summarize(Variable[] vars, int operation) {

        /*HashSet<Variable> varSet = new HashSet<Variable>(Arrays.asList(vars));
        varSet.retainAll(variableSet);*/

        Factor result = new Factor(vars,
                operation == SUMMARIZE_MAX ? Double.NEGATIVE_INFINITY :
                    (operation == SUMMARIZE_MIN ? Double.POSITIVE_INFINITY : 0));
        Hashtable<Variable, Integer> map = null;
        for (int i=0, len=size; i<len; i++) {
            map = getMapping(i, map);
            final int idx = result.getIndex(map);
            
            switch(operation) {
                case SUMMARIZE_MAX:
                    result.values[idx] = Math.max(values[i], result.values[idx]);
                    break;
                    
                case SUMMARIZE_SUM:
                    result.values[idx] += values[i];
                    break;

                case SUMMARIZE_MIN:
                    result.values[idx] = Math.min(values[i], result.values[idx]);
                    break;
            }
            
        }

        return result;
    }

    /**
     * Negates this factor, applying the inverse of the given operation to
     * all it's values.
     *
     * @see #combine(es.csic.iiia.iea.ddm.Factor, int)
     * @param operation operation to use.
     */
    public void negate(int operation) {

        for (int i=0, len=size; i<len; i++) {

            switch(operation) {
                case COMBINE_PRODUCT:
                    values[i] = 1/values[i];
                    break;

                case COMBINE_SUM:
                    values[i] = -values[i];
                    break;
            }
            
        }

    }

    /**
     * Normalizes this factor in the specified mode.
     *
     * @param mode Normalization mode to use.
     */
    public void normalize(int mode) {

        if (mode == NORMALIZE_NONE) return;

        // Calculate aggregation
        final int len = values.length;
        double sum = 0;
        for (int i=0; i<len; i++) {
            sum += values[i];
        }

        final double dlen = (double)len;
        switch(mode) {
            case NORMALIZE_SUM0:
                final double avg = sum/dlen;
                for (int i=0; i<len; i++) {
                    values[i] -= avg;
                }
                break;

            case NORMALIZE_SUM1:
                // Avoid div by 0
                for (int i=0; i<len; i++) {
                    values[i] = sum!=0 ? values[i]/sum : 1/dlen;
                }
                break;
        }

    }

    /**
     * Reduces the factor, fixing the variable-value pairs of the mapping
     * table.
     *
     * @param mapping variable-value pairs to fix.
     * @return new reduced factor.
     */
    public Factor reduce(Hashtable<Variable, Integer> mapping) {

        // Calculate the new factor's variables
        HashSet<Variable> newVariables = new HashSet<Variable>(variableSet);
        newVariables.removeAll(mapping.keySet());
        if (newVariables.size() == 0) {
            return null;
        }
        final Variable[] v = new Variable[newVariables.size()];

        // Instantiate it
        Factor result = new Factor(newVariables.toArray(v));

        Hashtable<Variable, Integer> map = null;
        for (int i=0, len=result.size; i<len; i++) {
            map = result.getMapping(i, map);
            map.putAll(mapping);
            final int idx = getIndex(map);
            result.values[i] = values[idx];
        }

        return result;
    }

    /**
     * Instantiates the variables of this factor into the given mapping such
     * that it is an "optimal" configuration.
     *
     * @param mapping current variable mappings.
     * @param operation summarizing operation used.
     */
    public void getBestConfiguration(Hashtable<Variable, Integer> mapping, int operation) {
        
        // Find the maximal value
        ArrayList<Integer> idx = new ArrayList<Integer>();
        if (operation == SUMMARIZE_MAX) {
            double max = Double.NEGATIVE_INFINITY;
            for (int i=0; i<values.length; i++) {
                if (values[i] > max) {
                    max = values[i];
                    idx.clear();
                }
                if (values[i] >= max) {
                    idx.add(i);
                }
            }
        } else if (operation == SUMMARIZE_MIN) {
            double min = Double.POSITIVE_INFINITY;
            for (int i=0; i<values.length; i++) {
                if (values[i] < min) {
                    min = values[i];
                    idx.clear();
                }
                if (values[i] <= min) {
                    idx.add(i);
                }
            }
        }

        int i = idx.get(new Random().nextInt(idx.size()));
        // Retrieve it's mapping
        mapping.putAll(getMapping(i, null));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Factor other = (Factor) obj;
        
        return equals(other, 0.0001);
    }

    /**
     * Indicates whether some other factor is "equal to" this one, concerning a
     * delta.
     *
     * @param other the reference object with which to compare.
     * @param delta the maximum delta between factor values for which both
     * numbers are still considered equal.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    public boolean equals(Factor other, double delta) {
        
        if (other == null) {
            return false;
        }

        if (this.variableSet != other.variableSet &&
                (this.variableSet == null ||
                !this.variableSet.equals(other.variableSet)))
        {
            return false;
        }

        Hashtable<Variable, Integer> map = null;
        for(int i=0, len=this.size; i<len; i++) {
            map = this.getMapping(i, map);
            final double e = this.values[i] - other.values[other.getIndex(map)];
            if (Math.abs(e) > delta) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.variableSet != null ? this.variableSet.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(getName());
        buf.append(" {");
        buf.append(values[0]);
        for(int i=1; i<values.length; i++) {
            buf.append(",");
            buf.append(values[i]);
        }
        buf.append("}");

        return buf.toString();
    }

    public String getName() {
        StringBuffer buf = new StringBuffer();
        buf.append("F(");
        if (variables.length>0) {
            buf.append(variables[0].getName());
            for(int i=1; i<variables.length; i++) {
                buf.append(",");
                buf.append(variables[i].getName());
            }
        }
        buf.append(")");

        return buf.toString();
    }

    public double getMinValue() {
        double min = Double.POSITIVE_INFINITY;
        for (int i=0; i<values.length; i++) {
            if (values[i] < min) min = values[i];
        }
        return min;
    }

}
