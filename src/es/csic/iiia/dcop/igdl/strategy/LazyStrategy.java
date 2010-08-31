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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class LazyStrategy extends IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    public IGdlMessage getPartition(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        // Informational, just for debugging
        if (log.isTraceEnabled()) {
            StringBuilder buf = new StringBuilder();
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
        IGdlMessage msg = new IGdlMessage();
        
        // Parts is a list of functions that will be sent through the edge
        // (after summarizing to the edge's variable)
        ArrayList<CostFunction> parts = new ArrayList<CostFunction>();

        // Partsev is a list containing the sets of edge variables present
        // in the corresponding part.
        ArrayList<Collection<Variable>> partsev = new ArrayList<Collection<Variable>>();

        // Iterate over the functions, merging them whenever it's possible
        // or creating a new function when it's not.
        final int r = node.getR();
        log.trace("-- Calculating partitions (r=" + r + ")");
        for (CostFunction inFunction : fs) {
            // Obtain a set of edge variables in inFunction
            Collection<Variable> sev = inFunction.getSharedVariables(e.getVariables());

            // Check if the source function is already bigger than what we
            // can manage.
            while (sev.size() > r) {
                // Remove one variable
                Variable v = sev.iterator().next();
                sev.remove(v);
                Collection<Variable> nfv = new HashSet<Variable>(inFunction.getVariableSet());
                nfv.remove(v);

                if (log.isTraceEnabled()) {
                    log.trace("\tRemoving " + v.getName() + " from " + inFunction);
                }
                inFunction = inFunction.summarize(nfv.toArray(new Variable[0]));
                if (log.isTraceEnabled()) {
                    log.trace("\t-> " + inFunction);
                }
            }

            // Check if there's a suitable existing part where we can merge
            // inFunction
            boolean merged = false;
            
            for (int i=0, len=parts.size(); i<len; i++) {
                final Collection<Variable> partev = partsev.get(i);

                // Tmp is to avoid editing the original set
                Collection<Variable> tmp = new HashSet<Variable>(partev);
                tmp.addAll(sev);
                //log.trace("\t\t(" + i + ") tmp size: " + tmp.size());
                if (tmp.size() <= r) {

                    if (log.isTraceEnabled()) {
                        log.trace("\tP(" + i + ") += " + inFunction);
                    }

                    parts.set(i, parts.get(i).combine(inFunction));
                    msg.cc += parts.get(i).getSize();
                    partsev.set(i, tmp);
                    merged = true;
                    break;
                }

            }

            // If the function could not be merged, create a new part
            if (!merged) {

                if (log.isTraceEnabled()) {
                    log.trace("\tP(" + parts.size() + ")  = " + inFunction);
                }

                parts.add(inFunction);
                partsev.add(sev);
            }
        }

        // Now that we have all the parts, summarize and add them
        log.trace("-- Resulting partitions");
        for (int i=0, len=parts.size(); i<len; i++) {
            if (log.isTraceEnabled()) {
                log.trace("\t" + parts.get(i));
            }
            final Variable[] vars = partsev.get(i).toArray(new Variable[0]);
            final CostFunction f = parts.get(i).summarize(vars);
            msg.addFactor(f);
            msg.cc += f.getSize();
            if (log.isTraceEnabled()) {
                log.trace("\tSummarizes to : " + f);
            }
        }

        msg = this.filterMessage(e, msg);

        return msg;
    }

}
