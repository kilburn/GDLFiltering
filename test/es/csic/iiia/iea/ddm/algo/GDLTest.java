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

package es.csic.iiia.iea.ddm.algo;

import es.csic.iiia.iea.ddm.Factor;
import es.csic.iiia.iea.ddm.Variable;
import es.csic.iiia.iea.ddm.cg.CliqueGraph;
import es.csic.iiia.iea.ddm.cg.CgResult;
import es.csic.iiia.iea.ddm.mp.DefaultResults;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GDLTest {

    public GDLTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Test
    public void testGdlSprinkleTree() {
        
        Variable c,s,r;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        Variable[] variables = new Variable[] {s,c,r};

        // Cloudy
        Factor f0 = new Factor(new Variable[] {c});
        f0.setValues(new double[] {0.25, 0.75});
        // Cloudy | Sprinkler
        Factor f1 = new Factor(new Variable[] {c, s});
        f1.setValues(new double[] {0.25, 0.4, 0.75, 0.6});
        // Cloudy | Rain
        Factor f2 = new Factor(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.75, 0.2, 0.25});
        Factor[] factors = new Factor[] {f0,f1,f2};

        // Expected solution
        int summarize = Factor.SUMMARIZE_SUM;
        int combine = Factor.COMBINE_PRODUCT;
        int normalize = Factor.NORMALIZE_SUM1;
        Factor solution = EnumerateSolutions.enumerateSolutions(factors, combine);

        
        // Initialize using max-sum
        CliqueGraph cg = MaxSum.buildGraph(factors);
        cg.setMode(summarize, combine, normalize);
        DefaultResults<CgResult> results = cg.run(100);
        assertEquals(5, results.getIterations());

        // Fist beliefs are always those from the single-variable cliques
        ArrayList<CgResult> beliefs = results.getResults();
        for (int i=0; i<variables.length; i++) {
            Variable[] vars = beliefs.get(i).getFactor().getVariableSet().toArray(new Variable[]{c});
            Factor partialSolution = solution.summarize(vars, summarize);
            partialSolution.normalize(normalize);
            assertEquals(beliefs.get(i).getFactor(), partialSolution);
        }
        System.out.println(results);
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Test
    public void testGdlSprinkleTree2() {

        Variable c,s,r;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        Variable[] variables = new Variable[] {s,c,r};

        // Cloudy
        Factor f0 = new Factor(new Variable[] {c});
        f0.setValues(new double[] {0.25, 0.75});
        // Cloudy | Sprinkler
        Factor f1 = new Factor(new Variable[] {c, s});
        f1.setValues(new double[] {0.25, 0.4, 0.75, 0.6});
        // Cloudy | Rain
        Factor f2 = new Factor(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.75, 0.2, 0.25});
        Factor[] factors = new Factor[] {f0,f1,f2};

        // Expected solution
        int summarize = Factor.SUMMARIZE_MAX;
        int combine = Factor.COMBINE_PRODUCT;
        int normalize = Factor.NORMALIZE_SUM1;
        Factor solution = EnumerateSolutions.enumerateSolutions(factors, combine);

        // Initialize using max-sum
        CliqueGraph cg = MaxSum.buildGraph(factors);
        cg.setMode(summarize, combine, normalize);
        DefaultResults<CgResult> results = cg.run(100);
        assertEquals(4, results.getIterations());

        // Fist beliefs are always those from the single-variable cliques
        ArrayList<CgResult> beliefs = results.getResults();
        for (int i=0; i<variables.length; i++) {
            Variable[] vars = beliefs.get(i).getFactor().getVariableSet().toArray(new Variable[]{c});
            Factor partialSolution = solution.summarize(vars, summarize);
            partialSolution.normalize(normalize);
            assertEquals(beliefs.get(i).getFactor(), partialSolution);
        }
        System.out.println(results);
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Test
    public void testGdlSprinkleTreeU() {

        Variable c,s,r,w;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        w = new Variable("wetglass",2);

        // Cloudy
        Factor f0 = new Factor(new Variable[] {c});
        f0.setValues(new double[] {0.5, 0.5});
        // Cloudy | Sprinkler
        Factor f1 = new Factor(new Variable[] {c, s});
        f1.setValues(new double[] {0.5, 0.5, 0.9, 0.1});
        // Cloudy | Rain
        Factor f2 = new Factor(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.2, 0.2, 0.8});
        // Sprinkler | Rain | WetGlass
        Factor f3 = new Factor(new Variable[] {s, r, w});
        f3.setValues(new double[] {1, 0, 0.1, 0.9, 0.1, 0.9, 0.01, 0.99});
        Factor[] factors = new Factor[] {f1,f2,f3,f0};

        // Expected solution
        int summarize = Factor.SUMMARIZE_MAX;
        int combine = Factor.COMBINE_SUM;
        int normalize = Factor.NORMALIZE_SUM0;

        // Initialize using max-sum
        CliqueGraph cg = MaxSum.buildGraph(factors);
        cg.setMode(summarize, combine, normalize);
        DefaultResults<CgResult> results = cg.run(100);
        assertEquals(13, results.getIterations());

        // Expected beliefs
        Factor b1 = new Factor(new Variable[]{c});
        b1.setValues(new double[]{-0.1050, 0.1050});
        Factor b2 = new Factor(new Variable[]{s});
        b2.setValues(new double[]{0.1050, -0.1050});
        Factor b3 = new Factor(new Variable[]{r});
        b3.setValues(new double[]{-0.1050, 0.1050});
        Factor b4 = new Factor(new Variable[]{w});
        b4.setValues(new double[]{-0.1050, 0.1050});
        Factor b5 = new Factor(new Variable[]{c});
        b5.setValues(new double[]{-0.1050, 0.1050});
        Factor b6 = new Factor(new Variable[]{c,s});
        b6.setValues(new double[]{0.0050, 0.0950, 0.3050, -0.4050});
        Factor b7 = new Factor(new Variable[]{c,r});
        b7.setValues(new double[]{0.1950, -0.5050, -0.0950, 0.4050});
        Factor b8 = new Factor(new Variable[]{s,r,w});
        b8.setValues(new double[]{
            0.4950, -0.5050, -0.0950, 0.7050, -0.7050, 0.0950, -0.4850, 0.4950
        });

        ArrayList<Factor> solutions = new ArrayList<Factor>(8);
        solutions.addAll(Arrays.asList(
                new Factor[] {b1, b2, b3, b4, b5, b6, b7, b8})
        );

        // Solutions will be empty only if all the beliefs matched a solution
        for (CgResult b : results.getResults()) {
            assertTrue(solutions.remove(b.getFactor()));
        }
        assertEquals(0, solutions.size());
        System.out.println(results);
    }

    /**
     * Test of gdl method, of class GDL.
     */
    @Test
    public void testGdlSprinkle1() {

        Variable c,s,r,w;
        c = new Variable("cloudy", 2);
        s = new Variable("sprinkler",2);
        r = new Variable("rain",2);
        w = new Variable("wetglass",2);

        // Cloudy
        Factor f0 = new Factor(new Variable[] {c});
        f0.setValues(new double[] {0.5, 0.5});
        // Cloudy | Sprinkler
        Factor f1 = new Factor(new Variable[] {c, s});
        f1.setValues(new double[] {0.5, 0.5, 0.9, 0.1});
        // Cloudy | Rain
        Factor f2 = new Factor(new Variable[] {c, r});
        f2.setValues(new double[] {0.8, 0.2, 0.2, 0.8});
        // Sprinkler | Rain | WetGlass
        Factor f3 = new Factor(new Variable[] {s, r, w});
        f3.setValues(new double[] {1, 0, 0.1, 0.9, 0.1, 0.9, 0.01, 0.99});
        Factor[] factors = new Factor[] {f1,f2,f3,f0};

        // Expected solution
        int summarize = Factor.SUMMARIZE_SUM;
        int combine = Factor.COMBINE_PRODUCT;
        int normalize = Factor.NORMALIZE_SUM1;

        // Initialize using max-sum
        CliqueGraph cg = MaxSum.buildGraph(factors);
        cg.setMode(summarize, combine, normalize);
        DefaultResults<CgResult> results = cg.run(100);
        assertEquals(9, results.getIterations());

        // Expected beliefs
        Factor b1 = new Factor(new Variable[]{s});
        b1.setValues(new double[]{0.7, 0.3});
        Factor b2 = new Factor(new Variable[]{r});
        b2.setValues(new double[]{0.5, 0.5});
        Factor b3 = new Factor(new Variable[]{w});
        b3.setValues(new double[]{0.4015, 0.5985});
        Factor b4 = new Factor(new Variable[]{c});
        b4.setValues(new double[]{0.5, 0.5});
        Factor b5 = new Factor(new Variable[]{c,s});
        b5.setValues(new double[]{0.25, 0.25, 0.45, 0.05});
        Factor b6 = new Factor(new Variable[]{c,r});
        b6.setValues(new double[]{0.4, 0.1, 0.1, 0.4});
        Factor b7 = new Factor(new Variable[]{s,r,w});
        b7.setValues(new double[]{
            0.35, 0, 0.035, 0.3150, 0.015, 0.1350, 0.0015, 0.1485
        });
        Factor b8 = new Factor(new Variable[]{c});
        b8.setValues(new double[]{0.5, 0.5});

        ArrayList<Factor> solutions = new ArrayList<Factor>(8);
        solutions.addAll(Arrays.asList(
                new Factor[] {b1, b2, b3, b4, b5, b6, b7, b8})
        );

        // Solutions will be empty only if all the beliefs matched a solution
        for (CgResult b : results.getResults()) {
            assertTrue(solutions.remove(b.getFactor()));
        }
        assertEquals(0, solutions.size());
        System.out.println(results);
    }

}