/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2011, IIIA-CSIC, Artificial Intelligence Research Institute
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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class PerformanceTest {
    
    private static int DOMAIN = 2;
    private static int NVARS = 10;
    private static int NTIMES = 100;
    
    private CostFunctionFactory factory;
    private CostFunction falseFunction;
    
    private long preLaunch;
    
    @Before
    public void setUp() {
        factory = new CostFunctionFactory();
        CostFunctionTypeFactory cff = new HypercubeCostFunctionFactory(factory);
        factory.setDenseFactory(cff);
        factory.setSparseFactory(cff);
        factory.setCombineOperation(CostFunction.Combine.SUM);
        
        falseFunction = factory.buildCostFunction(new Variable[0], 0);
        
        preLaunch = System.nanoTime();
    }
    
    @After
    public void tearDown() {
        double afterLaunch = System.nanoTime();
        
        System.err.println("Test took " + (afterLaunch-preLaunch)/1000000f + " ms");
    }
    
    @Test
    public void testArrayPerformance() {
        int size = 1;
        long preLaunch, afterLaunch;
        
        preLaunch = System.nanoTime();
        for (int i=0; i<NVARS; i++) {
            size *= DOMAIN;
        }
        
        double[] values1 = new double[size];
        double[] values2 = new double[size];
        
        for (int i=0; i<size; i++) {
            values1[i] = 1f;
        }
        for (int i=0; i<size; i++) {
            values2[i] = 1f;
        }
        afterLaunch = System.nanoTime();
        System.err.println("-> Initialization took " + (afterLaunch-preLaunch)/1000000f + " ms");
        
        preLaunch = System.nanoTime();
        
        for (int j=0; j<NTIMES; j++) {
            double[] values3 = new double[size];
            for (int i=0; i<size; i++) {
                values3[i] = values1[i] + values2[i];
            }
        }
        afterLaunch = System.nanoTime();
        System.err.println("-> Computation took " + (afterLaunch-preLaunch)/1000000f + " ms");
    }
    
    @Test
    public void testCostFunctionPerformance() {
        long preLaunch, afterLaunch;
        
        Variable[] v1 = new Variable[NVARS];
        Variable[] v2 = new Variable[NVARS];
        for (int i = 0; i < NVARS; i++) {
            final Variable v = new Variable(DOMAIN);
            v1[i] = v;
            v2[i] = v;
        }
        
        preLaunch = System.nanoTime();
        final CostFunction f1 = factory.buildCostFunction(v1, 1);
        final CostFunction f2 = factory.buildCostFunction(v2, 1);
        afterLaunch = System.nanoTime();
        System.err.println("-> Initialization took " + (afterLaunch-preLaunch)/1000000f + " ms");
        
        preLaunch = System.nanoTime();
        for (int i=0; i<NTIMES; i++) {
            final CostFunction f3 = f1.combine(f2);
        }
        afterLaunch = System.nanoTime();
        System.err.println("-> Computation took " + (afterLaunch-preLaunch)/1000000f + " ms");
    }
    
    @Test
    @Ignore
    public void testBoth() {
        int x = 0;
        int y = 0;
        int xc = 100;
        for (int i=0; i<1000000; i++) {
            if (--xc == 0) {
                x += 1 % 10;
                if (++y == 10) x = 0;
                xc = 100;
            }
        }
    }
    
    @Test
    @Ignore
    public void testModulus() {
        int x = 0;
        int xc = 100;
        for (int i=0; i<1000000; i++) {
            if (--xc == 0) {
                x += 1 % 100;
                xc = 100;
            }
        }
    }
    
    @Test
    @Ignore
    public void testConditional() {
        int x = 0;
        int xc = 100;
        for (int i=0; i<1000000; i++) {
            if (--xc == 0) {
                if (++x == 100) x=0;
                xc = 100;
            }
        }
    }
    
}
