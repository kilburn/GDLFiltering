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

package es.csic.iiia.dcop.gdlf.strategies.slice;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.util.CostFunctionStats;
import es.csic.iiia.dcop.util.metrics.Metric;
import java.util.ArrayList;
import java.util.List;

/**
 * Brute force decomposition method.
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class BruteForceSliceStrategy implements SliceStrategy {
    
    private final Metric metric;
    
    /**
     * Builds a new brute force decompositioner.
     * 
     * @param metric metric to evaluate how informative the extracted functions
     * are.
     */
    public BruteForceSliceStrategy(Metric metric) {
        this.metric = metric;
    }

    public List<CostFunction> slice(List<CostFunction> fs, int r) {
        List<CostFunction> res = new ArrayList<CostFunction>();
        for (CostFunction f : fs) {
            sliceFunction(f, r, res);
        }

        return res;
    }

    private void sliceFunction(CostFunction f, int r, List<CostFunction> fs) {
        // Don't try to break a fitting message into smaller pieces
        if (f.getVariableSet().size() <= r) {
            fs.add(f);
            return;
        }
        
        // Obtain the projection approximation
        CostFunction[] res = CostFunctionStats.getBestApproximation(
                f, r, metric, Integer.MAX_VALUE);
        for (int i=0; i<res.length-1; i++) {
            fs.add(res[i]);
        }
    }

}
