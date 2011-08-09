/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2011, IIIA-CSIC, Artificial Intelligence Research Institute
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
package es.csic.iiia.dcop.io;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class EvidenceReader {

    public static void incorporate(List<CostFunction> factors, VariableAssignment assignments, InputStream evidence) {
        // Fetch the variables
        Set<Variable> vars = new HashSet<Variable>();
        for (CostFunction f : factors) {
            vars.addAll(f.getVariableSet());
        }
        
        BufferedReader buf = new BufferedReader(new InputStreamReader(evidence));
        StreamTokenizer sTokenizer = new StreamTokenizer(buf);
        sTokenizer.parseNumbers();
        try {
            int v=0, nvars = 0, var = 0;
            int state = 0;
            while (sTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                if (sTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                    v = (int)sTokenizer.nval;
                }
                
                switch(state) {
                    case 0:
                        if (v != 1) {
                            System.err.println("Error: this solver does not support multiple evidences in a single run.");
                            System.exit(1);
                        }
                        state = 1;
                        break;
                        
                    case 1:
                        nvars = v;
                        state = 2;
                        break;
                        
                    case 2:
                        var = v;
                        state = 3;
                        break;
                        
                    case 3:
                        nvars--;
                        if (nvars == 0) {
                            state = 4;
                        }
                        
                        fixVariable(factors, vars, assignments, var, v);
                        state = 2;
                        break;
                }
                
            }
        } catch (IOException ex) {
            Logger.getLogger(EvidenceReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void fixVariable(List<CostFunction> factors, Set<Variable> vars, 
            VariableAssignment assignments, int var, int v) {
        Variable rv = null;
        for (Variable vv : vars) {
            if (vv.getId() == var) {
                rv = vv;
                break;
            }
        }
        
        // Fix rv to v
        Variable nv = new Variable(String.valueOf(var), 1);
        assignments.put(rv, v);
        
        for (int i=0, len=factors.size(); i<len; i++) {
            factors.set(i, factors.get(i).reduce(assignments));
        }
    }
    
}
