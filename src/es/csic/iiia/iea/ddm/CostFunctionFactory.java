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

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CostFunctionFactory {

    public static final int HYPERCUBE_FACTOR = 1;
    public static final int LIST_FACTOR = 2;

    private int type;
    
    public CostFunctionFactory() {
        type = HYPERCUBE_FACTOR;
    }
    
    public CostFunctionFactory(int type) {
        this.type = type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public CostFunction buildCostFunction(Variable[] variables) {
        AbstractCostFunction f = null;
        switch(type) {
            case HYPERCUBE_FACTOR:
                f = new HypercubeCostFunction(variables);
                break;
            case LIST_FACTOR:
                f = new ListCostFunction(variables);
                break;
        }
        return f;
    }

    public CostFunction buildCostFunction(Variable[] variables, int initialValue) {
        AbstractCostFunction f = null;
        switch(type) {
            case HYPERCUBE_FACTOR:
                f = new HypercubeCostFunction(variables, initialValue);
                break;
            case LIST_FACTOR:
                f = new ListCostFunction(variables, initialValue);
                break;
        }
        return f;
    }

    public CostFunction buildCostFunction(CostFunction function) {
        AbstractCostFunction f = null;
        switch(type) {
            case HYPERCUBE_FACTOR:
                f = new HypercubeCostFunction(function);
            case LIST_FACTOR:
                f = new ListCostFunction(function);
        }
        return f;
    }

}