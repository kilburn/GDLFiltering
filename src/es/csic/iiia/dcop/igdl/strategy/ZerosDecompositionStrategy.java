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
import es.csic.iiia.dcop.util.CostFunctionStats;
import es.csic.iiia.dcop.util.metrics.Metric;
import es.csic.iiia.dcop.util.metrics.Norm0;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class ZerosDecompositionStrategy extends ApproximationStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    private Metric informationLossNorm = new Norm0();

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    @Override
    protected IGdlMessage approximate(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        long cc = 0;
        
        // Message to be sent
        IGdlMessage msg = new IGdlMessage();

        // Calculate the "big" function that should be sent
        CostFunction remaining = node.getFactory().buildNeutralCostFunction(new Variable[0]);
        remaining = remaining.combine(fs);
        
        // null belief yields an empty message
        if (remaining == null) {
            return msg;
        }

        msg.cc += remaining.getSize();
        
        // Filter the belief
        remaining = this.filterFactor(e, remaining);

        // Summarize the belief to the shared variables
        remaining = remaining.summarize(
            remaining.getSharedVariables(e.getVariables()).toArray(new Variable[0])
        );

        msg.cc += remaining.getSize();

        // Don't try to break a fitting message into smaller pieces
        if (remaining.getVariableSet().size() <= node.getR()) {
            msg.addFactor(remaining);
            return msg;
        }

        // Remove the constant value (summarization to no variables)
        CostFunction cst = remaining.summarize(new Variable[0]);
        msg.addFactor(cst);
        remaining = remaining.combine(cst.negate());
        msg.cc += remaining.getSize();

        // Obtain the projection approximation
        CostFunction[] res =
                CostFunctionStats.getZeroDecompositionApproximation(remaining, node.getR());
        for (int i=0; i<res.length-1; i++) {
            msg.addFactor(res[i]);
            msg.cc += remaining.getSize();
        }

        // And the total information lost
        msg.setInformationLoss(
                informationLossNorm.getValue(res[res.length-1])
        );

        return msg;
    }

}