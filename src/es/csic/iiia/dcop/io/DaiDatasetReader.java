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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class DaiDatasetReader {
    private static Logger log = LoggerFactory.getLogger(DaiDatasetReader.class);

    public List<CostFunction> read(BufferedReader input, CostFunctionFactory factory) {
        StreamTokenizer sTokenizer = null;
        List<CostFunction> factors = new ArrayList<CostFunction>();
        final double nogood = factory.getSummarizeOperation().getNoGood();

        /**
         * FSM States:
         * 0. Reading nfactors.
         * 1. Reading nvars.
         * 2. Reading the nvars variable ids.
         * 3. Reading the nvars variable domains.
         * 4. Reading nelements.
         * 5. Reading the nelements idx.
         * 6. Reading the nelements value.
         */
        int state = 0;

        try {

            //use buffering, reading one line at a time
            sTokenizer = new StreamTokenizer(input);
            sTokenizer.parseNumbers();
            double v=0;
            int nfactors=0, nvars=0, vidx=0, nelements=0, en=0;
            long fsize=0, eidx=0;
            Variable[] vars = null;
            Integer[] varsIds = null;
            HashMap<Integer, Variable> varsmap = new HashMap<Integer, Variable>();
            CostFunction f = null;

            while (sTokenizer.nextToken() != StreamTokenizer.TT_EOF) {

                if (sTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                    v = sTokenizer.nval;
                } else if (sTokenizer.ttype == StreamTokenizer.TT_WORD
                        && sTokenizer.sval.equalsIgnoreCase("inf")) {
                    v = Double.POSITIVE_INFINITY;
                } else if (sTokenizer.ttype == 45
                        && sTokenizer.nextToken() == StreamTokenizer.TT_WORD
                        && sTokenizer.sval.equalsIgnoreCase("inf")) {
                    v = Double.NEGATIVE_INFINITY;
                } else {
                    throw new UnsupportedOperationException("Unrecognized token: " + sTokenizer.sval);
                }

                switch(state) {
                    case 0:
                        nfactors = (int)v;
                        state = 1;
                        break;

                    case 1:
                        nvars = (int)v;
                        varsIds = new Integer[nvars];
                        vars = new Variable[nvars];
                        vidx = 0;
                        state = 2;
                        break;

                    case 2:
                        varsIds[vidx] = (int)v;
                        if (++vidx == nvars) {
                            state = 3;
                            vidx = 0;
                        }
                        break;

                    case 3:
                        Integer id = varsIds[vidx];
                        if (varsmap.containsKey(id)) {
                            vars[vidx] = varsmap.get(id);
                        } else {
                            Variable var = new Variable(id.toString(), (int)v);
                            vars[vidx] = var;
                            varsmap.put(id, var);
                        }
                        if (++vidx == nvars) {
                            // Create the factor
                            Variable[] vars2 = new Variable[nvars];
                            for (int ii=0; ii<nvars; ii++) {
                                vars2[ii] = vars[nvars-1-ii];
                            }
                            f = factory.buildSparseCostFunction(vars2, nogood);
                            fsize = f.getSize();
                            factors.add(f);
                            state = 4;
                            vidx = 0;
                        }
                        break;

                    case 4:
                        nelements = (int)v;
                        en = 0;
                        state = 5;
                        break;

                    case 5:
                        eidx = (long)v;
                        state = 6;
                        break;

                    case 6:
                        en++;
                        f.setValue(eidx, Math.log(v));
                        if (en == nelements) {
                            state = 1;
                        } else {
                            state = 5;
                        }
                        break;
                }
            }

        } catch (IOException ex){
            ex.printStackTrace();
        }

        return factors;
    }

}
