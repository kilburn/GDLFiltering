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

package es.csic.iiia.dcop.vp;

import es.csic.iiia.dcop.vp.strategy.expansion.GreedyExpansion;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.algo.JunctionTreeAlgo;
import es.csic.iiia.dcop.bb.UBGraph;
import es.csic.iiia.dcop.bb.UBResults;
import es.csic.iiia.dcop.dfs.MCS;
import es.csic.iiia.dcop.gdl.GdlFactory;
import es.csic.iiia.dcop.up.UPFactory;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import es.csic.iiia.dcop.vp.strategy.solving.OptimalSolvingStrategy;
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
public class VPTest {

    private Variable[] v;
    private CostFunction[] f;
    private UPGraph cg;
    private CostFunctionFactory factory;
    private VPStrategy solvingStrategy;

    public VPTest() {
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

        v = new Variable[10];
        for (int i=0; i<10; i++) {
            v[i] = new Variable(String.valueOf(i+1), 2);
        }

        // And now factors
        f = new CostFunction[]{
            factory.buildCostFunction( new Variable[]{v[0],v[1]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[0],v[1]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[0],v[1]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[0],v[2]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[0],v[2]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[0],v[3]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[0],v[4]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[1],v[2]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[1],v[3]}, 0 ),
            factory.buildCostFunction( new Variable[]{v[3],v[4]}, 0 ),
        };
        for (CostFunction fac : f) {
            fac.setValues(new double[]{2, 1, 1, 0});
        }

        MCS mcs = new MCS(Arrays.asList(f));
        UPFactory fac = new GdlFactory();
        cg = JunctionTreeAlgo.buildGraph(fac, mcs.getFactorDistribution(), mcs.getAdjacency());

        solvingStrategy = new VPStrategy(new GreedyExpansion(), new OptimalSolvingStrategy());
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of buildResults method, of class VPGraph.
     */
    @Test
    public void testBuildResults() {
        VPGraph instance = new VPGraph(cg, solvingStrategy);
        VPResults result = instance.buildResults();
        assertNotNull(result);
    }

    /**
     * Test of buildResults method, of class VPGraph.
     */
    /*@Test
    public void testSpanningTreeConstruction() {
        VPGraph instance = new VPGraph(cg);
        System.out.println(instance);
    }*/

    /**
     * Test of buildResults method, of class VPGraph.
     */
    @Test
    public void testSpanningTreeRunMax() {
        CostFunction.Summarize summarize = CostFunction.Summarize.MAX;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);
        cg.setFactory(factory);
        cg.run(100);
        VPGraph instance = new VPGraph(cg, solvingStrategy);

        VariableAssignment expResult = new VariableAssignment();
        for (int i=0; i<5; i++) {
            expResult.put(v[i], 0);
        }
        VPResults vp = instance.run(100);
        UBGraph ub = new UBGraph(instance);
        UBResults ubres = ub.run(100);
        VariableAssignment map = ubres.getMap();
        assertEquals(map, expResult);
    }

    /**
     * Test of buildResults method, of class VPGraph.
     */
    @Test
    public void testSpanningTreeRunMin() {
        CostFunction.Summarize summarize = CostFunction.Summarize.MIN;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);
        cg.setFactory(factory);
        cg.run(100);
        VPGraph instance = new VPGraph(cg, solvingStrategy);

        VariableAssignment expResult = new VariableAssignment();
        for (int i=0; i<5; i++) {
            expResult.put(v[i], 1);
        }
        VPResults result = instance.run(100);
        UBGraph ub = new UBGraph(instance);
        UBResults ubres = ub.run(100);
        VariableAssignment map = ubres.getMap();
        assertEquals(map, expResult);
    }

}
