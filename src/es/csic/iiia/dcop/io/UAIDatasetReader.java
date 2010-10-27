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

package es.csic.iiia.dcop.io;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class UAIDatasetReader {
    private CostFunctionFactory factory;

    public CostFunction[] read(BufferedReader input, CostFunctionFactory factory) {
        this.factory = factory;
        StreamTokenizer sTokenizer = null;


        int nvars = 0;
        Variable[] vars = null;
        int nfactors = 0;
        CostFunction[] factors = null;

        /**
         * FSM States:
         * 0. Initial state. Going to read nvars.
         * 1. Reading the n variables' domain.
         * 2. Going to read nfactors.
         * 3. Reading the number of variables in factor i.
         * 4. Reading the actual variables in factor i.
         * 5. Reading nvalues in factor i (reseted)
         * 6. Reading the actual values in factor i.
         */
        int state = 0;

        try {

            //use buffering, reading one line at a time
            sTokenizer = new StreamTokenizer(input);
            double v=0; int i=0; int j=0; int nFactorVars = 0;
            Variable[] factorVars = null; int nFactorStates = 0;
            double[] factorValues = null;

            while (sTokenizer.nextToken() != StreamTokenizer.TT_EOF) {

                if (sTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                    v = sTokenizer.nval;
                } else {
                    System.err.println("Unrecognized token: " + sTokenizer.sval);
                }

                switch(state) {
                    case 0:
                        nvars = (int)v;
                        vars = new Variable[nvars];
                        i=0;
                        state = 1;
                        break;

                    case 1:
                        vars[i++] = new Variable((int)v);
                        if (i == nvars) {
                            state = 2;
                        }
                        break;

                    case 2:
                        nfactors = (int)v;
                        factors = new CostFunction[nfactors];
                        i=0;
                        state = 3;
                        break;

                    case 3:
                        nFactorVars = (int)v;
                        state = 4;
                        j=0;
                        factorVars = new Variable[nFactorVars];
                        break;

                    case 4:
                        factorVars[j++] = vars[(int)v];
                        if (j == nFactorVars) {
                            factors[i++] = factory.buildCostFunction(factorVars);
                            if (i == nfactors) {
                                state = 5;
                                i=0;
                                break;
                            }
                            state = 3;
                        }
                        break;

                    case 5:
                        nFactorStates = (int)v;
                        if (nFactorStates != factors[i].getSize()) {
                            System.err.println("Mismatch in number of tuples for a factor");
                            System.exit(0);
                        }
                        j=0; factorValues = new double[nFactorStates];
                        state = 6;
                        break;

                    case 6:
                        // The input values are probabilities, so we have to
                        // log them, and negating makes the costs positive
                        // (so we have to minimize instead of maximize)
                        factorValues[j++] = -Math.log(v);
                        if (j == nFactorStates) {
                            factors[i++].setValues(factorValues);
                            if (i == nfactors) {
                                System.out.println("Done reading");
                            }
                            state = 5;
                        }
                        break;
                }
            }

        } catch (IOException ex){
            ex.printStackTrace();
        }

        this.factory = null;
        return factors;
    }

}
