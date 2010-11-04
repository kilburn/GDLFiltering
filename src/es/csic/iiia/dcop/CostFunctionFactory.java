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

import es.csic.iiia.dcop.CostFunction.Combine;
import es.csic.iiia.dcop.CostFunction.Normalize;
import es.csic.iiia.dcop.CostFunction.Summarize;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CostFunctionFactory {

    /**
     * Summarize operation to use.
     */
    private CostFunction.Summarize summarizeOperation = CostFunction.Summarize.MAX;

    /**
     * Combine operation to use.
     */
    private CostFunction.Combine combineOperation = CostFunction.Combine.SUM;

    /**
     * Normalization type to use.
     */
    private CostFunction.Normalize normalizationType = CostFunction.Normalize.NONE;

    private CostFunctionTypeFactory denseFactory = new HypercubeCostFunctionFactory(this);

    public CostFunctionTypeFactory getDenseFactory() {
        return denseFactory;
    }

    public void setDenseFactory(CostFunctionTypeFactory denseFactory) {
        this.denseFactory = denseFactory;
        setMode(summarizeOperation, combineOperation, normalizationType);
    }

    public CostFunctionTypeFactory getSparseFactory() {
        return sparseFactory;
    }

    public void setSparseFactory(CostFunctionTypeFactory sparseFactory) {
        this.sparseFactory = sparseFactory;
        setMode(summarizeOperation, combineOperation, normalizationType);
    }
    private CostFunctionTypeFactory sparseFactory = new MapCostFunctionFactory(this);

    public CostFunction buildCostFunction(Variable[] variables) {
        return denseFactory.buildCostFunction(variables);
    }

    public CostFunction buildNeutralCostFunction(Variable[] variables) {
        return denseFactory.buildNeutralCostFunction(variables);
    }

    public CostFunction buildCostFunction(Variable[] variables, double initialValue) {
        return denseFactory.buildCostFunction(variables, initialValue);
    }

    public CostFunction buildCostFunction(CostFunction function) {
        double sparsity = function.getNumberOfNoGoods()/(double)function.getSize();
        return sparsity > 0.8
                ? sparseFactory.buildCostFunction(function)
                : denseFactory.buildCostFunction(function);
    }

    public CostFunction buildSparseCostFunction(Variable[] variables) {
        return sparseFactory.buildCostFunction(variables);
    }

    public CostFunction buildSparseNeutralCostFunction(Variable[] variables) {
        return sparseFactory.buildNeutralCostFunction(variables);
    }

    public CostFunction buildSparseCostFunction(Variable[] variables, double initialValue) {
        return sparseFactory.buildCostFunction(variables, initialValue);
    }

    public CostFunction buildSparseCostFunction(CostFunction function) {
        double sparsity = function.getNumberOfNoGoods()/(double)function.getSize();
        return sparsity > 0.8
                ? sparseFactory.buildCostFunction(function)
                : denseFactory.buildCostFunction(function);
    }

    public void setMode(CostFunction.Summarize summarizeOperation,
            CostFunction.Combine combineOperation,
            CostFunction.Normalize normalizationType) {
        this.combineOperation = combineOperation;
        this.summarizeOperation = summarizeOperation;
        this.normalizationType = normalizationType;
    }

    public Combine getCombineOperation() {
        return combineOperation;
    }

    public void setCombineOperation(Combine combineOperation) {
        this.combineOperation = combineOperation;
    }

    public Normalize getNormalizationType() {
        return normalizationType;
    }

    public void setNormalizationType(Normalize normalizationType) {
        this.normalizationType = normalizationType;
    }

    public Summarize getSummarizeOperation() {
        return summarizeOperation;
    }

    public void setSummarizeOperation(Summarize summarizeOperation) {
        this.summarizeOperation = summarizeOperation;
    }
 
}
