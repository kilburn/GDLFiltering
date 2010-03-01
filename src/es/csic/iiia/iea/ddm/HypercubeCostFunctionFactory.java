/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010, Expression company is undefined on line 6, column 62 in Templates/Licenses/license-bsd.txt.
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
 *   Neither the name of Expression company is undefined on line 22, column 41 in Templates/Licenses/license-bsd.txt. 
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   Expression company is undefined on line 26, column 21 in Templates/Licenses/license-bsd.txt.
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
 * @author marc
 */
public class HypercubeCostFunctionFactory extends AbstractCostFunctionFactory {

    public CostFunction buildCostFunction(Variable[] variables) {
        HypercubeCostFunction c = new HypercubeCostFunction(variables);
        initialize(c);
        return c;
    }

    public CostFunction buildCostFunction(Variable[] variables, double initialValue) {
        HypercubeCostFunction c = new HypercubeCostFunction(variables);
        initialize(c, initialValue);
        return c;
    }

    public CostFunction buildCostFunction(CostFunction function) {
        HypercubeCostFunction c = new HypercubeCostFunction(function);
        return c;
    }

}
