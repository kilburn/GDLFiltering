/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2009, IIIA-CSIC, Artificial Intelligence Research Institute
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class HypercubeCostFunctionTest {

    private HypercubeCostFunction instance;
    private Variable[] variables;

    private Variable a,b,c,d;
    private HypercubeCostFunction f1, fda, fdc, fa, fb;

    public HypercubeCostFunctionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        variables = new Variable[] {
            new Variable("a", 3),
            new Variable("b", 3),
            new Variable("c", 3)
        };
        instance = new HypercubeCostFunction(variables);

        a = new Variable("a", 2);
        b = new Variable("b", 2);
        c = new Variable("c", 3);
        d = new Variable("d", 2);

        f1 = new HypercubeCostFunction(new Variable[]{a,b,c});
        f1.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });

        fda = new HypercubeCostFunction(new Variable[]{d,a});
        fda.setValues(new double[]{0.1, 0.4, 0.3, 0.2});

        fdc = new HypercubeCostFunction(new Variable[]{d,c});
        fdc.setValues(new double[]{0.1, 0.15, 0.25, 0.15, 0.3, 0.15});

        fa = new HypercubeCostFunction(new Variable[]{a});
        fa.setValues(new double[]{0.3, 0.7});

        fb = new HypercubeCostFunction(new Variable[]{b});
        fb.setValues(new double[]{0.6, 0.4});
    }

    @After
    public void tearDown() {
    }

    /**
     * Test equality, of class HypercubeCostFunction.
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

        HypercubeCostFunction instance2 = new HypercubeCostFunction(new Variable[] {
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
     * Test of copy constructor, of class HypercubeCostFunction.
     */
    @Test
    public void testCopyConstructor() {
        CostFunction f = new HypercubeCostFunction(f1);
        
        assertNotSame(f, f1);
        assertEquals(f, f1);
        assertNotSame(f.getVariableSet(), f1.getVariableSet());
    }

    /**
     * Test of subindexToIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testSubindexToIndex1() {
        int[] sub = {0, 0, 0};
        int index = 0;
        assertEquals(instance.subindexToIndex(sub), index);
    }

    /**
     * Test of getIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetIndex1() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 0);
        map.put(variables[1], 0);
        map.put(variables[2], 0);
        assertEquals(0, instance.getIndex(map));
    }

    /**
     * Test of subindexToIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testSubindexToIndex2() {
        int[] sub = {1, 2, 0};
        int index = 15;
        assertEquals(instance.subindexToIndex(sub), index);
    }

    /**
     * Test of getIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetIndex2() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(15, instance.getIndex(map));
    }

    /**
     * Test of subindexToIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testSubindexToIndex3() {
        int[] sub = {2, 2, 2};
        int index = 26;
        assertEquals(instance.subindexToIndex(sub), index);
    }

    /**
     * Test of getIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetIndex3() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        assertEquals(26, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetIndex4() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        // Non-functor variables in the mapping should be ignored
        map.put(new Variable("a", 20), 2);
        map.put(new Variable("d", 3), 2);
        assertEquals(26, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetIndex5() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
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
     * Test of indexToSubindex method, of class HypercubeCostFunction.
     */
    @Test
    public void testindexToSubindex1() {
        int[] sub = {0, 0, 0};
        int index = 0;
        assertArrayEquals(instance.indexToSubindex(index), sub);
    }

    /**
     * Test of getMapping method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetMapping1() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 0);
        map.put(variables[1], 0);
        map.put(variables[2], 0);
        assertEquals(map, instance.getMapping(0, null));
    }

    /**
     * Test of subindexToIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testindexToSubindex2() {
        int[] sub = {1, 2, 0};
        int index = 15;
        assertArrayEquals(instance.indexToSubindex(index), sub);
    }

    /**
     * Test of getMapping method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetMapping2() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(map, instance.getMapping(15, null));
    }

    /**
     * Test of subindexToIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testindexToSubindex3() {
        int[] sub = {2, 2, 2};
        int index = 26;
        assertArrayEquals(instance.indexToSubindex(index), sub);
    }

    /**
     * Test of getMapping method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetMapping3() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        assertEquals(map, instance.getMapping(26, null));
    }

    /**
     * Test of subindexToIndex method, of class HypercubeCostFunction.
     */
    @Test
    public void testindexToSubindex4() {
        HypercubeCostFunction f = new HypercubeCostFunction(new Variable[]{
            new Variable("a", 2),
            new Variable("b", 2),
            new Variable("c", 3),
        });
        int[] sub = {0, 1, 1};
        int index = 4;
        assertArrayEquals(f.indexToSubindex(index), sub);
    }

    /**
     * Test of getMapping method, of class HypercubeCostFunction.
     */
    @Test
    public void testGetMapping4() {
        Variable[] vars = new Variable[]{
            new Variable("a", 2),
            new Variable("b", 2),
            new Variable("c", 3),
        };
        HypercubeCostFunction f = new HypercubeCostFunction(vars);
        Hashtable<Variable, Integer> map = new Hashtable<Variable,Integer>();
        map.put(vars[0], 0);
        map.put(vars[1], 1);
        map.put(vars[2], 1);
        assertEquals(map, f.getMapping(4, null));
    }

    /**
     * Test of setValue method, of class HypercubeCostFunction.
     */
    @Test
    public void testSetValue1() {
        int[] sub = {1, 2, 0};
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
    }

    /**
     * Test of setValue method, of class HypercubeCostFunction.
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
     * Test of setValue method, of class HypercubeCostFunction.
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
     * Test of setValue method, of class HypercubeCostFunction.
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
     * Test of setValue method, of class HypercubeCostFunction.
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
     * Test of setValues method, of class HypercubeCostFunction.
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
     * Test of setValues method, of class HypercubeCostFunction.
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
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize1() {
        Variable[] vars = new Variable[]{a};
        CostFunction sum = f1.summarize(vars, CostFunction.SUMMARIZE_SUM);
        CostFunction res = new HypercubeCostFunction(vars);
        res.setValues(new double[]{0.63, 0.37});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize2() {
        Variable[] vars = new Variable[]{a};
        CostFunction sum = f1.summarize(vars, CostFunction.SUMMARIZE_MAX);
        CostFunction res = new HypercubeCostFunction(vars);
        res.setValues(new double[]{0.2, 0.2});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize3() {
        Variable[] vars = new Variable[]{a,c};
        CostFunction sum = f1.summarize(vars, CostFunction.SUMMARIZE_SUM);
        CostFunction res = new HypercubeCostFunction(vars);
        res.setValues(new double[]{0.3,0.25,0.08,0.2,0.1,0.07});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize4() {
        Variable[] vars = new Variable[]{a,c};
        CostFunction sum = f1.summarize(vars, CostFunction.SUMMARIZE_MAX);
        CostFunction res = new HypercubeCostFunction(vars);
        res.setValues(new double[]{0.2,0.2,0.05,0.2,0.1,0.04});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize5() {
        CostFunction sum = f1.summarize(new Variable[]{c,a}, CostFunction.SUMMARIZE_SUM);
        CostFunction res = f1.summarize(new Variable[]{a,c}, CostFunction.SUMMARIZE_SUM);
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize6() {
        CostFunction sum = f1.summarize(new Variable[]{c,a}, CostFunction.SUMMARIZE_MAX);
        CostFunction res = f1.summarize(new Variable[]{a,c}, CostFunction.SUMMARIZE_MAX);
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize7() {
        Variable[] vars = new Variable[]{a,c,d};
        CostFunction sum = f1.summarize(vars, CostFunction.SUMMARIZE_SUM);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,c,d});
        res.setValues(new double[]{0.3,0,0.25,0,0.08,0,0.2,0,0.1,0,0.07,0});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class HypercubeCostFunction.
     */
    @Test
    public void testSummarize8() {
        Variable[] vars = new Variable[]{a,b,c};
        CostFunction sum = f1.summarize(vars, CostFunction.SUMMARIZE_SUM);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });
        assertEquals(sum, res);
    }

    /**
     * Test of combine method, of class HypercubeCostFunction.
     */
    @Test
    public void testCombine1() {
        CostFunction sum = f1.summarize(new Variable[]{a,c}, CostFunction.SUMMARIZE_SUM);
        CostFunction com = f1.combine(sum, CostFunction.COMBINE_PRODUCT);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1*0.3, 0.2*.25, 0.05*.08, 0.2*0.3, 0.05*.25, 0.03*.08, 0,
            0, 0.03*.07, 0.2*0.2, 0.1*0.1, 0.04*0.07
        });
        assertEquals(com, res);
    }

    /**
     * Test of combine method, of class HypercubeCostFunction.
     */
    @Test
    public void testCombine2() {
        CostFunction sum = f1.summarize(new Variable[]{a,c}, CostFunction.SUMMARIZE_SUM);
        CostFunction com = f1.combine(sum, CostFunction.COMBINE_SUM);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1+0.3, 0.2+.25, 0.05+.08, 0.2+0.3, 0.05+.25, 0.03+.08, 0+0.2,
            0+0.1, 0.03+.07, 0.2+0.2, 0.1+0.1, 0.04+0.07
        });
        assertEquals(com, res);
    }

    /**
     * Test of combine method, of class HypercubeCostFunction.
     */
    @Test
    public void testCombine3() {
        CostFunction sf  = new HypercubeCostFunction(new Variable[]{a,b,c,d}, 1);
        CostFunction com = sf.combine(fda, CostFunction.COMBINE_PRODUCT);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c,d});
        res.setValues(new double[]{
            0.1, 0.3, 0.1, 0.3, 0.1, 0.3,
            0.1, 0.3, 0.1, 0.3, 0.1, 0.3,
            0.4, 0.2, 0.4, 0.2, 0.4, 0.2,
            0.4, 0.2, 0.4, 0.2, 0.4, 0.2,
        });
        assertEquals(com, res);
    }

    /**
     * Test of combine method, of class HypercubeCostFunction.
     */
    @Test
    public void testCombineEmptyFunction1() {
        CostFunction sf  = new HypercubeCostFunction(new Variable[]{});
        CostFunction com = sf.combine(fda, CostFunction.COMBINE_PRODUCT);
        assertEquals(fda, com);
    }

    /**
     * Test of combine method, of class HypercubeCostFunction.
     */
    @Test
    public void testCombineEmptyFunction2() {
        CostFunction sf  = new HypercubeCostFunction(new Variable[]{});
        CostFunction com = fda.combine(sf, CostFunction.COMBINE_PRODUCT);
        assertEquals(fda, com);
    }

    /**
     * Test of normalize method, of class HypercubeCostFunction.
     */
    @Test
    public void testNormalize1() {
        fa.normalize(HypercubeCostFunction.NORMALIZE_SUM1);
        HypercubeCostFunction res = new HypercubeCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            0.3, 0.7
        });
        assertEquals(fa, res);
    }

    /**
     * Test of normalize method, of class HypercubeCostFunction.
     */
    @Test
    public void testNormalize2() {
        fa.normalize(CostFunction.NORMALIZE_SUM0);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            -0.2, 0.2
        });
        assertEquals(fa, res);
    }
    
    /**
     * Test of normalize method, of class HypercubeCostFunction.
     */
    @Test
    public void testNormalize3() {
        CostFunction fac = new HypercubeCostFunction(new Variable[]{a,b,c});
        fac.setValues(new double[]{
            0.2, 0.4, 0.1, 0.4, 0.1, 0.06, 0, 0, 0.06, 0.4, 0.2, 0.08
        });
        fac.normalize(CostFunction.NORMALIZE_SUM1);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });
        assertEquals(fac, res);
    }

    /**
     * Test of normalize method, of class HypercubeCostFunction.
     */
    @Test
    public void testNormalize4() {
        f1.normalize(CostFunction.NORMALIZE_SUM0);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c});
        final double r = 1d/12d;
        res.setValues(new double[]{
            0.1-r, 0.2-r, 0.05-r, 0.2-r, 0.05-r, 0.03-r,
            0-r, 0-r, 0.03-r, 0.2-r, 0.1-r, 0.04-r
        });
        assertEquals(f1, res);
    }

    /**
     * Test of normalize method, of class HypercubeCostFunction.
     */
    @Test
    public void testNormalize5() {
        CostFunction fac = new HypercubeCostFunction(new Variable[]{a,b,c});
        fac.setValues(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        });
        fac.normalize(CostFunction.NORMALIZE_SUM1);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b,c});
        final double r = 1d/12d;
        res.setValues(new double[]{
            r, r, r, r, r, r, r, r, r, r, r, r
        });
        assertEquals(fac, res);
    }

    /**
     * Test of negate method, of class HypercubeCostFunction.
     */
    @Test
    public void testNegate1() {
        fa.negate(CostFunction.COMBINE_PRODUCT);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            1/0.3, 1/0.7
        });
        assertEquals(fa, res);
    }

    /**
     * Test of negate method, of class HypercubeCostFunction.
     */
    @Test
    public void testNegate2() {
        fa.negate(CostFunction.COMBINE_SUM);
        CostFunction res = new HypercubeCostFunction(new Variable[]{a});
        res.setValues(new double[]{
            -0.3, -0.7
        });
        assertEquals(fa, res);
    }

    /**
     * Test of negate method, of class HypercubeCostFunction.
     */
    @Test
    public void testReduce1() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable, Integer>();
        map.put(a, 0);
        CostFunction red = f1.reduce(map);

        CostFunction res = new HypercubeCostFunction(new Variable[]{b,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03
        });
        assertEquals(red, res);
    }

    /**
     * Test of negate method, of class HypercubeCostFunction.
     */
    @Test
    public void testReduce2() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable, Integer>();
        map.put(b, 0);
        CostFunction red = f1.reduce(map);

        CostFunction res = new HypercubeCostFunction(new Variable[]{a,c});
        res.setValues(new double[]{
            0.1, 0.2, 0.05, 0, 0, 0.03
        });
        assertEquals(red, res);
    }

    /**
     * Test of negate method, of class HypercubeCostFunction.
     */
    @Test
    public void testReduce3() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable, Integer>();
        map.put(c, 0);
        CostFunction red = f1.reduce(map);

        CostFunction res = new HypercubeCostFunction(new Variable[]{a,b});
        res.setValues(new double[]{
            0.1, 0.2, 0, 0.2
        });
        assertEquals(red, res);
    }

    /**
     * Test of negate method, of class HypercubeCostFunction.
     */
    @Test
    public void testReduce4() {
        Hashtable<Variable, Integer> map = new Hashtable<Variable, Integer>();
        map.put(a, 0);
        map.put(b, 0);
        map.put(c, 0);
        CostFunction red = f1.reduce(map);

        HypercubeCostFunction res = null;
        assertEquals(red, res);
    }

}