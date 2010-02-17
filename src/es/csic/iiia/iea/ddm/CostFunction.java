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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public interface CostFunction {
    /**
     * Perform the combine operator using product.
     */
    int COMBINE_PRODUCT = 1;
    /**
     * Perform the combine operator using addition.
     */
    int COMBINE_SUM = 0;
    /**
     * Do not normalize.
     */
    int NORMALIZE_NONE = 2;
    /**
     * Normalize so that all values add to 0.
     */
    int NORMALIZE_SUM0 = 0;
    /**
     * Normalize so that all values add to 1.
     */
    int NORMALIZE_SUM1 = 1;
    /**
     * Perform the summzarize operator using maximum.
     */
    int SUMMARIZE_MAX = 1;
    /**
     * Perform the summzarize operator using minimum.
     */
    int SUMMARIZE_MIN = 2;
    /**
     * Perform the summarize operator using addition.
     */
    int SUMMARIZE_SUM = 0;

    /**
     * Combine this factor with the given one, using the specified operation.
     *
     * @param factor factor to combine with.
     * @param operation operation to use.
     * @return a new CostFunction which is the result of the combination between this
     * and the given one.
     */
    CostFunction combine(CostFunction factor, int operation);

    /**
     * {@inheritDoc}
     */
    @Override
    boolean equals(Object obj);

    /**
     * Instantiates the variables of this factor into the given mapping such
     * that it is an "optimal" configuration.
     *
     * @param mapping current variable mappings.
     * @param operation summarizing operation used.
     */
    void getBestConfiguration(Hashtable<Variable, Integer> mapping, int operation);

    /**
     * Returns the index of the values array corresponding to the specified
     * variables mapping.
     *
     * @param mapping variable/value mapping table.
     * @return index of the values array corresponding to the given mapping.
     */
    int getIndex(Hashtable<Variable, Integer> mapping);

    /**
     * Returns the variable/value mapping corresponding to the specified
     * index of the values array.
     *
     * @param index of the values array.
     * @param mapping mapping table to fill, instantiated if null.
     * @return variable/value mapping corresponding to the given index.
     */
    Hashtable<Variable, Integer> getMapping(int index, Hashtable<Variable, Integer> mapping);

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
     * Gets the value of this factor for the given variable/value mapping.
     *
     * @param mapping variable/value mapping.
     * @return value corresponding factor value.
     */
    double getValue(Hashtable<Variable, Integer> mapping);

    double[] getValues();

    /**
     * Gets the set of variables of this factor.
     *
     * @return set of variables of this factor.
     */
    Set<Variable> getVariableSet();

    /**
     * Negates this factor, applying the inverse of the given operation to
     * all it's values.
     *
     * @see #combine(es.csic.iiia.iea.ddm.CostFunction, int)
     * @param operation operation to use.
     */
    void negate(int operation);

    /**
     * Normalizes this factor in the specified mode.
     *
     * @param mode Normalization mode to use.
     */
    void normalize(int mode);

    /**
     * Reduces the factor, fixing the variable-value pairs of the mapping
     * table.
     *
     * @param mapping variable-value pairs to fix.
     * @return new reduced factor.
     */
    CostFunction reduce(Hashtable<Variable, Integer> mapping);

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
    CostFunction summarize(Variable[] vars, int operation);

    /**
     * Obtains an iterator over the linearized indices of non-infinity elements of this
     * cost function.
     *
     * @return Iterator over the indices of this cost function.
     */
    public abstract Iterator<Integer> iterator();

    /**
     * Utility to allow generation of new cost functions using the same class
     * as the current one.
     *
     * @TODO: Review this thing. Dependency injection anyone?
     */
    public CostFunction buildCostFunction(Variable[] variables);
    /**
     * Utility to allow generation of new cost functions using the same class
     * as the current one.
     */
    public CostFunction buildCostFunction(Variable[] variables, int initialValue);
    /**
     * Utility to allow generation of new cost functions using the same class
     * as the current one.
     */
    public CostFunction buildCostFunction(CostFunction factor);
    
}
