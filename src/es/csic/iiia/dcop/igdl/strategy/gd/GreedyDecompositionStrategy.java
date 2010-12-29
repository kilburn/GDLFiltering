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

package es.csic.iiia.dcop.igdl.strategy.gd;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.strategy.ApproximationStrategy;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.CombinationGenerator;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the greedy decomposition approximation for FIGDL's cost
 * propagation phase.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class GreedyDecompositionStrategy extends ApproximationStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

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
        Variable[] edgeVars = remaining.getSharedVariables(e.getVariables()).toArray(new Variable[0]);
        remaining = remaining.summarize(edgeVars);

        msg.cc += remaining.getSize();

        // Don't try to break a fitting message into smaller pieces
        if (remaining.getVariableSet().size() <= node.getR()) {
            msg.addFactor(remaining);
            return msg;
        }

        // Greedily explore the lattice of possible projections
        double best = 0;
        do {
            best = Double.NEGATIVE_INFINITY;

            // Select the projection with higher gain
            CombinationGenerator g = new CombinationGenerator(edgeVars, node.getR());
            CostFunction bestFunction = null, nextFunction = null;
            while(g.hasNext()) {
                Variable[] next = g.next().toArray(new Variable[0]);
                nextFunction = remaining.summarize(next);
                double gain = getGain(nextFunction);
                if (gain > best) {
                    bestFunction = nextFunction;
                    best = gain;
                }
            }
            
            // Add it to the message, and update the remaining
            if (best > 0) {
                msg.addFactor(bestFunction);
                remaining = remaining.combine(bestFunction.negate());
            }
        } while (best > 0);

        return msg;
    }

    protected abstract double getGain(CostFunction f);

}
