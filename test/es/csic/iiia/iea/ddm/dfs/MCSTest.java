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

package es.csic.iiia.iea.ddm.dfs;

import es.csic.iiia.iea.ddm.Factor;
import es.csic.iiia.iea.ddm.Variable;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map.Entry;
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
public class MCSTest {

    private Variable[] v;
    private Factor[] f;

    public MCSTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        // Set up variables
        v = new Variable[10];
        for (int i=0; i<10; i++) {
            v[i] = new Variable(String.valueOf(i+1), 2);
        }

        // And now factors
        f = new Factor[]{
            new Factor( new Variable[]{v[0],v[1]} ),
            new Factor( new Variable[]{v[0],v[1]} ),
            new Factor( new Variable[]{v[0],v[1]} ),
            new Factor( new Variable[]{v[0],v[2]} ),
            new Factor( new Variable[]{v[0],v[2]} ),
            new Factor( new Variable[]{v[0],v[3]} ),
            new Factor( new Variable[]{v[0],v[4]} ),
            new Factor( new Variable[]{v[1],v[2]} ),
            new Factor( new Variable[]{v[1],v[3]} ),
            new Factor( new Variable[]{v[3],v[4]} ),
        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetDFS() {
        Hashtable<Variable, char[]> expectedResult = new Hashtable<Variable, char[]>();
        expectedResult.put(v[0], new char[]{0, 0, 0, 0, 0});
        expectedResult.put(v[1], new char[]{1, 0, 0, 0, 0});
        expectedResult.put(v[2], new char[]{0, 1, 0, 0, 0});
        expectedResult.put(v[3], new char[]{0, 1, 0, 0, 0});
        expectedResult.put(v[4], new char[]{0, 0, 0, 1, 0});

        MCS mcs = new MCS(f);
        Hashtable<Variable, char[]> result = mcs.getDFS();
        //mcs.printDFS();
        for (Entry<Variable,char[]> e : result.entrySet()) {
            assertArrayEquals(e.getValue(), expectedResult.get(e.getKey()));
        }
    }

    @Test
    public void testAssignFactors() {
        Hashtable<Variable, Factor[]> expectedResult = new Hashtable<Variable, Factor[]>();
        expectedResult.put(v[0], new Factor[]{});
        expectedResult.put(v[1], new Factor[]{f[0], f[1], f[2]});
        expectedResult.put(v[2], new Factor[]{f[3], f[4], f[7]});
        expectedResult.put(v[3], new Factor[]{f[5], f[8]});
        expectedResult.put(v[4], new Factor[]{f[6], f[9]});

        MCS mcs = new MCS(f);
        Hashtable<Variable, Factor[]> result = mcs.getFactorAssignments();
        for (Entry<Variable,Factor[]> e : result.entrySet()) {

            System.out.print(e.getKey().getName() + ":[");
            Factor[] fl = e.getValue();
            for (int i=0; i<fl.length; i++) {
                if (i>0) System.out.print(", ");
                System.out.print(fl[i].getName());
            }
            System.out.println("]");
            
            assertArrayEquals(e.getValue(), expectedResult.get(e.getKey()));
        }
    }

}