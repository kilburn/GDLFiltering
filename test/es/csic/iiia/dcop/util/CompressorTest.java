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

package es.csic.iiia.dcop.util;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.HypercubeCostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CompressorTest {

    private CostFunctionFactory factory;
    private Variable x, y, z, t, v;
    private Variable[] vars;
    private CostFunction f1, f2, f3, f4;

    public CompressorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        factory = new CostFunctionFactory();
        factory.setMode(CostFunction.Summarize.MIN, CostFunction.Combine.SUM,
                CostFunction.Normalize.NONE);

        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        t = new Variable("t", 2);
        v = new Variable("t", 2);
        vars = new Variable[]{x, y, z, t, v};

        f1 = factory.buildCostFunction(vars);
        f1.setValues(new double[]{
            0.369,4.41,4.550,3.090,1.329,6.309,6.309,4.989,4.910,4.170,10.030,3.309,2.830,3.030,8.23,2.13,2.070,8.29,2.769,2.849,3.37,10.129,3.909,5.010,4.73,6.009,6.009,1.149,2.830,4.330,4.11,0.270
        });

        f2 = factory.buildCostFunction(vars, 0);
        f2.setValue(20, 0.123983);

        f3 = factory.buildCostFunction(vars, Double.POSITIVE_INFINITY);
        f3.setValue(20, 0.123983);
        
        f4 = factory.buildCostFunction(vars, 0);
        for (int i=0; i<vars.length; i+=2) {
            f4.setValue(i, Math.random());
        }
        f4.setValue(20, 0.123983);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCompressedSizeF method, of class Compressor.
     */
    @Test
    public void testGetCompressedSizeF() {
        System.out.println("getCompressedSizeF");
        System.out.println("f1 orig: " + f1.getSize()*8);
        System.out.println("f1 comp: " + Compressor.getCompressedSizeF(f1));
        System.out.println("f2 comp: " + Compressor.getCompressedSizeF(f2));
        System.out.println("f3 comp: " + Compressor.getCompressedSizeF(f3));
        System.out.println("f4 comp: " + Compressor.getCompressedSizeF(f4));
    }

    /**
     * Test of getCompressedSizeFs method, of class Compressor.
     */
    @Test
    public void testGetCompressedSizeFs() {
        System.out.println("getCompressedSizeFs");
        Collection<CostFunction> fs = Arrays.asList(
                new CostFunction[]{f1, f2, f3, f4}
        );

        System.out.println("fs comp: " + Compressor.getCompressedSizeFs(fs));
    }

}