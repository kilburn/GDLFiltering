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
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.IGdlNode;
import es.csic.iiia.dcop.igdl.strategy.ExpStrategy;
import es.csic.iiia.dcop.igdl.strategy.IGdlPartitionStrategy;
import es.csic.iiia.dcop.igdl.strategy.LazyStrategy;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
public class KWayPartitionerTest {

    private CostFunctionFactory factory;

    public KWayPartitionerTest() {
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
        factory.setMode(CostFunction.Summarize.MIN, CostFunction.Combine.SUM,
                CostFunction.Normalize.NONE);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getPartitions method, of class KWayPartitioner.
     */
    @Test
    public void testGetPartitions() {
        Variable x, y, z, t;
        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        t = new Variable("t", 2);
        CostFunction f1 = factory.buildCostFunction(new Variable[]{x, z});
        f1.setValues(new double[]{0.2, 0.2, 0.3, 0.1});
        CostFunction f2 = factory.buildCostFunction(new Variable[]{y, z});
        f2.setValues(new double[]{0.6, Math.random(), 0.2, 0.3});
        CostFunction f3 = factory.buildCostFunction(new Variable[]{t, y});
        f3.setValues(new double[]{0.4, 0.3, 0.6, 0.6});
        CostFunction f4 = factory.buildCostFunction(new Variable[]{t, z});
        f4.setValues(new double[]{0.2, Math.random(), 0.8, 0.3});
        CostFunction f5 = factory.buildCostFunction(new Variable[]{x, t});
        f5.setValues(new double[]{0.1, 0.3, 0.4, Math.random()});

        List<CostFunction> fs = Arrays.asList(new CostFunction[]{f1, f2, f3, f4, f5});
        List<Variable> evs = Arrays.asList(new Variable[]{x, y, z, t});
        KWayPartitioner instance = new KWayPartitioner(fs, new HashSet<Variable>(evs), 2);
        IGdlMessage result = instance.getPartitions();
        

        IGdlPartitionStrategy s = new ExpStrategy();
        IGdlNode n1 = new IGdlNode();
        n1.setR(2);
        IGdlNode n2 = new IGdlNode();
        n2.setR(2);
        UPEdge e = new UPEdge(n1,n2);
        e.setVariables(evs.toArray(new Variable[0]));
        s.initialize(n1);
        IGdlMessage result2 = s.getPartition(new ArrayList<CostFunction>(fs),e);

        result.setBelief(f1.combine(f2).combine(f3).combine(f4).combine(f5).summarize(evs.toArray(new Variable[0])));
        System.out.println(result);
        result2.setBelief(f1.combine(f2).combine(f3).combine(f4).combine(f5).summarize(evs.toArray(new Variable[0])));
        System.out.println(result2);
    }

}