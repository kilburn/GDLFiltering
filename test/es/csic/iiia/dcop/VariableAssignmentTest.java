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

package es.csic.iiia.dcop;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
@Ignore
public class VariableAssignmentTest {

    private static int SIZE = 20;
    private static long ITERS = 10000000;
    private Variable[] v;

    public VariableAssignmentTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        v = new Variable[SIZE];
        for (int i=0; i<SIZE; i++) {
            v[i] = new Variable(i);
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testHashtable() {
        for (int j=0; j<ITERS; j++) {
            Hashtable<Variable, Integer> map = new Hashtable<Variable, Integer>();
            for (int i=0; i<SIZE; i++) {
                map.put(v[i], i);
            }
        }
    }

    @Test
    public void testHashMap() {
        for (int j=0; j<ITERS; j++) {
            HashMap<Variable, Integer> map = new HashMap<Variable, Integer>();
            for (int i=0; i<SIZE; i++) {
                map.put(v[i], i);
            }
        }
    }

    @Test
    public void testVariableAssignment() {
        for (int j=0; j<ITERS; j++) {
            VariableAssignment map = new VariableAssignment();
            for (int i=0; i<SIZE; i++) {
                map.put(v[i], i);
            }
        }
    }

}