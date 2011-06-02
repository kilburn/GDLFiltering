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

package es.csic.iiia.dcop.vp.strategy.solving;

import es.csic.iiia.dcop.vp.strategy.solving.SolvingStrategy;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.FactorGraph;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.dsa.DSA;
import es.csic.iiia.dcop.dsa.DSAResults;
import es.csic.iiia.dcop.vp.strategy.CandidateSolution;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class DSASolvingStrategy implements SolvingStrategy {

    public CandidateSolution getCandidateSolution(ArrayList<CostFunction> fs,
            int parentIndex, VariableAssignment parentAssignment) {

        // Sparsity check!
        double sparsity = 0;
        HashSet<Variable> totalVars = new HashSet<Variable>();
        for (CostFunction f : fs) {
            sparsity = Math.max(sparsity, f.getNumberOfNoGoods()/(double)f.getSize());
            totalVars.addAll(f.getVariableSet());
        }

        double size = 1; sparsity = 1 - sparsity;
        for (Variable v : totalVars) {
            size *= v.getDomain();
        }
        if (size * sparsity < 1e4) {
            // TODO: Implement something.
        }

        FactorGraph fg = new FactorGraph(fs);
        return new DSACandidateSolution(fg, parentIndex, parentAssignment);
    }

    private class DSACandidateSolution extends CandidateSolution {
        private FactorGraph fg;

        public DSACandidateSolution(FactorGraph fg, int parentIndex, VariableAssignment parentAssignment) {
            super(parentIndex, parentAssignment);
            this.fg = fg;

            DSA dsa = new DSA(fg);
            DSAResults res = dsa.run(1000);
            assignment = res.getGlobalAssignment();
            assignment.putAll(parentAssignment);

            this.cost = fg.getValue(assignment);
        }

        @Override
        public CandidateSolution next() {
            return new DSACandidateSolution(fg, parentIndex, parentAssignment);
        }

    }

}
