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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author marc
 */
public class HypercubeCostFunctionTest extends AbstractCostFunctionTest {

    @Override
    public CostFunctionFactory buildFactory() {
        CostFunctionFactory f = new CostFunctionFactory();
        CostFunctionTypeFactory cff = new HypercubeCostFunctionFactory(f);
        f.setDenseFactory(cff);
        f.setSparseFactory(cff);
        return f;
    }
    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombine1() {
        factory.setMode(CostFunction.Summarize.SUM, CostFunction.Combine.PRODUCT, CostFunction.Normalize.NONE);
        CostFunction sum = f1.summarize(new Variable[]{a,c});
        CostFunction com = f1.combine(sum);
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c}, 0);
        res.setValues(new double[]{
            0.1*0.3, 0.2*.25, 0.05*.08, 0.2*0.3, 0.05*.25, 0.03*.08, 0,
            0, 0.03*.07, 0.2*0.2, 0.1*0.1, 0.04*0.07
        });
        assertEquals(res, com);
        assertSame(com.getFactory(), res.getFactory());
    }

}
