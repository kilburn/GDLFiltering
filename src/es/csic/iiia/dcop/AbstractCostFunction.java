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
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Base implementation of a cost function.
 *
 * This class provides some basic methods implementing the operations that can
 * be made over cost functions, while delegating the actual cost/utility values
 * representation/storage to the concrete class that extends lit.
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
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
    protected long size;

    /**
     * List of aggregated dimensionality up to "index".
     */
    protected long[] sizes;

    /**
     * The factory that generated this CostFunction.
     */
    private CostFunctionFactory factory;
    
    /**
     * Comparator to order functions by sparsity
     */
    private static Comparator<CostFunction> sparseComparator = new Comparator<CostFunction>() {
        // Sort in ascending sparsity order
        public int compare(CostFunction t, CostFunction t1) {
            final float r1 = t.getNumberOfNoGoods()/(float)t.getSize();
            final float r2 = t1.getNumberOfNoGoods()/(float)t1.getSize();
            return r1 < r2 ? -1 : (r1 == r2 ? 0 : 1);
        }
    };

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

    public void initialize(Double initialValue) {
        for (long i=0; i<size; i++) {
            setValue(i, initialValue);
        }
    }

    public void setFactory(CostFunctionFactory factory) {
        this.factory = factory;
    }

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
        sizes = new long[len];
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

    public VariableAssignment getOptimalConfiguration(VariableAssignment mapping) {
        if (mapping == null) {
            mapping = new VariableAssignment(variables.length);
        }

        // Empty cost functions have no optimal value
        if (variables.length == 0) {
            return mapping;
        }

        long i = getOptimalConfiguration();
        if (i < 0) {i = nextRandomLong(size);}
        mapping.putAll(getMapping(i, null));
        return mapping;
    }

    private static long nextRandomLong(long n) {
        Random random = new Random();


        if (n <= 0) {
            throw new IllegalArgumentException(
                "Upper bound for nextInt must be positive"
            );
        }
        // Code adapted from Harmony Random#nextInt(int)
        if ((n & -n) == n) { // n is power of 2
            // dropping lower order bits improves behaviour for low values of n
            return (random.nextLong() & 0x7fffffffffffffffL) >> 63 // drop all the bits
                - bitsRequired(n-1); // except the ones we need
        }
        // Not a power of two
        long val;
        long bits;
        do { // reject some values to improve distribution
            bits = random.nextLong() & 0x7fffffffffffffffL;
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    private static int bitsRequired(long num){
        // Derived from Hacker's Delight, Figure 5-9
        long y=num; // for checking right bits
        int n=0; // number of leading zeros found
        while(true){
            // 64 = number of bits in a long
            if (num < 0) {
                return 64-n; // no leading zeroes left
            }
            if (y == 0) {
                return n; // no bits left to check
            }
            n++;
            num=num << 1; // check leading bits
            y=y >> 1; // check trailing bits
        }
    }

    public long getOptimalConfiguration() {
        // Find the maximal value
        Summarize operation = factory.getSummarizeOperation();
        ArrayList<Long> idx = new ArrayList<Long>();
        double optimal = operation.getNoGood();
        TLongIterator it = iterator();
        while(it.hasNext()) {
            final long i = it.next();
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
    public long getIndex(VariableAssignment mapping) {
        final int len = variables.length;
        if (len == 0) {
            // This can be an empty or a constant factor
            return size == 0 ? -1 : 0;
        }

        long idx = 0;
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
    @Override
    public TLongList getIndexes(VariableAssignment mapping) {
        TLongList idxs = new TLongArrayList();

        final int len = variables.length;
        if (len == 0) {
            if (size > 0) {
                idxs.add(0L);
            }
            return idxs;
        }
        idxs.add(0L);
        
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
    public VariableAssignment getMapping(long index, VariableAssignment mapping) {
        if (mapping == null) {
            mapping = new VariableAssignment(variables.length);
        } else {
            mapping.clear();
        }

        final int len = variables.length;
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            mapping.put(variables[i], (int)(index / sizes[ii]));
            index = index % sizes[ii];
        }
        return mapping;
    }

    /**
     * Get the function's size (in number of possible configurations).
     * @return number of function's possible configurations.
     */
    @Override public long getSize() {
        return size;
    }

    /**
     * Get the functions's aggregated dimesionalities vector.
     * @return function's aggregated dimensionalities vector.
     */
    protected long[] getSizes() {
        return sizes;
    }

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
            for(long i=1; i<size; i++) {
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
            for(long i=0; i<size; i++) {
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

    public double getValue(int[] index) {
        return getValue(subindexToIndex(index));
    }

    public double getValue(VariableAssignment mapping) {
        long idx = this.getIndex(mapping);
        if (idx < 0)
            return getFactory().getCombineOperation().getNeutralValue();
        return getValue(idx);
    }

    public Set<Variable> getVariableSet() {
        return variableSet;
    }

    public Set<Variable> getSharedVariables(CostFunction factor) {
        return getSharedVariables(factor.getVariableSet());
    }

    public Set<Variable> getSharedVariables(Variable[] variables) {
        return getSharedVariables(Arrays.asList(variables));
    }

    public Set<Variable> getSharedVariables(Collection variables) {
        HashSet<Variable> res = new HashSet<Variable>(variableSet);
        res.retainAll(variables);
        return res;
    }

    /**
     * Returns the subindices list (ordered list of values for each variable of
     * this factor) corresponding to the given values array index.
     *
     * @param index values array index.
     * @return subindices list.
     */
    protected int[] indexToSubindex(long index) {
        int[] idx = new int[variables.length];
        final int len = variables.length;
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            idx[i] = (int)(index / sizes[ii]);
            index = index % sizes[ii];
        }
        return idx;
    }
    
    /**
     * Fills in the subindices list (ordered list of values for each variable of
     * this factor) corresponding to the given values array index.
     *
     * @param index values array index.
     * @param idx subindices list to fill.
     */
    protected void indexToSubindex(long index, int[] idx) {
        final int len = variables.length;
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            idx[i] = (int)(index / sizes[ii]);
            index = index % sizes[ii];
        }
    }

    public CostFunction negate() {
        Combine operation = factory.getCombineOperation();
        CostFunction result = factory.buildCostFunction(this);
        TLongIterator it = iterator();
        while(it.hasNext()) {
            final long i = it.next();
            final double v = operation.negate(getValue(i));
            if (Double.isNaN(v)) {
                throw new RuntimeException("Negation generated a NaN value. Halting.");
            }
            result.setValue(i, v);
        }
        return result;
    }
    
    public CostFunction invert() {
        Combine operation = factory.getCombineOperation();
        CostFunction result = factory.buildCostFunction(this);
        TLongIterator it = iterator();
        while(it.hasNext()) {
            final long i = it.next();
            result.setValue(i, operation.invert(getValue(i)));
        }
        return result;
    }

    public CostFunction combine(CostFunction factor) {
        final double ng = factory.getSummarizeOperation().getNoGood();

        // Combination with null factors gives a null / the other factor
        if (factor == null || factor.getSize()==0) {
            return factory.buildCostFunction(this);
        }
        if (this.getSize() == 0) {
            return factory.buildCostFunction(factor);
        }

        // Compute the variable set intersection (sets doesn't allow duplicates)
        LinkedHashSet<Variable> varSet = new LinkedHashSet<Variable>(variableSet);
        varSet.addAll(factor.getVariableSet());
        Variable[] vars = varSet.toArray(new Variable[0]);

        // Choose between sparse and dense functions
        CostFunction result;
        final double ratio1 = getNumberOfNoGoods() / (float)getSize();
        final double ratio2 = factor.getNumberOfNoGoods() / (float)factor.getSize();
        if (ratio1 > 0.8 || ratio2 > 0.8) {
            // Sparse functions pay off
            CostFunction left  = ratio1 > ratio2 ? this : factor;
            CostFunction right = left == this ? factor : this;
            
            result = factory.buildSparseCostFunction(vars, ng);
            left = left.summarize(vars);
            sparseCombine(left, right, result);
        } else {
            // It is better to use dense functions
            result = factory.buildCostFunction(vars, 1);
            if (result instanceof MapCostFunction) {
                result = factory.buildSparseCostFunction(vars, ng);
                factor = factor.summarize(vars);
                sparseCombine(factor, this, result);
            } else {
                denseCombine(this, factor, result);
            }
        }

        return result;
    }
    
    private void sparseCombine(CostFunction left, CostFunction right,
            CostFunction result)
    {
        final Combine operation = factory.getCombineOperation();
        
        MasterIterator it = left.masterIterator();
        final int[] subidxs = it.getIndices();
        ConditionedIterator rit = right.conditionedIterator(left);
        while(it.hasNext()) {
            final long i = it.next();
            final double lv = left.getValue(i);
            final double rv = right.getValue(rit.nextSubidxs(subidxs));
            final double v = operation.eval(lv, rv);
            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value (" + lv + "," + rv + "). Halting.");
            }
            result.setValue(i, v);
        }
    }
    
    private void sparseCombine(CostFunction left, List<CostFunction> right,
            CostFunction result)
    {
        final Combine operation = factory.getCombineOperation();
        final double ng = factory.getSummarizeOperation().getNoGood();
        final int rsize = right.size();
        
        MasterIterator it = left.masterIterator();
        final int[] subidxs = it.getIndices();
        ConditionedIterator[] rit = new ConditionedIterator[right.size()];
        for (int i=0; i<rsize; i++) {
                rit[i] = right.get(i).conditionedIterator(left);
        }
        while(it.hasNext()) {
            final long idx = it.next();
            double v = left.getValue(idx);
            for (int i=0; i<rsize; i++) {
                final double rv = right.get(i).getValue(rit[i].nextSubidxs(subidxs));
                v = operation.eval(v, rv);
                if (v == ng) {
                    break;
                }
            }
            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value. Halting.");
            }
            result.setValue(idx, v);
        }
    }
    
    private void denseCombine(CostFunction f1, CostFunction f2,
            CostFunction result)
    {   
        final Combine operation = factory.getCombineOperation();
        MasterIterator       it = result.masterIterator();
        ConditionedIterator  i1 = f1.conditionedIterator(result);
        ConditionedIterator  i2 = f2.conditionedIterator(result);
        final int[] subidx      = it.getIndices();
        while (it.hasNext()) {
            final long i = it.next();
            final double v1 = f1.getValue(i1.nextSubidxs(subidx));
            final double v2 = f2.getValue(i2.nextSubidxs(subidx));
            final double v = operation.eval(v1, v2);
            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value (" + v1 + "," + v2 + "). Halting.");
            }
            result.setValue(i, v);
        }
    }
    
    @Override
    public ConditionedIterator conditionedIterator(CostFunction f) {
        return new DefaultConditionedIterator(f);
    }


    public CostFunction combine(List<CostFunction> fs) {
        fs = new ArrayList<CostFunction>(fs);

        // Remove null functions
        for (int i=fs.size()-1; i>=0; i--) {
            if (fs.get(i) == null) fs.remove(i);
        }

        // If lit's a single (or none) function, just fallback to normal combine.
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
        //   2. If any ratio > 0.8 : Result is sparse, and
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
            Collections.sort(fs, sparseComparator);
            // @TODO : Check if the order is right!!
            Variable[] vs = vars.toArray(new Variable[0]);
            CostFunction left = fs.remove(fs.size()-1).summarize(vs);
            CostFunction result = factory.buildSparseCostFunction(vs, nogood);
            sparseCombine(left, fs, result);
            return result;
        }


        // Unoptimized base implementation:
        // Iterate over the result positions, fetching the values from ourselves
        // and all the other factors.
        CostFunction result = factory.buildCostFunction(vars.toArray(new Variable[0]), operation.getNeutralValue());
        final int niterators = fs.size();
        ConditionedIterator[] iterators = new ConditionedIterator[niterators];
        for (int i=0; i<niterators; i++) {
            iterators[i] = fs.get(i).conditionedIterator(result);
        }
        
        MasterIterator it = result.masterIterator();
        final int[] subidx = it.getIndices();
        while (it.hasNext()) {
            final long idx = it.next();
            double v = fs.get(0).getValue(iterators[0].nextSubidxs(subidx));
            for (int i=1; i<niterators; i++) {
                final long idx2 = iterators[i].nextSubidxs(subidx);
                v = operation.eval(v, fs.get(i).getValue(idx2));
            }

            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value. Halting.");
            }
            
            result.setValue(idx, v);
        }

        return result;
    }

    public CostFunction normalize() {
        Normalize mode = factory.getNormalizationType();
        if (mode == Normalize.NONE) {
            return this;
        }

        CostFunction result = factory.buildCostFunction(this);

        // Calculate aggregation
        TLongIterator it = iterator();
        double sum = 0;
        while(it.hasNext()) {
            sum += getValue(it.next());
        }
        
        //@TODO: This is noooot so clear.
        final double dlen = (double)(size - getNumberOfNoGoods());
        final double avg = sum / dlen;
        if (Double.isNaN(avg)) {
            throw new RuntimeException("Normalization generated a NaN value. Halting.");
        }
        it = iterator();
        switch (mode) {
            case SUM0:
                while(it.hasNext()) {
                    final long i = it.next();
                    double v = getValue(i) - avg;
                    if (Double.isNaN(v)) {
                        throw new RuntimeException("Normalization generated a NaN value. Halting.");
                    }
                    result.setValue(i, v);
                }
                break;
            case SUM1:
                // Avoid div by 0
                while(it.hasNext()) {
                    final long i = it.next();
                    final double value = getValue(i);
                    final double v = sum != 0 ? value/sum : 1/dlen;
                    result.setValue(i, v);
                }
                break;
        }

        return result;
    }

    public CostFunction reduce(VariableAssignment mapping) {
        if (mapping == null || mapping.isEmpty())
            return factory.buildCostFunction(this);

        // Calculate the new factor's variables
        LinkedHashSet<Variable> newVariables = new LinkedHashSet<Variable>(variableSet);
        newVariables.removeAll(mapping.keySet());

        // Does this factor reduce to a constant?
        if (newVariables.size() == 0) {
            CostFunction result = factory.buildCostFunction(new Variable[0], getValue(mapping));
            return result;
        }

        // Instantiate lit
        float sparsity = this.getNumberOfNoGoods() / (float)this.getSize();
        if (sparsity > 0.8) {
            CostFunction result = factory.buildSparseCostFunction(
                    newVariables.toArray(new Variable[0]), factory.getSummarizeOperation().getNoGood());
            return sparseReduce(result, mapping);
        }

        CostFunction result = factory.buildCostFunction(newVariables.toArray(new Variable[0]), 0);
        VariableAssignment map = null;
        for (long i = 0, len = result.getSize(); i < len; i++) {
            map = result.getMapping(i, map);
            map.putAll(mapping);
            final long idx = getIndex(map);
            result.setValue(i, getValue(idx));
        }

        return result;
    }

    private CostFunction sparseReduce(CostFunction result, VariableAssignment mapping) {

        // Intersect mappings with the variables of this factor
        Set<Variable> vs = getSharedVariables(mapping.keySet());
        TLongIterator it = iterator();
        VariableAssignment map = null;
        while (it.hasNext()) {
            final long i = it.next();
            map = getMapping(i, map);

            boolean ok = true;
            for (Variable v : vs) {
                if (mapping.get(v) != map.get(v)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            result.setValue(result.getIndex(map), getValue(i));
        }

        return result;
    }

    public CostFunction filter(CostFunction f, double bound) {
        CostFunction result = factory.buildCostFunction(this);
        Summarize operation = factory.getSummarizeOperation();

        // Compute the total tuple values after combining
        CostFunction combi = this.combine(f);
        combi = combi.summarize(this.variables);

        // Perform the actual filtering
        boolean allNogoods = true;
        TLongIterator it = iterator();
        while(it.hasNext()) {
            final long i = it.next();
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
        final int nfs = fs.size();
        ConditionedIterator[] iterators = new ConditionedIterator[nfs];
        for (int i=0; i<nfs; i++) {
            iterators[i] = fs.get(i).conditionedIterator(this);
        }
        
        MasterIterator it = masterIterator();
        final int[] subidxs = it.getIndices();
        while(it.hasNext()) {
            final long idx = it.next();
            
            double v = getValue(idx);
            for (int i=0; i<nfs; i++) {
                final long idx2 = iterators[i].nextSubidxs(subidxs);
                v = com.eval(v, fs.get(i).getValue(idx2));
                if (sum.isBetter(bound, v)) break;
            }
            
            
            if (sum.isBetter(bound, v)) {
                result.setValue(idx, ng);
            } else {
                allNogoods = false;
            }
        }

        if (allNogoods) {
            return factory.buildCostFunction(new Variable[0], ng);
        }

        return result;
    }

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
                throw new IllegalArgumentException("Invalid index " + subindex[i] + " for dimension " + i);
            }
            idx += sizes[subindex.length - i - 1] * subindex[i];
        }
        return idx;
    }

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

        MasterIterator it = masterIterator();
        final int[] subidxs = it.getIndices();
        ConditionedIterator rit = result.conditionedIterator(this);
        while (it.hasNext()) {
            final long i = it.next();
            
            // This value is lost during the summarization
            rit.nextSubidxs(subidxs);
            while (rit.hasNextOffset()) {
                final long idx = rit.nextOffset();
                result.setValue(idx, operation.eval(getValue(i), result.getValue(idx)));
            }
        }
        return result;
    }

    @Override
    public long getNumberOfZeros() {
        int zeros = 0;
        for(TLongIterator it = iterator(); it.hasNext();){
            if (getValue(it.next()) == 0) {
                zeros++;
            }
        }
        return zeros;
    }

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
        for (long i=0; i<size; i++) {
            map = this.getMapping(i, map);
            final double v1 = getValue(i);
            final double v2 = other.getValue(map);
            if (Double.isNaN(v1) || Double.isNaN(v2)) {
                return false;
            }
            final double e = getValue(i) - other.getValue(map);
            if (Math.abs(e) > delta) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.variableSet != null ? this.variableSet.hashCode() : 0);
        return hash;
    }

    /**
     * Default implementation of conditioned iterator.
     */
    public class DefaultConditionedIterator implements ConditionedIterator {
        private int[] referenceIdxs;
        private int[] idxsToReference;
        private int len = variables.length;
        private AbstractCostFunction master;
        private int noffsets;
        private long[] offsets;
        private long idx;
        private int currentOffset;
        
        /**
         * Builds a conditioned iterator.
         * @param reference conditioner cost function whose natural order should
         * be followed.
         */
        public DefaultConditionedIterator(CostFunction reference) {
            if (!(reference instanceof AbstractCostFunction)) {
                throw new RuntimeException("Unable to build custom iterator for arbitrary CostFunction subtypes");
            }
            AbstractCostFunction other = (AbstractCostFunction)reference;
            master = other;
                        
            referenceIdxs = new int[other.variables.length];
            idxsToReference = new int[len];

//            int idx = 0;
//            for (Variable v1 : vs)  {
//                for (int j=0; j<len; j++) {
//                    final Variable v2 = variables[j];
//                    if (v1.equals(v2)) {
//                        idxsToReference[j] = idx;
//                        break;
//                    }
//                }
//                idx++;
//            }
            
            ArrayList<Integer> freeVars = new ArrayList<Integer>(len);
            Arrays.fill(idxsToReference, -1);
            for (int j=0; j<len; j++) {
                boolean found = false;
                
                for (int i=0, olen=other.variables.length; i<olen; i++) {    
                    if (variables[j].equals(other.variables[i])) {
                        idxsToReference[j] = i;
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    freeVars.add(j);
                }
            }
            
            // Ofsset computation (a single parent index maps to multiple
            // indices of this function)
            if  (!freeVars.isEmpty()) {
                
                // Compute the number of offsets
                noffsets = 1;
                for (int i : freeVars) {
                    noffsets *= variables[i].getDomain();
                    if (noffsets < 0) {
                        throw new RuntimeException("Offset index overflow.");
                    }
                }
                
                // Compute the actual offsets
                offsets = new long[noffsets];
                long multiplier = 1;
                for (int i : freeVars) {
                    final int domain = variables[i].getDomain();
                    int oidx = 0;
                    while (oidx < noffsets) {
                        for (int j=0; j<domain; j++) {
                            for (int k=0; k<multiplier; k++) {
                                offsets[oidx++] += sizes[len - i - 1]*j;
                            }
                        }
                    }
                    multiplier *= domain;
                }
                
            } else {
                noffsets = 1;
                offsets = new long[]{0l};
            }
            
            Arrays.fill(referenceIdxs, 0);
        }

        public long next(long referenceIdx) {
            master.indexToSubindex(referenceIdx, referenceIdxs);
            
            // Compute subindex -> index
            idx = 0;
            for (int i = 0; i < len; i++) {
                final int referenceIdxi = idxsToReference[i];
                if (referenceIdxi >= 0) {
                    final int idxv = referenceIdxs[referenceIdxi];
                    idx += sizes[len - i - 1] * idxv;
                }
            }
            
            currentOffset = 0;
            return idx;
        }
        
        public long nextSubidxs(int[] referenceIdxs) {
            //master.indexToSubindex(referenceIdx, referenceIdxs);
            
            // Compute subindex -> index
            idx = 0;
            for (int i = 0; i < len; i++) {
                final int referenceIdxi = idxsToReference[i];
                if (referenceIdxi >= 0) {
                    final int idxv = referenceIdxs[referenceIdxi];
                    idx += sizes[len - i - 1] * idxv;
                }
            }
            
            currentOffset = 0;
            return idx;
        }
        
        public boolean hasNextOffset() {
            return currentOffset < noffsets;
        }
        
        public long nextOffset() {
            return idx + offsets[currentOffset++];
        }

    }
}
