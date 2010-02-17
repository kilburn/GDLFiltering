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

package es.csic.iiia.iea.ddm.st;

import es.csic.iiia.iea.ddm.CostFunction;
import es.csic.iiia.iea.ddm.HypercubeCostFunction;
import es.csic.iiia.iea.ddm.Variable;
import es.csic.iiia.iea.ddm.algo.JunctionTreeAlgo;
import es.csic.iiia.iea.ddm.cg.CliqueGraph;
import es.csic.iiia.iea.ddm.dfs.MCS;
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
public class SpanningTreeTest {

    private Variable[] v;
    private CostFunction[] f;
    private CliqueGraph cg;

    public SpanningTreeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        v = new Variable[10];
        for (int i=0; i<10; i++) {
            v[i] = new Variable(String.valueOf(i+1), 2);
        }

        // And now factors
        f = new CostFunction[]{
            new HypercubeCostFunction( new Variable[]{v[0],v[1]} ),
            new HypercubeCostFunction( new Variable[]{v[0],v[1]} ),
            new HypercubeCostFunction( new Variable[]{v[0],v[1]} ),
            new HypercubeCostFunction( new Variable[]{v[0],v[2]} ),
            new HypercubeCostFunction( new Variable[]{v[0],v[2]} ),
            new HypercubeCostFunction( new Variable[]{v[0],v[3]} ),
            new HypercubeCostFunction( new Variable[]{v[0],v[4]} ),
            new HypercubeCostFunction( new Variable[]{v[1],v[2]} ),
            new HypercubeCostFunction( new Variable[]{v[1],v[3]} ),
            new HypercubeCostFunction( new Variable[]{v[3],v[4]} ),
        };
        for (CostFunction fac : f) {
            fac.setValues(new double[]{2, 1, 1, 0});
        }

        MCS mcs = new MCS(f);
        cg = JunctionTreeAlgo.buildGraph(mcs.getFactorDistribution(), mcs.getAdjacency());
        JunctionTreeAlgo.propagateVariables(cg);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of buildResults method, of class SpanningTree.
     */
    @Test
    public void testBuildResults() {
        SpanningTree instance = new SpanningTree(cg);
        StResults result = instance.buildResults();
        assertNotNull(result);
    }

    /**
     * Test of buildResults method, of class SpanningTree.
     */
    /*@Test
    public void testSpanningTreeConstruction() {
        SpanningTree instance = new SpanningTree(cg);
        System.out.println(instance);
    }*/

    /**
     * Test of buildResults method, of class SpanningTree.
     */
    @Test
    public void testSpanningTreeRunMax() {
        int summarize = CostFunction.SUMMARIZE_MAX;
        int combine = CostFunction.COMBINE_SUM;
        int normalize = CostFunction.NORMALIZE_NONE;
        cg.setMode(summarize, combine, normalize);
        cg.run(100);
        SpanningTree instance = new SpanningTree(cg);

        Hashtable<Variable, Integer> expResult = new Hashtable<Variable, Integer>();
        for (int i=0; i<5; i++) {
            expResult.put(v[i], 0);
        }
        StResults result = instance.run(100);
        assertEquals(result.getMapping(), expResult);
    }

    /**
     * Test of buildResults method, of class SpanningTree.
     */
    @Test
    public void testSpanningTreeRunMin() {
        int summarize = CostFunction.SUMMARIZE_MIN;
        int combine = CostFunction.COMBINE_SUM;
        int normalize = CostFunction.NORMALIZE_NONE;
        cg.setMode(summarize, combine, normalize);
        cg.run(100);
        SpanningTree instance = new SpanningTree(cg);

        Hashtable<Variable, Integer> expResult = new Hashtable<Variable, Integer>();
        for (int i=0; i<5; i++) {
            expResult.put(v[i], 1);
        }
        StResults result = instance.run(100);
        assertEquals(result.getMapping(), expResult);
    }

}