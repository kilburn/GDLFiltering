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
import es.csic.iiia.dcop.util.metrics.Metric;
import es.csic.iiia.dcop.util.metrics.Norm0;
import es.csic.iiia.dcop.util.metrics.Norm1;
import es.csic.iiia.dcop.util.metrics.Norm2;
import es.csic.iiia.dcop.util.metrics.NormInf;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CostFunctionStatsTest {
    private static double delta = 0.0001;
    
    private CostFunctionFactory factory;
    private Variable x, y, z;

    public CostFunctionStatsTest() {
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
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getExp method, of class CostFunctionStats.
     */
    @Ignore
    @Test
    public void testGetExp() {
        System.out.println("getExp");
        CostFunction o1 = null;
        int expResult = 0;
        int result = CostFunctionStats.getExp(o1);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatValue method, of class CostFunctionStats.
     */
    @Test
    public void testFormatValue() {
        double value = 0.000002;
        String expResult = "0";
        String result = CostFunctionStats.formatValue(value);
        assertEquals("Near-zero values shown as 0", expResult, result);

        value = 0.2007;
        expResult = "0.200";
        result = CostFunctionStats.formatValue(value);
        assertEquals("Show a maximum of 3 decimal digits", expResult, result);

        value = 2000.2007;
        expResult = "2000.200";
        result = CostFunctionStats.formatValue(value);
        assertEquals("Always maintain the integer part", expResult, result);
    }

    /**
     * Test of getRank method, of class CostFunctionStats.
     */
    @Test
    public void testGetRank() {
        CostFunction f = factory.buildCostFunction(new Variable[]{x});
        f.setValues(new double[]{0.2, 0.8});
        double expResult = 0.6;
        double result = CostFunctionStats.getRank(f);
        assertEquals("Rank works with positive values", expResult, result, delta);
    }

    /**
     * Test of getEntropy method, of class CostFunctionStats.
     */
    @Ignore
    @Test
    public void testGetEntropy() {
        System.out.println("getEntropy");
        CostFunction f = null;
        double expResult = 0.0;
        double result = CostFunctionStats.getEntropy(f);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSum method, of class CostFunctionStats.
     */
    @Test
    public void testGetSum() {
        CostFunction f = factory.buildCostFunction(new Variable[]{x,y});
        f.setValues(new double[]{0.2, 0.8, 0.4, 0.3});
        double expResult = 1.7;
        double result = CostFunctionStats.getSum(f);
        assertEquals("getSum works with positive-only values", expResult, result, delta);

        f.setValues(new double[]{0.2, -0.8, 0.4, 0.3});
        expResult = 0.1;
        result = CostFunctionStats.getSum(f);
        assertEquals("getSum works with mixed values", expResult, result, delta);
    }

    /**
     * Test of getInformationGains method, of class CostFunctionStats.
     */
    @Test
    public void testGetInformationGains() {
        CostFunction f = factory.buildCostFunction(new Variable[]{x,y});
        f.setValues(new double[]{0.2, 0.8, 0.1, 0.3});
        double[] expResult = new double[]{0.6, 0.8};
        double[] result = CostFunctionStats.getInformationGains(f);
        for (int i=0; i<result.length; i++) {
            assertEquals("Gain with positive values", expResult[i], result[i], delta);
        }
    }

    /**
     * Test of getSum method, of class CostFunctionStats.
     */
    @Test
    public void testGet0Norm() {
        Metric metric = new Norm0();

        CostFunction f = factory.buildCostFunction(new Variable[]{x,y});
        f.setValues(new double[]{0.2, 0, 0.4, 0});
        double expResult = 2;
        double result = metric.getValue(f);
        assertEquals("L0Norm works with positive-only values", expResult, result, delta);

        f.setValues(new double[]{0, -0.8, 0.4, 0.3});
        expResult = 3;
        result = metric.getValue(f);
        assertEquals("L0Norm works with mixed values", expResult, result, delta);
    }

    /**
     * Test of getSum method, of class CostFunctionStats.
     */
    @Test
    public void testGet1Norm() {
        Metric metric = new Norm1();

        CostFunction f = factory.buildCostFunction(new Variable[]{x,y});
        f.setValues(new double[]{0.2, 0.8, 0.4, 0.3});
        double expResult = 1.7;
        double result = metric.getValue(f);
        assertEquals("L1Norm works with positive-only values", expResult, result, delta);

        f.setValues(new double[]{0.2, -0.8, 0.4, 0.3});
        expResult = 1.7;
        result = metric.getValue(f);
        assertEquals("L1Norm works with mixed values", expResult, result, delta);
    }

    /**
     * Test of getSum method, of class CostFunctionStats.
     */
    @Test
    public void testGet2Norm() {
        Metric metric = new Norm2();

        CostFunction f = factory.buildCostFunction(new Variable[]{x,y});
        f.setValues(new double[]{0.2, 0.8, 0.4, 0.3});
        double expResult = 0.93;
        double result = metric.getValue(f);
        assertEquals("L1Norm works with positive-only values", expResult, result, delta);

        f.setValues(new double[]{0.2, -0.8, 0.4, 0.3});
        expResult = 0.93;
        result = metric.getValue(f);
        assertEquals("L1Norm works with mixed values", expResult, result, delta);
    }

    /**
     * Test of getSum method, of class CostFunctionStats.
     */
    @Test
    public void testGetInfNorm() {
        Metric metric = new NormInf();

        CostFunction f = factory.buildCostFunction(new Variable[]{x,y});
        f.setValues(new double[]{0.2, 0.8, 0.4, 0.3});
        double expResult = 0.8;
        double result = metric.getValue(f);
        assertEquals("L1Norm works with positive-only values", expResult, result, delta);

        f.setValues(new double[]{0.2, -0.8, 0.4, 0.7});
        expResult = 0.7;
        result = metric.getValue(f);
        assertEquals("L1Norm works with mixed values", expResult, result, delta);
    }

    /**
     * Test of getBestApproximation method, of class CostFunctionStats.
     */
    @Test
    public void testGetBestApproximation() {
        Metric[] metrics = new Metric[]{
            new Norm0(),
            new Norm1(),
            new Norm2(),
            new NormInf(),
        };
        String[] names = new String[]{
            "Norm0", "Norm1", "Norm2", "NormInf"
        };
        for (String name : names) {
            System.out.print("\t" + name);
        }
        System.out.println();

        double[][] results = new double[metrics.length][metrics.length];


        /*CostFunction f = factory.buildCostFunction(new Variable[]{x,y,z});
        f.setValues(new double[]{0.25, -0.6, 0.22, 0.334, 0.681, -0.774, 0.1123, 0.4123});*/
        Variable xx, yy, zz, tt;
        xx = new Variable("x", 3);
        yy = new Variable("y", 3);
        zz = new Variable("z", 2);
        tt = new Variable("t", 2);
        CostFunction f1 = factory.buildCostFunction(new Variable[]{xx, zz});
        f1.setValues(new double[]{Math.random(), Math.random(), Math.random(),
            Math.random(), Math.random(), Math.random(), });
        CostFunction f2 = factory.buildCostFunction(new Variable[]{yy, zz});
        f2.setValues(new double[]{Math.random(), Math.random(), Math.random(),
            Math.random(), Math.random(), Math.random(), });
        CostFunction f3 = factory.buildCostFunction(new Variable[]{xx, yy});
        f3.setValues(new double[]{Math.random(), Math.random(), Math.random(),
            Math.random(), Math.random(), Math.random(),
            Math.random(), Math.random(), Math.random(), });
        CostFunction f4 = factory.buildCostFunction(new Variable[]{tt, zz});
        f4.setValues(new double[]{Math.random(), Math.random(), Math.random(),
            Math.random(), });

        CostFunction f = f1.combine(f2).combine(f3).combine(f4);
        System.out.println("OB: " + f);

        for (int i=0; i<metrics.length; i++) {
            CostFunction[] res = CostFunctionStats.getBestApproximation(f, 2, metrics[i], 1000);

            System.out.println(names[i]);
            for (int j=0; j<res.length-1; j++) {
                CostFunction r = res[j];
                System.out.println("\t" + r);
            }
            CostFunction remainder = res[res.length-1];
            System.out.println("\tF: " + remainder);
            
            for (int j=0; j<metrics.length; j++) {
                results[i][j] = metrics[j].getValue(remainder);
            }
        }

        for (int i=0; i<metrics.length; i++) {
            System.out.print(names[i]);
            for (int j=0; j<metrics.length; j++) {
                System.out.print("\t" + CostFunctionStats.formatValue(results[i][j]));
            }
            System.out.println();
        }

        // Retrieve the best approximation of them all
        CostFunction[] res = CostFunctionStats.getVotedBestApproximation(f, 2, 1000);
        System.out.println("Best");
        for (int i=0; i<res.length-1; i++) {
            System.out.println("\t" + res[i]);
        }

        /*
        System.out.println("===== Incremental");
        for (int i=0; i<metrics.length; i++) {
            ArrayList<CostFunction> resl = new ArrayList<CostFunction>();
            CostFunction rem = f.getFactory().buildCostFunction(f);
            for (int n=1; n<3; n++) {
                CostFunction[] res = CostFunctionStats.getBestApproximation(rem, n, metrics[i], 1000);
                CostFunction comb = null;
                for (CostFunction r : res) {
                    comb = r.combine(comb);
                }
                if (comb != null) {
                    comb.negate();
                    rem = rem.combine(comb);
                    resl.addAll(Arrays.asList(res));
                }
            }
            CostFunction[] res = resl.toArray(new CostFunction[0]);

            System.out.println(names[i]);
            CostFunction comb = null;
            for (CostFunction r : res) {
                comb = r.combine(comb);
                System.out.println("\t" + r);
            }
            comb.negate();
            CostFunction remainder = f.combine(comb);
            System.out.println("\tF: " + remainder);

            for (int j=0; j<metrics.length; j++) {
                results[i][j] = metrics[j].getValue(remainder);
            }
        }

        for (int i=0; i<metrics.length; i++) {
            System.out.print(names[i]);
            for (int j=0; j<metrics.length; j++) {
                System.out.print("\t" + CostFunctionStats.formatValue(results[i][j]));
            }
            System.out.println();
        }*/

    }

    @Test
    public void testGetApproximation2() {
        final double v = factory.getSummarizeOperation().getNoGood();
        Variable x = new Variable("x", 2);
        Variable y = new Variable("y", 2);
        Variable z = new Variable("z", 2);
        Variable t = new Variable("t", 2);
        Variable u = new Variable("u", 2);
        CostFunction cf = factory.buildSparseCostFunction(new Variable[]{x,y,z,t,u});
        cf.setValues(new double[] {
            v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, 4.61, 4.61, 4.61,
            4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61,
            4.61, 4.61
        });

        CostFunction[] fs = CostFunctionStats.getZeroDecompositionApproximation(cf, 3);
        for (int i=0;i<fs.length-1;i++) {
            System.out.println("F:" + fs[i]);
        }
        System.out.println("Remainder: " + fs[fs.length-1]);

        CostFunction[] res = CostFunctionStats.getVotedBestApproximation(cf, 3, 1000);
        System.out.println("Best");
        for (int i=0; i<res.length-1; i++) {
            System.out.println("\t" + res[i]);
        }
    }

}