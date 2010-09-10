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
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.CombinationGenerator;
import es.csic.iiia.dcop.util.CostFunctionStats;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the greedy decomposition approximation for FIGDL's cost
 * propagation phase.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class GreedyDecompositionStrategy extends IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    @Override
    protected IGdlMessage partition(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        // Message to be sent
        IGdlMessage msg = new IGdlMessage();

        // Combine everything
        CostFunction belief = null;
        for (CostFunction f : fs) {
            belief = f.combine(belief);
        }
        msg.cc += belief.getSize();
        belief = belief.summarize(e.getVariables());
        msg.cc += belief.getSize();
        msg.setBelief(belief);

        // Obtain the best approximation
        CostFunction res[] = CostFunctionStats.getVotedBestApproximation(belief, node.getR(), 1000);
        for (int i=0; i<res.length-1; i++) {
            msg.addFactor(res[i]);
            msg.cc += belief.getSize() * CombinationGenerator.binom(belief.getVariableSet().size(), node.getR());
        }

        return msg;
    }

}
