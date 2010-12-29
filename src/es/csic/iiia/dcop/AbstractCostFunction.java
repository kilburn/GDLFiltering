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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Base implementation of a cost function.
 *
 * This class provides some basic methods implementing the operations that can
 * be made over cost functions, while delegating the actual cost/utility values
 * representation/storage to the concrete class that extends it.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractCostFunction implements CostFunction {

    /**
     * Ordered list of variables involved in this function.
     */
    protected Variable[] variables;

    /**
     * Unordered set of variables involved in this function.
     */
    protected LinkedHashSet<Variable> variableSet;

    /**
     * Total size (in elements) of the hypercube formed by this function's
     * variables.
     */
    protected int size;

    /**
     * List of aggregated dimensionality up to "index".
     */
    protected int[] sizes;

    /**
     * The factory that generated this CostFunction.
     */
    private CostFunctionFactory factory;

    /**
     * Creates a new CostFunction, with unknown values.
     *
     * @param variables involved in this factor.
     */
    public AbstractCostFunction(Variable[] variables) {
        this.variables = variables;
        this.variableSet = new LinkedHashSet<Variable>(Arrays.asList(variables));
        computeFunctionSize();
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    public AbstractCostFunction(CostFunction factor) {
        factory = factor.getFactory();
        variableSet = new LinkedHashSet<Variable>(factor.getVariableSet());
        variables = variableSet.toArray(new Variable[0]);
        if (factor instanceof AbstractCostFunction) {
            size = factor.getSize();
            sizes = ((AbstractCostFunction)factor).getSizes().clone();
        } else {
            computeFunctionSize();
        }
    }

    /** {@inheritDoc} */
    public void initialize(Double initialValue) {
        for (int i=0; i<size; i++) {
            setValue(i, initialValue);
        }
    }

    /** {@inheritDoc} */
    public void setFactory(CostFunctionFactory factory) {
        this.factory = factory;
    }

    /** {@inheritDoc} */
    public CostFunctionFactory getFactory() {
        return factory;
    }

    /**
     * Computes the function's size and dimensionalities.
     * @see #size
     * @see #sizes
     */
    private void computeFunctionSize() {
        final int len = variables.length;
        size = 1;
        sizes = new int[len];
        boolean overflow = false;
        for (int i=0; i<len; i++) {
            sizes[i] = size;
            size *= variables[len-i-1].getDomain();
            if (size < 0) {
                overflow = true;
                break;
            }
        }

        if (overflow) {
            size = -1;
        }
    }

    /**
     * Gets the value of this factor for the given linearized index.
     *
     * @param index linearized index.
     * @return value corresponding factor value.
     */
    public abstract double getValue(int index);

    /**
     * Gets the value of this factor for the given linearized index.
     *
     * @param index linearized index.
     * @param value of the given index.
     */
    public abstract void setValue(int index, double value);

    /**
     * Obtains an iterator over the linearized indices of non-infinity elements of this
     * cost function.
     * 
     * @return Iterator over the indices of this cost function.
     */
    public abstract Iterator<Integer> iterator();

    /** {@inheritDoc} */
    public VariableAssignment getOptimalConfiguration(VariableAssignment mapping) {
        if (mapping == null) {
            mapping = new VariableAssignment(variables.length);
        }

        // Empty cost functions have no optimal value
        if (variables.length == 0) {
            return mapping;
        }

        int i = getOptimalConfiguration();
        if (i < 0) {i = new Random().nextInt(size);}
        mapping.putAll(getMapping(i, null));
        return mapping;
    }

    /** {@inheritDoc} */
    public int getOptimalConfiguration() {
        // Find the maximal value
        Summarize operation = factory.getSummarizeOperation();
        ArrayList<Integer> idx = new ArrayList<Integer>();
        double optimal = operation.getNoGood();
        Iterator<Integer> it = iterator();
        while(it.hasNext()) {
            final int i = it.next();
            final double value = getValue(i);
            if (operation.isBetter(value, optimal)) {
                optimal = value;
                idx.clear();
                idx.add(i);
            } else if (value == optimal) {
                idx.add(i);
            }
        }

        if (idx.isEmpty()) {
            return -1;
        }

        return idx.get(new Random().nextInt(idx.size()));
    }

    /**
     * Get the linearized index corresponding to the given variable mapping.
     * 
     * Warning: if there's more than one item matching the specified mapping,
     * only the first one is returned by this function!
     *
     * @param mapping of the desired configuration.
     * @return corresponding linearized index.
     */
    public int getIndex(VariableAssignment mapping) {
        final int len = variables.length;
        if (len == 0) {
            // This can be an empty or a constant factor
            return size == 0 ? -1 : 0;
        }

        int idx = 0;
        for (int i = 0; i < len; i++) {
            Integer v = mapping.get(variables[i]);
            if (v != null) {
                idx += sizes[len - i - 1] * v;
            } else {
                
            }
        }
        return idx;
    }

    /**
     * Get the linearized index corresponding to the given variable mapping.
     *
     * @param mapping of the desired configuration.
     * @return corresponding linearized index.
     */
    public ArrayList<Integer> getIndexes(VariableAssignment mapping) {
        ArrayList<Integer> idxs = new ArrayList<Integer>();

        final int len = variables.length;
        if (len == 0) {
            if (size > 0) {
                idxs.add(0);
            }
            return idxs;
        }
        idxs.add(0);
        
        for (int i = 0; i < len; i++) {
            Integer v = mapping.get(variables[i]);
            if (v != null) {
                // We might be tracking multiple valid indidces
                for (int j = 0; j < idxs.size(); j++) {
                    idxs.set(j, idxs.get(j) + sizes[len - i - 1] * v);
                }
            } else {
                // For each current index, we have to spawn "n" new indices,
                // where "n" is the free variable dimensionality
                for (int j = 0, ilen = idxs.size(); j < ilen; j++) {
                    final int n = variables[i].getDomain();
                    for (v = 0; v < n; v++) {
                        idxs.add(idxs.get(j) + sizes[len - i - 1] * v);
                    }
                }
            }
        }
        return idxs;
    }

    /**
     * Get the variable mapping corresponding to the given linearized index.
     *
     * @param index linearized index of the desired configuration.
     * @param mapping variable mapping to be filled. If null, a new mapping
     *                is automatically instantiated.
     * @return variable mapping filled with the desired configuration.
     */
    public VariableAssignment getMapping(int index, VariableAssignment mapping) {
        if (mapping == null) {
            mapping = new VariableAssignment(variables.length);
        } else {
            mapping.clear();
        }
        int[] sub = indexToSubindex(index);
        for (int i = 0, len = variables.length; i < len; i++) {
            mapping.put(variables[i], sub[i]);
        }
        return mapping;
    }

    /**
     * Get the function's size (in number of possible configurations).
     * @return number of function's possible configurations.
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the functions's aggregated dimesionalities vector.
     * @return function's aggregated dimensionalities vector.
     */
    protected int[] getSizes() {
        return sizes;
    }

    /** {@inheritDoc} */
    public String getName() {
        StringBuilder buf = new StringBuilder();
        buf.append("F(");
        if (variables.length > 0) {
            buf.append(variables[0].getName());
            for (int i = 1; i < variables.length; i++) {
                buf.append(",");
                buf.append(variables[i].getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append(" {");
        if (size>0 && getValues() != null) {
            buf.append(CostFunctionStats.formatValue(getValue(0)));
            for(int i=1; i<size; i++) {
                buf.append(",");
                buf.append(CostFunctionStats.formatValue(getValue(i)));
            }
        }
        buf.append("}");

        return buf.toString();
    }

    @Override
    public String toLongString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append(" {\n");
        if (size>0 && getValues() != null) {
            VariableAssignment map = null;
            for(int i=0; i<size; i++) {
                map = getMapping(i, map);
                for (Variable v : variables) {
                    buf.append(map.get(v));
                    buf.append(" ");
                }
                buf.append("| ");
                buf.append(CostFunctionStats.formatValue(getValue(i)));
                buf.append("\n");
            }
        }
        buf.append("}");

        return buf.toString();
    }

    /** {@inheritDoc} */
    public double getValue(int[] index) {
        return getValue(subindexToIndex(index));
    }

    /** {@inheritDoc} */
    public double getValue(VariableAssignment mapping) {
        int idx = this.getIndex(mapping);
        if (idx < 0)
            return getFactory().getCombineOperation().getNeutralValue();
        return getValue(idx);
    }

    /** {@inheritDoc} */
    public Set<Variable> getVariableSet() {
        return variableSet;
    }

    /** {@inheritDoc} */
    public Set<Variable> getSharedVariables(CostFunction factor) {
        return getSharedVariables(factor.getVariableSet());
    }

    /** {@inheritDoc} */
    public Set<Variable> getSharedVariables(Variable[] variables) {
        return getSharedVariables(Arrays.asList(variables));
    }

    /** {@inheritDoc} */
    public Set<Variable> getSharedVariables(Collection variables) {
        HashSet<Variable> res = new HashSet<Variable>(variableSet);
        res.retainAll(variables);
        return res;
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
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            idx[i] = index / sizes[ii];
            index = index % sizes[ii];
        }
        return idx;
    }

    /** {@inheritDoc} */
    public CostFunction negate() {
        Combine operation = factory.getCombineOperation();
        CostFunction result = factory.buildCostFunction(this);
        Iterator<Integer> it = iterator();
        while(it.hasNext()) {
            final int i = it.next();
            result.setValue(i, operation.invert(getValue(i)));
        }
        return result;
    }

    /** {@inheritDoc} */
    public CostFunction combine(CostFunction factor) {
        Combine operation = factory.getCombineOperation();
        Summarize sum = factory.getSummarizeOperation();
        final double nogood = sum.getNoGood();

        // Combination with null factors gives a null / the other factor
        if (factor == null || factor.getSize()==0) {
            return factory.buildCostFunction(this);
        }
        if (this.getSize() == 0) {
            return factory.buildCostFunction(factor);
        }

        // Compute the variable set intersection (sets doesn't allow duplicates)
        LinkedHashSet<Variable> vars = new LinkedHashSet<Variable>(variableSet);
        vars.addAll(factor.getVariableSet());

        // Choose between sparse and dense functions
        CostFunction result;
        final double ratio1 = getNumberOfNoGoods() / (float)getSize();
        final double ratio2 = factor.getNumberOfNoGoods() / (float)factor.getSize();
        if (ratio1 > 0.8 || ratio2 > 0.8) {
            result = factory.buildSparseCostFunction(vars.toArray(new Variable[0]),
                nogood);
        } else {
            result = factory.buildCostFunction(vars.toArray(new Variable[0]),
                nogood);
        }

        VariableAssignment map = null;
        CostFunction left  = ratio1 > ratio2 ? this : factor;
        CostFunction right = left == this ? factor : this;

        Iterator<Integer> it = left.iterator();
        VariableAssignment map2 = null;
        while(it.hasNext()) {
            final int i = it.next();
            final double lv = left.getValue(i);
            if (lv == nogood) continue;

            map = left.getMapping(i, map);

            for (int fidx : right.getIndexes(map)) {
                map2 = right.getMapping(fidx, map2);
                map2.putAll(map);

                final double v = operation.eval(lv, right.getValue(fidx));
                if (Double.isNaN(v)) {
                    throw new RuntimeException("Combination generated a NaN value (" + getValue(map) + "," + factor.getValue(map) + "). Halting.");
                }
                result.setValue(result.getIndex(map2), v);
            }
        }

        /** Unoptimized implementation!
        for (int i=0; i<result.getSize(); i++) {
            map = result.getMapping(i, map);

            final double v = operation.eval(getValue(map), factor.getValue(map));
            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value (" + getValue(map) + "," + factor.getValue(map) + "). Halting.");
            }
            result.setValue(i, v);
        }*/

        return result;
    }


    /** {@inheritDoc} */
    public CostFunction combine(List<CostFunction> fs) {
        fs = new ArrayList<CostFunction>(fs);

        // Remove null functions
        for (int i=fs.size()-1; i>=0; i--) {
            if (fs.get(i) == null) fs.remove(i);
        }

        // If it's a single (or none) function, just fallback to normal combine.
        if (fs.isEmpty()) {
            return factory.buildCostFunction(this);
        } else if (fs.size() == 1) {
            return combine(fs.get(0));
        }

        Combine operation = factory.getCombineOperation();
        Summarize sum = factory.getSummarizeOperation();
        final double nogood = sum.getNoGood();

        // Compute the variable set intersection (sets doesn't allow duplicates)
        LinkedHashSet<Variable> vars = new LinkedHashSet<Variable>(variableSet);
        for (CostFunction f : fs) {
            vars.addAll(f.getVariableSet());
        }

        // Optimized implementation plan:
        //   1. Look at the nogood ratio of each function
        //   2. If any ratio > 0.5 : Result is sparse, and
        //      -> loop :
        //         - Find the combination with min number of operations
        //         - Remove items from the list, add result
        //   3. Otherwise : Result is dense, and
        //      -> use unoptimized base implementation.

        boolean sparse = false;
        fs.add(this);
        for (CostFunction f : fs) {
            final double ratio = f.getNumberOfNoGoods() / (float)f.getSize();
            if (ratio > 0.8) {
                sparse = true;
                break;
            }
        }

        if (sparse) {
            // Sort functions by sparsity
            Collections.sort(fs, new Comparator<CostFunction>() {
                // Sort in ascending sparsity order
                public int compare(CostFunction t, CostFunction t1) {
                    final float r1 = t.getNumberOfNoGoods()/(float)t.getSize();
                    final float r2 = t1.getNumberOfNoGoods()/(float)t1.getSize();
                    return r1 > r2 ? 1 : (r1 == r2 ? 0 : -1);
                }
            });

            // Perform the actual combination in decreasing sparsity order
            CostFunction result = fs.remove(fs.size()-1);
            for (int i=fs.size()-1; i>=0; i--) {
                if (result.getNumberOfNoGoods() == result.getSize()) {
                    break;
                }
                final CostFunction f = fs.remove(i);
                result = result.combine(f);
            }
            if (result.getNumberOfNoGoods() == result.getSize()) {
                return factory.buildSparseCostFunction(vars.toArray(new Variable[0]));
            }

            return result;
        }


        // Unoptimized base implementation:
        // Iterate over the result positions, fetching the values from ourselves
        // and all the other factors.
        CostFunction result = factory.buildCostFunction(vars.toArray(new Variable[0]));

        fs.remove(fs.size()-1);
        VariableAssignment map = null;
        for (int i=0; i<result.getSize(); i++) {
            map = result.getMapping(i, map);

            double v = getValue(map);
            for (CostFunction f : fs) {
                v = operation.eval(v, f.getValue(map));
                if (v == nogood) break;
            }

            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value. Halting.");
            }
            
            result.setValue(i, v);
        }

        return result;
    }

    /** {@inheritDoc} */
    public CostFunction normalize() {
        Normalize mode = factory.getNormalizationType();
        if (mode == Normalize.NONE) {
            return this;
        }

        CostFunction result = factory.buildCostFunction(this);

        // Calculate aggregation
        Iterator<Integer> it = iterator();
        double sum = 0;
        while(it.hasNext()) {
            sum += getValue(it.next());
        }
        
        final double dlen = (double)size;
        final double avg = sum / dlen;
        it = iterator();
        switch (mode) {
            case SUM0:
                while(it.hasNext()) {
                    final int i = it.next();
                    result.setValue(i, getValue(i) - avg);
                }
                break;
            case SUM1:
                // Avoid div by 0
                while(it.hasNext()) {
                    final int i = it.next();
                    final double value = getValue(i);
                    final double v = sum != 0 ? value/sum : 1/dlen;
                    result.setValue(i, v);
                }
                break;
        }

        return result;
    }

    /** {@inheritDoc} */
    public CostFunction reduce(VariableAssignment mapping) {
        if (mapping == null || mapping.isEmpty())
            return factory.buildCostFunction(this);

        // Calculate the new factor's variables
        LinkedHashSet<Variable> newVariables = new LinkedHashSet<Variable>(variableSet);
        newVariables.removeAll(mapping.keySet());

        // Does this factor reduce to a constant?
        if (newVariables.size() == 0) {
            CostFunction result = factory.buildCostFunction(new Variable[0]);
            result.setValue(0, getValue(mapping));
            return result;
        }

        // Instantiate it
        CostFunction result = this.getNumberOfNoGoods() / this.getSize() > 0.8
                ? factory.buildSparseCostFunction(newVariables.toArray(new Variable[0]))
                : factory.buildCostFunction(newVariables.toArray(new Variable[0]));
        VariableAssignment map = null;
        for (int i = 0, len = result.getSize(); i < len; i++) {
            map = result.getMapping(i, map);
            map.putAll(mapping);
            final int idx = getIndex(map);
            result.setValue(i, getValue(idx));
        }
        
        return result;
    }

    /** {@inheritDoc} */
    public CostFunction filter(CostFunction f, double bound) {
        CostFunction result = factory.buildCostFunction(this);
        Summarize operation = factory.getSummarizeOperation();

        // Compute the total tuple values after combining
        CostFunction combi = this.combine(f);
        combi = combi.summarize(this.variables);

        // Perform the actual filtering
        //Iterator<Integer> it = result.iterator();
        boolean allNogoods = true;
        for (int i = 0, len = result.getSize(); i < len; i++) {
            if (combi.getValue(i) != operation.getNoGood()) {
                allNogoods = false;
            }
            if (operation.isBetter(bound, combi.getValue(i))) {
                result.setValue(i, operation.getNoGood());
            }
        }

        if (allNogoods) {
            return factory.buildCostFunction(new Variable[0], operation.getNoGood());
        }

        return result;
    }

    @Override
    public CostFunction filter(List<CostFunction> infs, double bound) {
        CostFunction result = factory.buildCostFunction(this);
        Summarize sum = factory.getSummarizeOperation();
        Combine   com = factory.getCombineOperation();
        final double ng = sum.getNoGood();
        ArrayList<CostFunction> fs = new ArrayList<CostFunction>(infs.size());

        // Reduce to shared variables (lower memory usage)
        for (CostFunction f : infs) {
            Set<Variable> sv = this.getSharedVariables(f);
            if (sv.size() != f.getVariableSet().size()) {
                fs.add(f.summarize(sv.toArray(new Variable[0])));
            } else {
                fs.add(f);
            }
        }

        // Perform the actual filtering (only on "good" tuples)
        boolean allNogoods = true; VariableAssignment map = null;
        Iterator<Integer> it = iterator();
        while(it.hasNext()) {
            final int i = it.next();
            map = getMapping(i, map);
            double v = getValue(i);

            for (CostFunction f : fs) {
                v = com.eval(v, f.getValue(map));
            }
            
            if (sum.isBetter(bound, v)) {
                result.setValue(i, sum.getNoGood());
            } else {
                allNogoods = false;
            }
        }

        if (allNogoods) {
            return factory.buildCostFunction(new Variable[0], ng);
        }

        return result;
    }

    /** {@inheritDoc} */
    public void setValue(int[] index, double value) {
        setValue(subindexToIndex(index), value);
    }

    /**
     * Converts a vector of indices (one for each variable) to the corresponding
     * linearized index of the whole function.
     *
     * @param subindex vector of variable configurations (indices).
     * @return corresponding linearized index.
     */
    protected int subindexToIndex(int[] subindex) {
        // Check index lengths
        if (subindex.length != sizes.length) {
            throw new IllegalArgumentException("Invalid index specification");
        }
        // Compute subindex -> index offset
        int idx = 0;
        for (int i = 0; i < subindex.length; i++) {
            // Check domain limits
            if (variables[i].getDomain() <= subindex[i]) {
                throw new IllegalArgumentException("Invalid index for dimension " + i);
            }
            idx += sizes[subindex.length - i - 1] * subindex[i];
        }
        return idx;
    }

    /** {@inheritDoc} */
    public CostFunction summarize(Variable[] vars) {
        Summarize operation = factory.getSummarizeOperation();

        // Choose between sparse and dense functions
        CostFunction result;
        final double ratio1 = getNumberOfNoGoods() / (float)getSize();
        if (ratio1 > 0.5) {
            result = factory.buildSparseCostFunction(vars,
                operation.getNoGood());
        } else {
            result = factory.buildCostFunction(vars,
                operation.getNoGood());
        }

        VariableAssignment map = null;
        Iterator<Integer> it = iterator();
        while (it.hasNext()) {
            final int i = it.next();
            map = getMapping(i, map);
            // This value is lost during the summarization
            for (int idx : result.getIndexes(map)) {
                result.setValue(idx, operation.eval(getValue(i), result.getValue(idx)));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    public int getNumberOfZeros() {
        int zeros = 0;
        for(Iterator<Integer> it = iterator(); it.hasNext();){
            if (getValue(it.next()) == 0) {
                zeros++;
            }
        }
        return zeros;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CostFunction)) {
            return false;
        }
        final CostFunction other = (CostFunction) obj;

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
    public boolean equals(CostFunction other, double delta) {

        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (this.variableSet != other.getVariableSet() &&
                (this.variableSet == null ||
                !this.variableSet.equals(other.getVariableSet())))
        {
            return false;
        }

        // Constant cost function handling
        if (variableSet.size() == 0) {
            final double e = getValue(0) - other.getValue(0);
            return Math.abs(e) <= delta;
        }

        VariableAssignment map = null;
        for (int i=0; i<size; i++) {
            map = this.getMapping(i, map);
            final double v1 = getValue(i);
            final double v2 = other.getValue(map);
            if (Double.isNaN(v1) ^ Double.isNaN(v2)) {
                return false;
            }
            final double e = getValue(i) - other.getValue(map);
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

}
