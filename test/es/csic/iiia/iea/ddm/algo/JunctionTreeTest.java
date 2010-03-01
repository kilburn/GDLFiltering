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

package es.csic.iiia.iea.ddm.algo;

import es.csic.iiia.iea.ddm.CostFunction;
import es.csic.iiia.iea.ddm.CostFunctionFactory;
import es.csic.iiia.iea.ddm.HypercubeCostFunction;
import es.csic.iiia.iea.ddm.HypercubeCostFunctionFactory;
import es.csic.iiia.iea.ddm.Variable;
import es.csic.iiia.iea.ddm.cg.CliqueGraph;
import es.csic.iiia.iea.ddm.dfs.MCS;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class JunctionTreeTest {

    private Variable[] v;
    private CostFunctionFactory factory;

    public JunctionTreeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        factory = new HypercubeCostFunctionFactory();

        v = new Variable[10];
        for (int i=0; i<10; i++) {
            v[i] = new Variable(String.valueOf(i+1), 2);
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of buildGraph method, of class JunctionTreeAlgo.
     */
    @Test
    public void testBuildGraph_FactorArr_booleanArrArr() {

        char[][] adjacency = new char[][]{
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        };

        CostFunction[][] factors = new CostFunction[][]{
            {factory.buildCostFunction( new Variable[]{v[0],v[7]} )},
            {},
            {factory.buildCostFunction( new Variable[]{v[1],v[2]} )},
            {factory.buildCostFunction( new Variable[]{v[2],v[3]} )},
            {factory.buildCostFunction( new Variable[]{v[3],v[4]} ),
                     factory.buildCostFunction( new Variable[]{v[1],v[3],v[4]} )},
            {factory.buildCostFunction( new Variable[]{v[1],v[5]} )},
            {factory.buildCostFunction( new Variable[]{v[2],v[6]} ),
                     factory.buildCostFunction( new Variable[]{v[1],v[6],v[7]} ),
                     factory.buildCostFunction( new Variable[]{v[0],v[6]} )},
            {factory.buildCostFunction( new Variable[]{v[3],v[7]} )},
            {factory.buildCostFunction( new Variable[]{v[4],v[8]} ),
                     factory.buildCostFunction( new Variable[]{v[5],v[8]} ),
                     factory.buildCostFunction( new Variable[]{v[0],v[8]} )},
            {factory.buildCostFunction( new Variable[]{v[2],v[5],v[9]} ),
                     factory.buildCostFunction( new Variable[]{v[5],v[6],v[9]} ),
                     factory.buildCostFunction( new Variable[]{v[4],v[7],v[9]} ),
                     factory.buildCostFunction( new Variable[]{v[0],v[8],v[9]} )},
        };
        
        CliqueGraph result = JunctionTreeAlgo.buildGraph(factors, adjacency);
        JunctionTreeAlgo.propagateVariables(result);
        //System.out.println(result);
    }

    /**
     * Test of buildGraph method, of class JunctionTreeAlgo.
     */
    @Test
    public void testBuildGraph_FactorArr() {
        // And now factors
        CostFunction[] factors = new CostFunction[]{
            factory.buildCostFunction( new Variable[]{v[0],v[1]} ),
            factory.buildCostFunction( new Variable[]{v[0],v[1]} ),
            factory.buildCostFunction( new Variable[]{v[0],v[1]} ),
            factory.buildCostFunction( new Variable[]{v[0],v[2]} ),
            factory.buildCostFunction( new Variable[]{v[0],v[2]} ),
            factory.buildCostFunction( new Variable[]{v[0],v[3]} ),
            factory.buildCostFunction( new Variable[]{v[0],v[4]} ),
            factory.buildCostFunction( new Variable[]{v[1],v[2]} ),
            factory.buildCostFunction( new Variable[]{v[1],v[3]} ),
            factory.buildCostFunction( new Variable[]{v[3],v[4]} ),
        };

        MCS mcs = new MCS(factors);
        CliqueGraph result = JunctionTreeAlgo.buildGraph(mcs.getFactorDistribution(), mcs.getAdjacency());
        JunctionTreeAlgo.propagateVariables(result);
        System.out.println(result);
    }

}
