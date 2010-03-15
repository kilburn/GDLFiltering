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

package es.csic.iiia.dcop.gdl;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.HypercubeCostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.algo.JunctionTreeAlgo;
import es.csic.iiia.dcop.dfs.MCS;
import es.csic.iiia.dcop.jt.JunctionTree;
import es.csic.iiia.dcop.mp.DefaultResults;
import es.csic.iiia.dcop.st.SpanningTree;
import es.csic.iiia.dcop.st.StResults;
import es.csic.iiia.dcop.up.UPFactory;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPResult;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
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

    private CostFunctionFactory factory;

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
        factory = new HypercubeCostFunctionFactory();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGdlGraphMode() {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MAX;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);

        Variable a,b,c;
        a = new Variable("a", 2);
        b = new Variable("b", 2);
        c = new Variable("c", 2);
        Variable[] variables = new Variable[] {a,b,c};

        // Simple cycle with unique solution
        CostFunction f0 = factory.buildCostFunction(new Variable[] {a, b});
        f0.setValues(new double[] {0, 1, 1, 0});
        CostFunction f1 = factory.buildCostFunction(new Variable[] {b, c});
        f1.setValues(new double[] {0, 2, 1, 0});
        CostFunction f2 = factory.buildCostFunction(new Variable[] {a, c});
        f2.setValues(new double[] {0, 2, 1, 0});
        CostFunction[] factors = new CostFunction[] {f0,f1,f2};

        // Build a junction tree
        MCS mcs = new MCS(factors);
        UPFactory f = new GdlFactory();
        UPGraph g = JunctionTreeAlgo.buildGraph(f, mcs.getFactorDistribution(), mcs.getAdjacency());
        JunctionTree jt = new JunctionTree(g);
        jt.run(100);

        // Run the UtilityPropagation phase
        g.setFactory(factory);
        DefaultResults<UPResult> results = g.run(100);

        // Extract a solution
        SpanningTree st = new SpanningTree(g);
        StResults res = st.run(100);
        Hashtable<Variable, Integer> map = res.getMapping();

        // The solution should be 0 0 1
        assertEquals((int)map.get(a), 0);
        assertEquals((int)map.get(b), 0);
        assertEquals((int)map.get(c), 1);
        
        // With a total utility of 4
        double cost = 0;
        for (CostFunction fn : factors) {
            cost += fn.getValue(map);
        }
        assertEquals(cost, 4, 0.0001);
    }

    @Test
    public void testGdlTreeMode() {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MAX;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);

        Variable a,b,c;
        a = new Variable("a", 2);
        b = new Variable("b", 2);
        c = new Variable("c", 2);
        Variable[] variables = new Variable[] {a,b,c};

        // Simple cycle with unique solution
        CostFunction f0 = factory.buildCostFunction(new Variable[] {a, b});
        f0.setValues(new double[] {0, 1, 1, 0});
        CostFunction f1 = factory.buildCostFunction(new Variable[] {b, c});
        f1.setValues(new double[] {0, 2, 1, 0});
        CostFunction f2 = factory.buildCostFunction(new Variable[] {a, c});
        f2.setValues(new double[] {0, 2, 1, 0});
        CostFunction[] factors = new CostFunction[] {f0,f1,f2};

        // Build a junction tree
        MCS mcs = new MCS(factors);
        UPFactory f = new GdlFactory();
        UPGraph g = JunctionTreeAlgo.buildGraph(f, mcs.getFactorDistribution(), mcs.getAdjacency());
        JunctionTree jt = new JunctionTree(g);
        jt.run(100);

        // Run the UtilityPropagation phase
        g.setFactory(factory);
        DefaultResults<UPResult> results = g.run(100);

        // Extract a solution
        SpanningTree st = new SpanningTree(g);
        StResults res = st.run(100);
        Hashtable<Variable, Integer> map = res.getMapping();

        // The solution should be 0 0 1
        assertEquals((int)map.get(a), 0);
        assertEquals((int)map.get(b), 0);
        assertEquals((int)map.get(c), 1);

        // With a total utility of 4
        double cost = 0;
        for (CostFunction fn : factors) {
            cost += fn.getValue(map);
        }
        assertEquals(cost, 4, 0.0001);
    }

}