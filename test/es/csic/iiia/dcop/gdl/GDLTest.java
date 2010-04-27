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
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.algo.JunctionTreeAlgo;
import es.csic.iiia.dcop.bb.UBGraph;
import es.csic.iiia.dcop.bb.UBResult;
import es.csic.iiia.dcop.bb.UBResults;
import es.csic.iiia.dcop.dfs.DFS;
import es.csic.iiia.dcop.dfs.MCN;
import es.csic.iiia.dcop.dfs.MCS;
import es.csic.iiia.dcop.igdl.IGdlFactory;
import es.csic.iiia.dcop.igdl.IGdlGraph;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.strategy.RankStrategy;
import es.csic.iiia.dcop.igdl.strategy.RankUpStrategy;
import es.csic.iiia.dcop.jt.JunctionTree;
import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.mp.DefaultResults;
import es.csic.iiia.dcop.up.UPFactory;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.util.CostFunctionStats;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.VPResults;
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
    @Ignore
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
        for (UPResult r : results.getResults()) {
            VariableAssignment map2 = r.getFactor().getOptimalConfiguration(null);
            for (Variable v : map2.keySet()) {
                System.out.println(v.getName() + ":" + map2.get(v));
            }
        }
        System.out.println(results);

        // Extract a solution
        VPGraph vp = new VPGraph(g);
        VPResults res = vp.run(100);
        VariableAssignment map = res.getMapping();

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
    @Ignore
    public void testThings() {
        CostFunction.Summarize summarize = CostFunction.Summarize.MIN;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);

        Variable x,y,z,t,u,v;
        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        t = new Variable("t", 2);
        u = new Variable("u", 2);
        v = new Variable("v", 2);
        Variable[] variables = new Variable[] {x, y, z, t, u, v};

        CostFunction fxy = factory.buildCostFunction(new Variable[] {x, y});
        fxy.setValues(new double[] {10, 5, 2, 8});
        CostFunction res = fxy.summarize(new Variable[]{x})
                .combine(fxy.summarize(new Variable[]{y}));
        System.out.println(fxy);
        System.out.println(res);
    }

    @Test
    @Ignore
    public void testIGdlExample() {
        System.out.println("RUNNING IGDL");
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MIN;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);

        Variable x,y,z,t,u,v;
        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        t = new Variable("t", 2);
        u = new Variable("u", 2);
        v = new Variable("v", 2);
        Variable[] variables = new Variable[] {x, y, z, t, u, v};

        // Simple cycle with unique solution
        CostFunction f0 = factory.buildCostFunction(new Variable[] {x, y});
        f0.setValues(new double[] {20, 10, 10, 0});
        CostFunction f1 = factory.buildCostFunction(new Variable[] {y, t});
        f1.setValues(new double[] {0, 4, 4, 12});
        CostFunction f2 = factory.buildCostFunction(new Variable[] {z, t});
        f2.setValues(new double[] {14, 10, 14, 10});
        CostFunction f3 = factory.buildCostFunction(new Variable[] {t, u});
        f3.setValues(new double[] {3, 2, 2, 0});
        CostFunction f4 = factory.buildCostFunction(new Variable[] {z, v});
        f4.setValues(new double[] {3, 2, 2, 0});
        CostFunction f5 = factory.buildCostFunction(new Variable[] {u, v});
        f5.setValues(new double[] {0, 10, 10, 0});
        CostFunction[] factors = new CostFunction[] {f0,f1,f2,f3,f4,f5};

        // Build a junction tree
        DFS dfs = new MCN(factors);
        UPFactory f = new IGdlFactory(Integer.MAX_VALUE, new RankUpStrategy());
        UPGraph g = JunctionTreeAlgo.buildGraph(f, dfs.getFactorDistribution(), dfs.getAdjacency());
        JunctionTree jt = new JunctionTree(g);
        jt.run(100);
        System.out.println(g);

        // Run the UtilityPropagation phase
        g.setFactory(factory);
        ((IGdlGraph)g).setR(1);
        DefaultResults<UPResult> results = g.run(100);
        System.out.println(results);

        // Extract a solution
        VPGraph vp = new VPGraph(g);
        VPResults res = vp.run(100);
        VariableAssignment map = res.getMapping();

        // The solution should be 0 0 1
        assertEquals(1, (int)map.get(x));
        assertEquals(1, (int)map.get(y));
        assertEquals(1, (int)map.get(z));
        assertEquals(0, (int)map.get(t));
        assertEquals(1, (int)map.get(u));
        assertEquals(1, (int)map.get(v));


        // With a total utility of 4
        double cost = 0;
        for (CostFunction fn : factors) {
            cost += fn.getValue(map);
        }
        assertEquals(cost, 20, 0.0001);

        // Extract the UB
        UBGraph ub = new UBGraph(vp);
        UBResults rs = ub.run(100);
        System.out.println(rs);

        // UB must be 20
        for (UBResult r : rs.getResults()) {
            assertEquals(20, r.getUB(), 0.0001);
        }
    }

    @Test
    @Ignore
    public void testGdlExample() {
        System.out.println("RUNNING GDL");
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MIN;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);

        Variable x,y,z,t,u,v;
        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        t = new Variable("t", 2);
        u = new Variable("u", 2);
        v = new Variable("v", 2);
        Variable[] variables = new Variable[] {x, y, z, t, u, v};

        // Simple cycle with unique solution
        CostFunction f0 = factory.buildCostFunction(new Variable[] {x, y});
        f0.setValues(new double[] {20, 10, 10, 0});
        CostFunction f1 = factory.buildCostFunction(new Variable[] {y, t});
        f1.setValues(new double[] {0, 4, 4, 12});
        CostFunction f2 = factory.buildCostFunction(new Variable[] {z, t});
        f2.setValues(new double[] {14, 10, 14, 10});
        CostFunction f3 = factory.buildCostFunction(new Variable[] {t, u});
        f3.setValues(new double[] {3, 2, 2, 0});
        CostFunction f4 = factory.buildCostFunction(new Variable[] {z, v});
        f4.setValues(new double[] {3, 2, 2, 0});
        CostFunction f5 = factory.buildCostFunction(new Variable[] {u, v});
        f5.setValues(new double[] {0, 10, 10, 0});
        CostFunction[] factors = new CostFunction[] {f0,f1,f2,f3,f4,f5};

        // Build a junction tree
        DFS dfs = new MCN(factors);
        GdlFactory f = new GdlFactory();
        f.setMode(Modes.GRAPH);
        UPGraph g = JunctionTreeAlgo.buildGraph(f, dfs.getFactorDistribution(), dfs.getAdjacency());
        JunctionTree jt = new JunctionTree(g);
        jt.run(100);
        System.out.println(g);

        // Run the UtilityPropagation phase
        g.setFactory(factory);
        //((IGdlGraph)g).setR(2);
        DefaultResults<UPResult> results = g.run(100);
        System.out.println(results);

        // Extract a solution
        VPGraph vp = new VPGraph(g);
        VPResults res = vp.run(100);
        VariableAssignment map = res.getMapping();

        // The solution should be 0 0 1
        assertEquals((int)map.get(x), 1);
        assertEquals((int)map.get(y), 1);
        assertEquals((int)map.get(z), 1);
        assertEquals((int)map.get(t), 0);
        assertEquals((int)map.get(u), 1);
        assertEquals((int)map.get(v), 1);


        // With a total utility of 4
        double cost = 0;
        for (CostFunction fn : factors) {
            cost += fn.getValue(map);
        }
        assertEquals(cost, 20, 0.0001);
    }

    @Test
    public void testTesting() {
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MIN;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);

        Variable x,y,z,t,u,v;
        x = new Variable("9", 2);
        y = new Variable("0", 2);
        z = new Variable("7", 2);
        t = new Variable("5", 2);
        u = new Variable("18", 2);
        v = new Variable("v", 2);
        Variable[] variables = new Variable[] {x, y, z, t, u, v};

        // Simple cycle with unique solution
        CostFunction f1 = factory.buildCostFunction(new Variable[] {x});
        f1.setValues(new double[] {-0.210,-0.25});
        CostFunction f2 = factory.buildCostFunction(new Variable[] {y, z});
        f2.setValues(new double[] {-3.110,-3.909,-3.89,-2.969});
        CostFunction f3 = factory.buildCostFunction(new Variable[] {t, z});
        f3.setValues(new double[] {-17.02,-15.8,-15.899,-16.96});
        CostFunction f4 = factory.buildCostFunction(new Variable[] {u, t});
        f4.setValues(new double[] {-1.81,-0.229,-0.25,-1.83});
        CostFunction f5 = factory.buildCostFunction(new Variable[] {x, u});
        f5.setValues(new double[] {0.71,-0.71,-0.71,0.71});
        CostFunction f6 = factory.buildCostFunction(new Variable[] {u});
        f6.setValues(new double[] {-0.04,0.04});
        CostFunction[] factors = new CostFunction[] {f1,f2,f3,f4,f5,f6};

        CostFunction p1 = f4.combine(f5).combine(f6).combine(f1).summarize(new Variable[]{u});
        CostFunction p2 = f2.combine(f3).summarize(new Variable[]{y, z});
        CostFunction ok=null;
        for (CostFunction f : factors) {
            System.err.println(f);
            System.err.println(new CostFunctionStats(f));
            ok = f.combine(ok);
        }
        ok.summarize(new Variable[]{y,z,u});
        IGdlMessage m = new IGdlMessage();
        m.addFactor(p1);
        m.addFactor(p2);
        m.setBelief(ok);
        System.err.println(m);

        p1 = f1.combine(f3).combine(f4).combine(f5).combine(f6).summarize(new Variable[]{u,z});
        p2 = f2.summarize(new Variable[]{y,z});
        m = new IGdlMessage();
        m.addFactor(p1);
        m.addFactor(p2);
        m.setBelief(ok);
        System.err.println(m);

        p1 = f1.combine(f2.summarize(new Variable[]{z})).combine(f3).combine(f4)
                .combine(f5).combine(f6).summarize(new Variable[]{u,z});
        m = new IGdlMessage();
        m.addFactor(p1);
        m.setBelief(ok);
        System.err.println(m);

    }

}