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
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.figdl.FIGdlNode;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.strategy.CandidateSolution;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class OptimalSolvingStrategy implements SolvingStrategy {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);

    public int getNumberOfSolutionsToExpand(
            ArrayList<VariableAssignment> mappings,
            UPNode upnode)
    {
        int solutionsToTry = 0;
        if (true) {
            
        } else {
            int remainingSlots = VPStrategy.numberOfSolutions - mappings.size();
            double p = 1;
            for(int i=0; i<remainingSlots; i++) {
                if (Math.random() < p) {
                    solutionsToTry++;
                }
            }
        }
        return solutionsToTry;
    }

    public CandidateSolution getCandidateSolution(ArrayList<CostFunction> fs,
            int parentIndex, VariableAssignment parentAssignment)
    {
        CostFunction belief = fs.remove(fs.size()-1).combine(fs);
        return new OptimalCandidateSolution(belief, parentIndex, parentAssignment);
    }

    private class OptimalCandidateSolution extends CandidateSolution {

        private CostFunction belief;

        public OptimalCandidateSolution(CostFunction belief, int parentIndex, VariableAssignment parentAssignment) {
            super(parentIndex, parentAssignment);
            this.belief = belief;
            this.assignment = belief.getOptimalConfiguration(null);
            this.assignment.putAll(parentAssignment);
        }

        @Override
        public CandidateSolution next() {
            belief.setValue(belief.getIndex(assignment),
                    belief.getFactory().getSummarizeOperation().getNoGood());
            return new OptimalCandidateSolution(belief, parentIndex, parentAssignment);
        }

    }

}
