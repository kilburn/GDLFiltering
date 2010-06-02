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
import es.csic.iiia.dcop.igdl.IGdlDedupMessage;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.IGdlNode;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.CostFunctionStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class RefinedExpStrategy extends IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    private IGdlPartitionStrategy strategy;

    @Override
    public void initialize(IUPNode node) {
        strategy = new LazyStrategy();
        strategy.initialize(node);
        super.initialize(node);
    }

    public IGdlMessage getPartition(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        // Informational, just for debugging
        if (log.isTraceEnabled()) {
            StringBuffer buf = new StringBuffer();
            int i = e.getVariables().length;
            for (Variable v : e.getVariables()) {
                buf.append(v.getName());
                if (--i != 0) buf.append(",");
            }
            log.trace("-- Edge vars: {" + buf.toString() + "}, Functions:");
            for (CostFunction f : fs) {
                log.trace("\t" + f);
            }
        }
        
        // Message to be sent
        IGdlMessage msg = new IGdlDedupMessage();

        // Separate functions containing only separator variables, and calculate
        // the belief
        CostFunction remaining = null;
        Set<Variable> vs = new HashSet<Variable>(Arrays.asList(e.getVariables()));
        for (int i=fs.size()-1; i>=0; i--) {
            final CostFunction f = fs.get(i);
            if (vs.containsAll(f.getVariableSet())) {
                log.trace("\t Contained: " + f);
                msg.addFactor(f);
            } else {
                fs.remove(i);
                remaining = f.combine(remaining);
            }
        }

        // Obtain the best approximation
        if (remaining != null) {
            remaining = remaining.summarize(e.getVariables());
            CostFunction res[] = CostFunctionStats.getVotedBestApproximation(remaining, node.getR(), 1000);
            for (int i=0; i<res.length-1; i++) {
                msg.addFactor(res[i]);
            }
        }

        // Test even further merging
        //msg = strategy.getPartition(msg.getFactors(), e);
        //msg.setBelief(belief);

        return msg;
    }

}
