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

import java.util.Arrays;
import gnu.trove.iterator.TLongIterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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
@Ignore
public abstract class CostFunctionTest {

    protected CostFunction instance;
    private Variable[] variables;
    protected CostFunctionFactory factory;

    private Variable a,b,c,d;
    private CostFunction f1, fda, fdc, fa, fb;

    public abstract CostFunctionFactory buildFactory();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        factory = buildFactory();

        variables = new Variable[] {
            new Variable("a", 3),
            new Variable("b", 3),
            new Variable("c", 3)
        };
        instance = factory.buildCostFunction(variables);

        a = new Variable("a", 2);
        b = new Variable("b", 2);
        c = new Variable("c", 3);
        d = new Variable("d", 2);

        f1 = factory.buildCostFunction(new Variable[]{a,b,c});
        f1.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });

        fda = factory.buildCostFunction(new Variable[]{d,a});
        fda.setValues(new double[]{0.1, 0.4, 0.3, 0.2});

        fdc = factory.buildCostFunction(new Variable[]{d,c});
        fdc.setValues(new double[]{0.1, 0.15, 0.25, 0.15, 0.3, 0.15});

        fa = factory.buildCostFunction(new Variable[]{a});
        fa.setValues(new double[]{0.3, 0.7});

        fb = factory.buildCostFunction(new Variable[]{b});
        fb.setValues(new double[]{0.6, 0.4});
    }

    @After
    public void tearDown() {
    }

    /**
     * Test equality, of class CostFunction.
     */
    @Test
    public void testEquals() {
        double[] values = {
            000, 001, 002,
            010, 011, 012,
            020, 021, 022,
            100, 101, 102,
            110, 111, 112,
            120, 121, 122,
            200, 201, 202,
            210, 211, 212,
            220, 221, 222,
        };
        instance.setValues(values);

        CostFunction instance2 = factory.buildCostFunction(new Variable[] {
            variables[0],
            variables[2],
            variables[1],
        });
        assertFalse(instance.equals(instance2));


        double[] values2 = {
            000, 010, 020,
            001, 011, 021,
            002, 012, 022,
            100, 110, 120,
            101, 111, 121,
            102, 112, 122,
            200, 210, 220,
            201, 211, 221,
            202, 212, 222,
        };
        instance2.setValues(values2);
        assertEquals(instance, instance2);

        instance.setValue(new int[]{0,0,0}, 0.5d);
        assertFalse(instance.equals(instance2));
    }

    /**
     * Test of copy constructor, of class CostFunction.
     */
    @Test
    public void testCopyConstructor() {
        CostFunction f = factory.buildCostFunction(f1);
        
        assertNotSame(f, f1);
        assertEquals(f, f1);
        assertNotSame(f.getVariableSet(), f1.getVariableSet());
        assertSame(f.getFactory(), f1.getFactory());
    }


    /**
     * Test of getIndex method, of class CostFunction.
     */
    @Test
    public void testGetIndex1() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 0);
        map.put(variables[1], 0);
        map.put(variables[2], 0);
        assertEquals(0, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class CostFunction.
     */
    @Test
    public void testGetIndex2() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(15, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class CostFunction.
     */
    @Test
    public void testGetIndex3() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        assertEquals(26, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class CostFunction.
     */
    @Test
    public void testGetIndex4() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        // Non-functor variables in the mapping should be ignored
        map.put(new Variable("a", 20), 2);
        map.put(new Variable("d", 3), 2);
        assertEquals(26, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class CostFunction.
     */
    @Test
    public void testGetIndex5() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        // Double-assignment to the mapping should just overwrite the previous
        // value
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(15, instance.getIndex(map));
    }

    /**
     * Test of getMapping method, of class CostFunction.
     */
    @Test
    public void testGetMapping1() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 0);
        map.put(variables[1], 0);
        map.put(variables[2], 0);
        assertEquals(map, instance.getMapping(0, null));
    }

    /**
     * Test of getMapping method, of class CostFunction.
     */
    @Test
    public void testGetMapping2() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(map, instance.getMapping(15, null));
    }

    /**
     * Test of getMapping method, of class CostFunction.
     */
    @Test
    public void testGetMapping3() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        assertEquals(map, instance.getMapping(26, null));
    }

    /**
     * Test of getMapping method, of class CostFunction.
     */
    @Test
    public void testGetMapping4() {
        Variable[] vars = new Variable[]{
            new Variable("a", 2),
            new Variable("b", 2),
            new Variable("c", 3),
        };
        CostFunction f = factory.buildCostFunction(vars);
        VariableAssignment map = new VariableAssignment();
        map.put(vars[0], 0);
        map.put(vars[1], 1);
        map.put(vars[2], 1);
        assertEquals(map, f.getMapping(4, null));
    }

    /**
     * Test of setValue method, of class CostFunction.
     */
    @Test
    public void testSetValue1() {
        int[] sub = {1, 2, 0};
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
    }

    /**
     * Test of setValue method, of class CostFunction.
     */
    @Test
    public void testSetValue2() {
        int[] sub = {2, 2, 2};
        int index = 26;
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
    }

    /**
     * Test of setValue method, of class CostFunction.
     */
    @Test
    public void testSetValue3() {
        int[] sub = {1, 2, 0};
        int index = 15;
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
    }

    /**
     * Test of setValue method, of class CostFunction.
     */
    @Test
    public void testSetValue4() {
        int[] index = {2, 2};
        double value = 5.2d;
        try {
            instance.setValue(index, value);
            fail("Value set for an invalid index!");
        } catch (RuntimeException e) {}
    }

    /**
     * Test of setValue method, of class CostFunction.
     */
    @Test
    public void testSetValue5() {
        int[] index = {2, 4, 2};
        double value = 5.2d;
        try {
            instance.setValue(index, value);
            fail("Value set for an invalid index!");
        } catch (RuntimeException e) {}
    }

    /**
     * Test of setValues method, of class CostFunction.
     */
    @Test
    public void testSetValues1() {
        double[] values = {
            000, 001, 002,
            010, 011, 012,
            020, 021, 022,
            100, 101, 102,
            110, 111, 112,
            120, 121, 122,
            200, 201, 202,
            210, 211, 212,
            220, 221, 222,
        };
        instance.setValues(values);
    }

    /**
     * Test of setValues method, of class CostFunction.
     */
    @Test
    public void testSetValues2() {
        double[] values = {0};
        try {
            instance.setValues(values);
            fail("Correctly set an invalid number of values!");
        } catch (RuntimeException e) {}
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize1() {
        Variable[] vars = new Variable[]{a};
        factory.setSummarizeOperation(CostFunction.Summarize.SUM);
        CostFunction sum = f1.summarize(vars);
        CostFunction res = factory.buildCostFunction(vars);
        res.setValues(new double[]{0.63, 0.37});
        assertEquals(res, sum);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize2() {
        Variable[] vars = new Variable[]{a};
        factory.setSummarizeOperation(CostFunction.Summarize.MAX);
        CostFunction sum = f1.summarize(vars);
        CostFunction res = factory.buildCostFunction(vars);
        res.setValues(new double[]{0.2, 0.2});
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize3() {
        Variable[] vars = new Variable[]{a,c};
        factory.setSummarizeOperation(CostFunction.Summarize.SUM);
        CostFunction sum = f1.summarize(vars);
        CostFunction res = factory.buildCostFunction(vars);
        res.setValues(new double[]{0.3,0.25,0.08,0.2,0.1,0.07});
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize4() {
        Variable[] vars = new Variable[]{a,c};
        factory.setSummarizeOperation(CostFunction.Summarize.MAX);
        CostFunction sum = f1.summarize(vars);
        CostFunction res = factory.buildCostFunction(vars);
        res.setValues(new double[]{0.2,0.2,0.05,0.2,0.1,0.04});
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize5() {
        factory.setSummarizeOperation(CostFunction.Summarize.SUM);
        CostFunction sum = f1.summarize(new Variable[]{c,a});
        CostFunction res = f1.summarize(new Variable[]{a,c});
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize6() {
        factory.setSummarizeOperation(CostFunction.Summarize.MAX);
        CostFunction sum = f1.summarize(new Variable[]{c,a});
        CostFunction res = f1.summarize(new Variable[]{a,c});
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    @Ignore
    public void testSummarize7() {
        Variable[] vars = new Variable[]{a,c,d};
        factory.setSummarizeOperation(CostFunction.Summarize.SUM);
        CostFunction sum = f1.summarize(vars);
        CostFunction res = factory.buildCostFunction(new Variable[]{a,c,d});
        res.setValues(new double[]{0.3,0,0.25,0,0.08,0,0.2,0,0.1,0,0.07,0});
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize8() {
        Variable[] vars = new Variable[]{a,b,c};
        factory.setSummarizeOperation(CostFunction.Summarize.SUM);
        CostFunction sum = f1.summarize(vars);
        assertSame(sum.getFactory(), f1.getFactory());
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c});
        assertSame(sum.getFactory(), res.getFactory());
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize9() {
        Variable[] vars = new Variable[]{d};
        factory.setSummarizeOperation(CostFunction.Summarize.MAX);
        CostFunction sum = f1.summarize(vars);
        assertSame(sum.getFactory(), f1.getFactory());
        CostFunction res = factory.buildCostFunction(new Variable[]{d});
        assertSame(sum.getFactory(), res.getFactory());
        res.setValues(new double[]{
            0.2, 0.2
        });
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize10() {
        Variable[] vars = new Variable[]{a,b};
        factory.setSummarizeOperation(CostFunction.Summarize.MIN);
        CostFunction sum = fa.summarize(vars);
        assertSame(sum.getFactory(), fa.getFactory());
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b});
        assertSame(sum.getFactory(), res.getFactory());
        res.setValues(new double[]{
            0.3, 0.3, 0.7, 0.7
        });
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize11() {
        Variable[] vars = new Variable[]{};
        factory.setSummarizeOperation(CostFunction.Summarize.MIN);
        CostFunction sum = fa.summarize(vars);
        assertSame(sum.getFactory(), fa.getFactory());
        CostFunction res = factory.buildCostFunction(new Variable[]{});
        assertSame(sum.getFactory(), res.getFactory());
        res.setValues(new double[]{
            0.3
        });
        assertEquals(sum, res);
        assertSame(sum.getFactory(), res.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombine1() {
        factory.setMode(CostFunction.Summarize.SUM, CostFunction.Combine.PRODUCT, CostFunction.Normalize.NONE);
        CostFunction sum = f1.summarize(new Variable[]{a,c});
        CostFunction com = f1.combine(sum);
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1*0.3, 0.2*.25, 0.05*.08, 0.2*0.3, 0.05*.25, 0.03*.08, 0,
            0, 0.03*.07, 0.2*0.2, 0.1*0.1, 0.04*0.07
        });
        assertEquals(com, res);
        assertSame(com.getFactory(), res.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    @Ignore
    public void testCombine2() {
        factory.setMode(CostFunction.Summarize.SUM, CostFunction.Combine.SUM, CostFunction.Normalize.NONE);
        CostFunction sum = f1.summarize(new Variable[]{a,c});
        CostFunction com = f1.combine(sum);
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1+0.3, 0.2+.25, 0.05+.08, 0.2+0.3, 0.05+.25, 0.03+.08, 0+0.2,
            0+0.1, 0.03+.07, 0.2+0.2, 0.1+0.1, 0.04+0.07
        });
        assertEquals(com, res);
        assertSame(com.getFactory(), res.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombine3() {
        factory.setCombineOperation(CostFunction.Combine.PRODUCT);
        CostFunction sf  = factory.buildCostFunction(new Variable[]{a,b,c,d}, 1);
        CostFunction com = sf.combine(fda);
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c,d});
        res.setValues(new double[]{
            0.1, 0.3, 0.1, 0.3, 0.1, 0.3,
            0.1, 0.3, 0.1, 0.3, 0.1, 0.3,
            0.4, 0.2, 0.4, 0.2, 0.4, 0.2,
            0.4, 0.2, 0.4, 0.2, 0.4, 0.2,
        });
        assertEquals(com, res);
        assertSame(com.getFactory(), res.getFactory());
    }

    @Test
    public void testCombineNogoods() {
        factory.setSummarizeOperation(CostFunction.Summarize.MIN);
        final double v = factory.getSummarizeOperation().getNoGood();

        Variable x = new Variable("x", 2);
        Variable y = new Variable("y", 2);
        Variable z = new Variable("z", 2);

        CostFunction cf1 = factory.buildCostFunction(new Variable[]{x,y});
        cf1.setValues(new double[]{v, v, v, v});
        CostFunction cf2 = factory.buildCostFunction(new Variable[]{y,z});
        cf2.setValues(new double[]{0.3, -0.3, -0.88, -0.12});

        CostFunction comb = cf1.combine(cf2);
        CostFunction res = factory.buildCostFunction(new Variable[]{x,y,z});
        res.setValues(new double[]{v, v, v, v, v, v, v, v});
        assertEquals(res, comb);

        comb = cf2.combine(cf1);
        assertEquals(res, comb);
    }

    @Test
    public void testSummarizeNogoods() {
        factory.setSummarizeOperation(CostFunction.Summarize.MAX);
        final double v = CostFunction.Summarize.MAX.getNoGood();
        Variable x = new Variable("x", 2);
        Variable y = new Variable("y", 2);
        Variable z = new Variable("z", 2);
        Variable t = new Variable("t", 2);
        Variable u = new Variable("u", 2);
        CostFunction cf = factory.buildCostFunction(new Variable[]{x,y,z,t,u});
        cf.setValues(new double[] {
            v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, 4.61, 4.61, 4.61,
            4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61,
            4.61, 4.61
        });

        assertTrue(cf.getValue(0) == v);
        CostFunction sum = cf.summarize(new Variable[0]);
        CostFunction res = factory.buildCostFunction(new Variable[0], 4.61);
        assertEquals(res, sum);

        CostFunction dif = cf.combine(res.negate());
        res = factory.buildCostFunction(new Variable[]{x,y,z,t,u});
        res.setValues(new double[] {
            v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        });
        assertEquals(res, dif);
    }

    @Test
    public void testFilter1() {
        factory.setSummarizeOperation(CostFunction.Summarize.MIN);
        factory.setCombineOperation(CostFunction.Combine.SUM);
        factory.setNormalizationType(CostFunction.Normalize.NONE);

        final double v = CostFunction.Summarize.MIN.getNoGood();
        Variable x = new Variable("x", 2);
        Variable y = new Variable("y", 2);
        CostFunction cf = factory.buildCostFunction(new Variable[]{x,y});
        cf.setValues(new double[]{v, v, 10, v});

        ArrayList<CostFunction> fl = new ArrayList<CostFunction>();
        CostFunction fi1 = factory.buildCostFunction(new Variable[]{x});
        fi1.setValues(new double[]{10, 10});


        CostFunction filtered = cf.filter(fi1, 12);

        CostFunction res = factory.buildCostFunction(new Variable[]{x,y});
        res.setValues(new double[]{v,v,v,v});

        assertEquals(res, filtered);

        // Optimal value?
        VariableAssignment map = filtered.getOptimalConfiguration(null);
        System.out.println(filtered.getValue(map));
        assertTrue(v == filtered.getValue(map));
    }


    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineEmptyFunction1() {
        factory.setCombineOperation(CostFunction.Combine.PRODUCT);
        CostFunction sf  = factory.buildNeutralCostFunction(new Variable[]{});
        assertEquals(1, sf.getSize());
        assertEquals(sf.getValue(0), CostFunction.Combine.PRODUCT.getNeutralValue(), 0.0001);
        CostFunction com = sf.combine(fda);
        assertEquals(fda, com);
        assertSame(fda.getFactory(), com.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineEmptyFunction2() {
        factory.setCombineOperation(CostFunction.Combine.PRODUCT);
        CostFunction sf  = factory.buildNeutralCostFunction(new Variable[]{});
        CostFunction com = fda.combine(sf);
        assertEquals(fda, com);
        assertSame(fda.getFactory(), com.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineConstantFunction() {
        factory.setCombineOperation(CostFunction.Combine.SUM);
        CostFunction sf  = factory.buildCostFunction(new Variable[]{});
        sf.setValue(0, 0.3);
        CostFunction com = fa.combine(sf);
        CostFunction res = factory.buildCostFunction(new Variable[]{a});
        res.setValues(new double[]{0.6, 1.0});
        assertEquals(res, com);
        assertSame(com.getFactory(), res.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineConstantFunction2() {
        factory.setCombineOperation(CostFunction.Combine.SUM);
        CostFunction sf  = factory.buildCostFunction(new Variable[]{});
        sf.setValue(0, 0.3);
        CostFunction com = sf.combine(fa);
        CostFunction res = factory.buildCostFunction(new Variable[]{a});
        res.setValues(new double[]{0.6, 1.0});
        assertEquals(res, com);
        assertSame(com.getFactory(), res.getFactory());
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineListSparse() {
        factory.setCombineOperation(CostFunction.Combine.SUM);
        final double ng = factory.getSummarizeOperation().getNoGood();

        ArrayList<CostFunction> fs = new ArrayList<CostFunction>();
        CostFunction sf  = factory.buildCostFunction(new Variable[]{c});
        sf.setValues(new double[]{ng, ng, 1.0});
        fs.add(sf);
        fs.add(fdc);
        sf = factory.buildCostFunction(new Variable[]{a});
        sf.setValues(new double[]{ng, 1.0});

        CostFunction com = sf.combine(fs);
        CostFunction res = factory.buildCostFunction(new Variable[]{c,a,d});
        res.setValues(new double[]{
            ng, ng, ng, ng,
            ng, ng, ng, ng,
            ng, ng, 2.25, 2.15
        });
        assertEquals(res, com);
        assertSame(com.getFactory(), res.getFactory());
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize1() {
        factory.setNormalizationType(CostFunction.Normalize.SUM1);
        fa = fa.normalize();
        CostFunction res = factory.buildCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            0.3, 0.7
        });
        assertEquals(fa, res);
        assertSame(fa.getFactory(), res.getFactory());
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize2() {
        factory.setNormalizationType(CostFunction.Normalize.SUM0);
        fa = fa.normalize();
        CostFunction res = factory.buildCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            -0.2, 0.2
        });
        assertEquals(fa, res);
        assertSame(fa.getFactory(), res.getFactory());
    }
    
    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize3() {
        factory.setNormalizationType(CostFunction.Normalize.SUM1);
        CostFunction fac = factory.buildCostFunction(new Variable[]{a,b,c});
        fac.setValues(new double[]{
            0.2, 0.4, 0.1, 0.4, 0.1, 0.06, 0, 0, 0.06, 0.4, 0.2, 0.08
        });
        fac = fac.normalize();
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });
        assertEquals(fac, res);
        assertSame(fac.getFactory(), res.getFactory());
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize4() {
        factory.setNormalizationType(CostFunction.Normalize.SUM0);
        f1 = f1.normalize();
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c});
        final double r = 1d/12d;
        res.setValues(new double[]{
            0.1-r, 0.2-r, 0.05-r, 0.2-r, 0.05-r, 0.03-r,
            0-r, 0-r, 0.03-r, 0.2-r, 0.1-r, 0.04-r
        });
        assertEquals(f1, res);
        assertSame(f1.getFactory(), res.getFactory());
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize5() {
        factory.setNormalizationType(CostFunction.Normalize.SUM1);
        CostFunction fac = factory.buildCostFunction(new Variable[]{a,b,c});
        fac.setValues(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        });
        fac = fac.normalize();
        CostFunction res = factory.buildCostFunction(new Variable[]{a,b,c});
        final double r = 1d/12d;
        res.setValues(new double[]{
            r, r, r, r, r, r, r, r, r, r, r, r
        });
        assertEquals(fac, res);
        assertSame(fac.getFactory(), res.getFactory());
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testNegate1() {
        factory.setCombineOperation(CostFunction.Combine.PRODUCT);
        fa = fa.negate();
        CostFunction res = factory.buildCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            1/0.3, 1/0.7
        });
        assertEquals(fa, res);
        assertSame(fa.getFactory(), res.getFactory());
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testNegate2() {
        factory.setCombineOperation(CostFunction.Combine.SUM);
        fa = fa.negate();
        CostFunction res = factory.buildCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            -0.3, -0.7
        });
        assertEquals(fa, res);
        assertSame(fa.getFactory(), res.getFactory());
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce1() {
        VariableAssignment map = new VariableAssignment();
        map.put(a, 0);
        CostFunction red = f1.reduce(map);

        CostFunction res = factory.buildCostFunction(new Variable[]{b,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03
        });
        assertEquals(red, res);
        assertSame(red.getFactory(), res.getFactory());
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce2() {
        VariableAssignment map = new VariableAssignment();
        map.put(b, 0);
        CostFunction red = f1.reduce(map);

        CostFunction res = factory.buildCostFunction(new Variable[]{a,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0, 0, 0.03
        });
        assertEquals(red, res);
        assertSame(red.getFactory(), res.getFactory());
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce3() {
        VariableAssignment map = new VariableAssignment();
        map.put(c, 0);
        CostFunction red = f1.reduce(map);

        CostFunction res = factory.buildCostFunction(new Variable[]{a,b});
        res.setValues(new double[]{
            0.1, 0.2, 0, 0.2
        });
        assertEquals(red, res);
        assertSame(red.getFactory(), res.getFactory());
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce4() {
        VariableAssignment map = new VariableAssignment();
        map.put(a, 0);
        map.put(b, 0);
        map.put(c, 0);
        CostFunction red = f1.reduce(map);
        CostFunction res = factory.buildCostFunction(new Variable[0]);
        res.setValue(0, 0.1);
        assertEquals(red, res);
    }

    /**
     * Test of iterator method, of class CostFunction.
     */
    @Test
    public void testIterator() {
        final double ng = factory.getSummarizeOperation().getNoGood();

        // Testing missing initial, existing end
        f1.setValue(0, ng);
        f1.setValue(1, ng);
        int len      = (int)(f1.getSize()-2);
        long offset  = 2;
        long[] idxs1 = new long[len];
        long[] idxs2 = new long[len];
        TLongIterator it = f1.iterator();
        for (int i=0; i<len; i++) {
            assertTrue(it.hasNext());
            idxs1[i] = i+offset;
            idxs2[i] = it.next();
        }
        Arrays.sort(idxs1);
        Arrays.sort(idxs2);
        for (int i=0; i<len; i++) {
            assertEquals(idxs1[i], idxs2[i]);
        }
        try {
            assertFalse(it.hasNext());
            it.next();
            fail("Calling next() should raise NoSuchElementException at this point.");
        } catch (NoSuchElementException e) {}

        // Testing existing initial, missing end
        f1.setValue(0, 0);
        f1.setValue(f1.getSize()-1, ng);
        it = f1.iterator();
        offset = 1;
        idxs1  = new long[len];
        idxs2  = new long[len];
        it = f1.iterator();
        idxs1[0] = 0;
        idxs2[0] = it.next();
        for (int i=1; i<len; i++) {
            assertTrue(it.hasNext());
            idxs1[i] = i+offset;
            idxs2[i] = it.next();
        }
        Arrays.sort(idxs1);
        Arrays.sort(idxs2);
        for (int i=1; i<len; i++) {
            assertEquals(idxs1[i], idxs2[i]);
        }
        try {
            assertFalse(it.hasNext());
            it.next();
            fail("Calling next() should raise NoSuchElementException at this point.");
        } catch (NoSuchElementException e) {}
    }
    
}
