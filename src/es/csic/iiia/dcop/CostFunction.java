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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public interface CostFunction {

    /**
     * Available combination modes.
     */
    public enum Combine {
        /**
         * Perform the combine operator using product.
         */
        PRODUCT {
            public double getNeutralValue() {return 1;}
            public double eval(double x, double y) {return x*y;}
            public double invert(double x) {return 1/x;}
        },
        /**
         * Perform the combine operator using addition.
         */
        SUM {
            public double getNeutralValue() {return 0;}
            public double eval(double x, double y) {return x+y;}
            public double invert(double x) {return -x;}
        };

        /**
         * Performs the combination of the given values according to the
         * current combination mode.
         *
         * @param x first value.
         * @param y second value.
         * @return combination result.
         */
        public abstract double eval(double x, double y);

        /**
         * Returns the neutral value of the combination mode.
         *
         * This is, it returns <em>0</em> when SUM-combining, or <em>1</em>
         * when PRODUCT-combining.
         *
         * @return combination <em>neutral</em> value.
         */
        public abstract double getNeutralValue();

        /**
         * Returns the inverse of the given value.
         *
         * Given {@link #eval(x, y)} and {@link #getNeutralValue()}, this function
         * returns the value such that:
         *
         * <code>
         * eval(x, inverse(x)) == getNeutralValue()
         * </code>
         *
         * @param x value.
         * @return the inverse of the given value.
         */
        public abstract double invert(double x);
    }

    /**
     * Available normalization modes.
     */
    public enum Normalize {
        /**
         * Do not normalize.
         */
        NONE,
        /**
         * Normalize so that all values add to 0.
         */
        SUM0,
        /**
         * Normalize so that all values add to 1.
         */
        SUM1;
    }

    /**
     * Available summarization modes.
     */
    public enum Summarize {
        /**
         * Perform the summzarize operator using maximum.
         */
        MAX {
            public double eval(double x, double y) {return Math.max(x, y);}
            public boolean isBetter(double x, double y) {return x > y;}
            public double getNoGood() {
                return Double.NEGATIVE_INFINITY;
            }
        },
        /**
         * Perform the summzarize operator using minimum.
         */
        MIN {
            public double eval(double x, double y) {return Math.min(x, y);}
            public boolean isBetter(double x, double y) {return x < y;}
            public double getNoGood() {
                return Double.POSITIVE_INFINITY;
            }
        },
        /**
         * Perform the summarize operator using addition.
         */
        SUM {
            public double eval(double x, double y) {return x+y;}
            public boolean isBetter(double x, double y) {
                throw new RuntimeException("I don't know how to compare when using SUM summarization.");
            }
            public double getNoGood() {
                return 0;
            }
        };

        /**
         * Performs the summarization of the given values according to the
         * current summarization mode.
         *
         * @param x first value.
         * @param y second value.
         * @return summarization result.
         */
        public abstract double eval(double x, double y);
        
        /**
         * Returns <em>true</em> if x is better than y according to the
         * summarization mode being used.
         * 
         * @param x first value.
         * @param y second value.
         * @return true if x is better than y or <em>false</em> otherwise.
         */
        public abstract boolean isBetter(double x, double y);

        /**
         * Returns the value corresponding to a <em>nogood</em> (infitely bad
         * value) according to the summarization mode being used.
         *
         * @return <em>nogood</em> (worst) value.
         */
        public abstract double getNoGood();
    }

    /**
     * Sets the initial cost/utility of all the factor configurations to the
     * given initial value.
     *
     * @param initialValue
     */
    public void initialize(Double initialValue);

    /**
     * Combine this factor with the given one, using the specified operation.
     *
     * @param factor factor to combine with.
     * @return a new CostFunction which is the result of the combination between this
     * and the given one.
     */
    CostFunction combine(CostFunction factor);

    /**
     * {@inheritDoc}
     */
    @Override boolean equals(Object obj);

    /**
     * Returns the optimal assignment for this factor.
     *
     * If an existing mapping is passed, the new assignments will be appended
     * to it. <strong>Warning:</strong> this function does not take into
     * account these pre-assigned variables to do the calculation.
     *
     * For example, if this factor is F(x,y){0,10,2,6}, summarization is set to
     * MIN and it receives {x:1,z:1} through the mapping, it will return
     * {x:0,z:1,y:0} as the new mapping. If you want to ensure an assignment
     * consistent with an existing mapping, use
     * {@link #reduce(java.util.Hashtable)} first.
     *
     * @param mapping current variable mappings.
     */
    VariableAssignment getOptimalConfiguration(VariableAssignment mapping);

    /**
     * Returns <strong>the first</strong> index of the values array
     * corresponding to the specified variables mapping.
     *
     * @param mapping variable/value mapping table.
     * @return index of the values array corresponding to the given mapping.
     */
    int getIndex(VariableAssignment mapping);

    /**
     * Get all the linearized indices corresponding to the given variable mapping.
     *
     * @param mapping of the desired configuration.
     * @return corresponding linearized index(es).
     */
    public List<Integer> getIndexes(VariableAssignment mapping);

    /**
     * Returns the variable/value mapping corresponding to the specified
     * index of the values array.
     *
     * @param index of the values array.
     * @param mapping mapping table to fill, instantiated if null.
     * @return variable/value mapping corresponding to the given index.
     */
    VariableAssignment getMapping(int index, VariableAssignment mapping);

    /**
     * Get a short string representation of this function.
     *
     * For debugging purposes, it includes the function's variables but not
     * the hypercube's values.
     *
     * @return name.
     */
    String getName();

    /**
     * Get the long string representation of this function, as hypercube
     * table.
     *
     * For debugging purposes, it includes the function's variables but not
     * the hypercube's values.
     *
     * @return name.
     */
    String toLongString();

    /**
     * Get the function's size (in number of possible configurations).
     * @return number of function's possible configurations.
     */
    int getSize();

    /**
     * Gets the value of this factor for the given variable states.
     *
     * @param index list of variable states.
     * @return value corresponding factor value.
     */
    double getValue(int[] index);

    /**
     * Gets the value of this factor for the given linearized index.
     *
     * @param index of the state.
     * @return value corresponding factor value.
     */
    double getValue(int index);

    /**
     * Gets the value of this factor for the given variable/value mapping.
     *
     * @param mapping variable/value mapping.
     * @return value corresponding factor value.
     */
    double getValue(VariableAssignment mapping);

    double[] getValues();

    /**
     * Gets the set of variables of this factor.
     *
     * @return set of variables of this factor.
     */
    Set<Variable> getVariableSet();

    /**
     * Gets the set of variables shared with the given factor.
     *
     * @parameter factor to compare against.
     * @return set of variables shared with the given factor.
     */
    Set<Variable> getSharedVariables(CostFunction factor);

    /**
     * Gets the set of variables shared with the given variable collection.
     *
     * @parameter variable collection to compare against.
     * @return set of variables shared with the given variable collection.
     */
    Set<Variable> getSharedVariables(Collection variables);

    /**
     * Gets the set of variables shared with the array of variables.
     *
     * @parameter array of variables to compare against.
     * @return set of variables shared with the given array of variables.
     */
    Set<Variable> getSharedVariables(Variable[] variables);

    /**
     * Negates this factor, applying the inverse of the given operation to
     * all it's values.
     *
     * @see #combine(es.csic.iiia.iea.ddm.CostFunction, int)
     */
    void negate();

    /**
     * Normalizes this factor in the specified mode.
     */
    void normalize();

    /**
     * Reduces the factor, fixing the variable-value pairs of the mapping
     * table.
     *
     * @param mapping variable-value pairs to fix.
     * @return new reduced factor.
     */
    CostFunction reduce(VariableAssignment mapping);

    /**
     * Sets the value of this factor for the given variable states.
     *
     * @param index list of variable states.
     * @param value corresponding factor value.
     */
    void setValue(int[] index, double value);

    /**
     * Sets the value of this factor for the given linealized variable states.
     *
     * @param index linealized variable states.
     * @param value value for this serialized state.
     */
    void setValue(int index, double value);

    /**
     * Sets the complete list of values for all possible variable states.
     *
     * @param values list of values for all possible variable states.
     */
    void setValues(double[] values);

    /**
     * Summarize this factor over the specified variables, using the given
     * operation.
     *
     * @param vars variables to summarize.
     * @param operation operation to use.
     * @return a new CostFunction which is the result of summarizing this one over
     * the specified variables.
     */
    CostFunction summarize(Variable[] vars);

    /**
     * Obtains an iterator over the linearized indices of non-infinity elements of this
     * cost function.
     *
     * @return Iterator over the indices of this cost function.
     */
    public Iterator<Integer> iterator();

    public void setFactory(CostFunctionFactory factory);

    public CostFunctionFactory getFactory();
    
}
