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

package es.csic.iiia.dcop.algo;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.gdl.GdlGraph;
import es.csic.iiia.dcop.mp.DefaultResults;
import es.csic.iiia.dcop.up.UPResult;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import static org.junit.Assert.*;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class MaxSumTest {

    private Variable a,b,c,d;
    private CostFunctionFactory factory;

    public MaxSumTest() {
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
        a = new Variable("a", 3);
        b = new Variable("b", 2);
        c = new Variable("c", 2);
        d = new Variable("d", 4);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of buildGraph2 method, of class MaxSum.
     */
    @Test
    public void testBuildGraph2() {
        CostFunction f1 = factory.buildCostFunction(new Variable[]{a,c});
        CostFunction f2 = factory.buildCostFunction(new Variable[]{c,d});
        CostFunction f3 = factory.buildCostFunction(new Variable[]{a,d});

        CostFunction[] factors = new CostFunction[]{f1,f2,f3};
        GdlGraph cg = MaxSum.buildGraph(Arrays.asList(factors));

        assertEquals(cg.getNodes().size(), 6);
        assertEquals(cg.getEdges().size(), 6);
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Theory
    public void GdlSprinkleTree(CostFunctionFactory factory) {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.SUM;
        CostFunction.Combine combine = CostFunction.Combine.PRODUCT;
        CostFunction.Normalize normalize = CostFunction.Normalize.SUM1;
        factory.setMode(summarize, combine, normalize);

        Variable c,s,r;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        Variable[] variables = new Variable[] {s,c,r};

        // Cloudy
        CostFunction f0 = factory.buildCostFunction(new Variable[] {c});
        f0.setValues(new double[] {0.25, 0.75});
        // Cloudy | Sprinkler
        CostFunction f1 = factory.buildCostFunction(new Variable[] {c, s});
        f1.setValues(new double[] {0.25, 0.4, 0.75, 0.6});
        // Cloudy | Rain
        CostFunction f2 = factory.buildCostFunction(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.75, 0.2, 0.25});
        CostFunction[] factors = new CostFunction[] {f0,f1,f2};

        // Expected solution
        CostFunction solution = EnumerateSolutions.enumerateSolutions(factors);

        // Initialize using max-sum
        GdlGraph cg = MaxSum.buildGraph(Arrays.asList(factors));
        cg.setFactory(factory);
        DefaultResults<UPResult> results = cg.run(100);
        System.out.println(results);
        //assertEquals(4, results.getIterations());

        // Fist beliefs are always those from the single-variable cliques
        ArrayList<UPResult> beliefs = results.getResults();
        for (int i=0; i<variables.length; i++) {
            Variable[] vars = beliefs.get(i).getFactor().getVariableSet().toArray(new Variable[]{c});
            CostFunction partialSolution = solution.summarize(vars);
            partialSolution = partialSolution.normalize();
            assertEquals(beliefs.get(i).getFactor(), partialSolution);
        }
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Theory
    public void GdlSprinkleTree2(CostFunctionFactory factory) {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MAX;
        CostFunction.Combine combine = CostFunction.Combine.PRODUCT;
        CostFunction.Normalize normalize = CostFunction.Normalize.SUM1;
        factory.setMode(summarize, combine, normalize);

        Variable c,s,r;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        Variable[] variables = new Variable[] {s,c,r};

        // Cloudy
        CostFunction f0 = factory.buildCostFunction(new Variable[] {c});
        f0.setValues(new double[] {0.25, 0.75});
        // Cloudy | Sprinkler
        CostFunction f1 = factory.buildCostFunction(new Variable[] {c, s});
        f1.setValues(new double[] {0.25, 0.4, 0.75, 0.6});
        // Cloudy | Rain
        CostFunction f2 = factory.buildCostFunction(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.75, 0.2, 0.25});
        CostFunction[] factors = new CostFunction[] {f0,f1,f2};

        // Expected solution
        CostFunction solution = EnumerateSolutions.enumerateSolutions(factors);

        // Initialize using max-sum
        GdlGraph cg = MaxSum.buildGraph(Arrays.asList(factors));
        cg.setFactory(factory);
        DefaultResults<UPResult> results = cg.run(100);
        assertEquals(4, results.getIterations());

        // Fist beliefs are always those from the single-variable cliques
        ArrayList<UPResult> beliefs = results.getResults();
        for (int i=0; i<variables.length; i++) {
            Variable[] vars = beliefs.get(i).getFactor().getVariableSet().toArray(new Variable[]{c});
            CostFunction partialSolution = solution.summarize(vars);
            partialSolution = partialSolution.normalize();
            assertEquals(beliefs.get(i).getFactor(), partialSolution);
        }
        System.out.println(results);
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Theory
    public void GdlSprinkleTreeU(CostFunctionFactory factory) {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MAX;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.SUM0;
        factory.setMode(summarize, combine, normalize);

        Variable c,s,r,w;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        w = new Variable("wetglass",2);

        // Cloudy
        CostFunction f0 = factory.buildCostFunction(new Variable[] {c});
        f0.setValues(new double[] {0.5, 0.5});
        // Cloudy | Sprinkler
        CostFunction f1 = factory.buildCostFunction(new Variable[] {c, s});
        f1.setValues(new double[] {0.5, 0.5, 0.9, 0.1});
        // Cloudy | Rain
        CostFunction f2 = factory.buildCostFunction(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.2, 0.2, 0.8});
        // Sprinkler | Rain | WetGlass
        CostFunction f3 = factory.buildCostFunction(new Variable[] {s, r, w});
        f3.setValues(new double[] {1, 0, 0.1, 0.9, 0.1, 0.9, 0.01, 0.99});
        CostFunction[] factors = new CostFunction[] {f1,f2,f3,f0};

        // Initialize using max-sum
        GdlGraph cg = MaxSum.buildGraph(Arrays.asList(factors));
        cg.setFactory(factory);
        DefaultResults<UPResult> results = cg.run(100);
        System.out.println(results);
        assertEquals(12, results.getIterations());

        // Expected beliefs
        CostFunction b1 = factory.buildCostFunction(new Variable[]{c});
        b1.setValues(new double[]{-0.1050, 0.1050});
        CostFunction b2 = factory.buildCostFunction(new Variable[]{s});
        b2.setValues(new double[]{0.1050, -0.1050});
        CostFunction b3 = factory.buildCostFunction(new Variable[]{r});
        b3.setValues(new double[]{-0.1050, 0.1050});
        CostFunction b4 = factory.buildCostFunction(new Variable[]{w});
        b4.setValues(new double[]{-0.1050, 0.1050});
        CostFunction b5 = factory.buildCostFunction(new Variable[]{c});
        b5.setValues(new double[]{-0.1050, 0.1050});
        CostFunction b6 = factory.buildCostFunction(new Variable[]{c,s});
        b6.setValues(new double[]{0.0050, 0.0950, 0.3050, -0.4050});
        CostFunction b7 = factory.buildCostFunction(new Variable[]{c,r});
        b7.setValues(new double[]{0.1950, -0.5050, -0.0950, 0.4050});
        CostFunction b8 = factory.buildCostFunction(new Variable[]{s,r,w});
        b8.setValues(new double[]{
            0.4950, -0.5050, -0.0950, 0.7050, -0.7050, 0.0950, -0.4850, 0.4950
        });

        ArrayList<CostFunction> solutions = new ArrayList<CostFunction>(8);
        solutions.addAll(Arrays.asList(
                new CostFunction[] {b1, b2, b3, b4, b5, b6, b7, b8})
        );

        // Solutions will be empty only if all the beliefs matched a solution
        for (UPResult b : results.getResults()) {
            assertTrue(solutions.remove(b.getFactor()));
        }
        assertEquals(0, solutions.size());
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Theory
    public void GdlSprinkle1(CostFunctionFactory factory) {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.SUM;
        CostFunction.Combine combine = CostFunction.Combine.PRODUCT;
        CostFunction.Normalize normalize = CostFunction.Normalize.SUM1;
        factory.setMode(summarize, combine, normalize);

        Variable c,s,r,w;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        w = new Variable("wetglass",2);

        // Cloudy
        CostFunction f0 = factory.buildCostFunction(new Variable[] {c});
        f0.setValues(new double[] {0.5, 0.5});
        // Cloudy | Sprinkler
        CostFunction f1 = factory.buildCostFunction(new Variable[] {c, s});
        f1.setValues(new double[] {0.5, 0.5, 0.9, 0.1});
        // Cloudy | Rain
        CostFunction f2 = factory.buildCostFunction(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.2, 0.2, 0.8});
        // Sprinkler | Rain | WetGlass
        CostFunction f3 = factory.buildCostFunction(new Variable[] {s, r, w});
        f3.setValues(new double[] {1, 0, 0.1, 0.9, 0.1, 0.9, 0.01, 0.99});
        CostFunction[] factors = new CostFunction[] {f1,f2,f3,f0};

        // Initialize using max-sum
        GdlGraph cg = MaxSum.buildGraph(Arrays.asList(factors));
        cg.setFactory(factory);
        DefaultResults<UPResult> results = cg.run(100);
        //assertEquals(4, results.getIterations());

        // Expected beliefs
        CostFunction b1 = factory.buildCostFunction(new Variable[]{s});
        b1.setValues(new double[]{0.7, 0.3});
        CostFunction b2 = factory.buildCostFunction(new Variable[]{r});
        b2.setValues(new double[]{0.5, 0.5});
        CostFunction b3 = factory.buildCostFunction(new Variable[]{w});
        b3.setValues(new double[]{0.4015, 0.5985});
        CostFunction b4 = factory.buildCostFunction(new Variable[]{c});
        b4.setValues(new double[]{0.5, 0.5});
        CostFunction b5 = factory.buildCostFunction(new Variable[]{c,s});
        b5.setValues(new double[]{0.25, 0.25, 0.45, 0.05});
        CostFunction b6 = factory.buildCostFunction(new Variable[]{c,r});
        b6.setValues(new double[]{0.4, 0.1, 0.1, 0.4});
        CostFunction b7 = factory.buildCostFunction(new Variable[]{s,r,w});
        b7.setValues(new double[]{
            0.35, 0, 0.035, 0.3150, 0.015, 0.1350, 0.0015, 0.1485
        });
        CostFunction b8 = factory.buildCostFunction(new Variable[]{c});
        b8.setValues(new double[]{0.5, 0.5});

        ArrayList<CostFunction> solutions = new ArrayList<CostFunction>(8);
        solutions.addAll(Arrays.asList(
                new CostFunction[] {b1, b2, b3, b4, b5, b6, b7, b8})
        );

        // Solutions will be empty only if all the beliefs matched a solution
        for (UPResult b : results.getResults()) {
            assertTrue(solutions.remove(b.getFactor()));
        }
        assertEquals(0, solutions.size());
        System.out.println(results);
    }

}
