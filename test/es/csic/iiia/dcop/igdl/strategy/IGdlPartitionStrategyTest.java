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

package es.csic.iiia.dcop.igdl.strategy;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.figdl.FIGdlNode;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.IGdlNode;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import java.util.ArrayList;
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
public abstract class IGdlPartitionStrategyTest {

    protected IUPNode node;
    protected CostFunctionFactory factory;

    public IGdlPartitionStrategyTest() {
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
        Variable x,y,z,u,v;
        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        u = new Variable("u", 2);
        v = new Variable("v", 2);

        CostFunction potential = factory.buildCostFunction(new Variable[]{x,y});
        potential.setValues(new double[]{2,1,1,2});

        node = new IGdlNode();
        node.addRelation(potential);
        node.setR(2);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class IGdlPartitionStrategy.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        IUPNode node = null;
        IGdlPartitionStrategy instance = new IGdlPartitionStrategyImpl();
        instance.initialize(node);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of partition method, of class IGdlPartitionStrategy.
     */
    @Test
    public void testPartition() {
        System.out.println("partition");
        ArrayList<CostFunction> fs = null;
        UPEdge<? extends IUPNode, IGdlMessage> e = null;
        IGdlPartitionStrategy instance = new IGdlPartitionStrategyImpl();
        IGdlMessage expResult = null;
        IGdlMessage result = instance.partition(fs, e);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPartition method, of class IGdlPartitionStrategy.
     */
    @Test
    public void testGetPartition() {
        System.out.println("getPartition");
        ArrayList<CostFunction> fs = null;
        UPEdge<? extends IUPNode, IGdlMessage> e = null;
        IGdlPartitionStrategy instance = new IGdlPartitionStrategyImpl();
        IGdlMessage expResult = null;
        IGdlMessage result = instance.getPartition(fs, e);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFilteringOptions method, of class IGdlPartitionStrategy.
     */
    @Test
    public void testSetFilteringOptions() {
        System.out.println("setFilteringOptions");
        double bound = 0.0;
        ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges = null;
        IGdlPartitionStrategy instance = new IGdlPartitionStrategyImpl();
        instance.setFilteringOptions(bound, previousEdges);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of filterFactor method, of class IGdlPartitionStrategy.
     */
    @Test
    public void testFilterFactor() {
        System.out.println("filterFactor");
        UPEdge<? extends IUPNode, IGdlMessage> e = null;
        CostFunction factor = null;
        IGdlPartitionStrategy instance = new IGdlPartitionStrategyImpl();
        CostFunction expResult = null;
        CostFunction result = instance.filterFactor(e, factor);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of filterMessage method, of class IGdlPartitionStrategy.
     */
    @Test
    public void testFilterMessage() {
        System.out.println("filterMessage");
        UPEdge<? extends IUPNode, IGdlMessage> e = null;
        IGdlMessage msg = null;
        IGdlPartitionStrategy instance = new IGdlPartitionStrategyImpl();
        IGdlMessage expResult = null;
        IGdlMessage result = instance.filterMessage(e, msg);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class IGdlPartitionStrategyImpl extends IGdlPartitionStrategy {

        public IGdlMessage partition(ArrayList<CostFunction> fs, UPEdge<? extends IUPNode, IGdlMessage> e) {
            return null;
        }
    }

}