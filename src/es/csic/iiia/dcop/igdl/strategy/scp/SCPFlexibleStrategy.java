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

package es.csic.iiia.dcop.igdl.strategy.scp;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.strategy.ApproximationStrategy;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class SCPFlexibleStrategy extends ApproximationStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    @Override
    protected IGdlMessage approximate(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {
        
        // Message to be sent
        IGdlMessage msg = new IGdlMessage();
        
        // Partitions is a list of functions that will be sent through the edge
        // (after summarizing to the edge's variables)
        ArrayList<ArrayList<CostFunction>> partitions = new ArrayList<ArrayList<CostFunction>>();

        // PartitionsVariables is a list containing the sets of variables present
        // in the corresponding approximate.
        ArrayList<Collection<Variable>> partitionsAllVariables = new ArrayList<Collection<Variable>>();
        ArrayList<Collection<Variable>> partitionsEdgeVariables = new ArrayList<Collection<Variable>>();

        // Iterate over the functions, merging them whenever it's possible
        // or creating a new function when it's not.
        final int r = node.getR();
        final int s = node.getS();
        log.trace("-- Calculating partitions (r=" + r + ")");
        for (CostFunction inFunction : fs) {
            // Obtain a set of variables in inFunction
            Collection<Variable> functionAllVariables = new HashSet<Variable>(inFunction.getVariableSet());

            // Check if the source function is already bigger than what we
            // can manage.
            while (functionAllVariables.size() > r) {
                // Remove one variable
                Variable v = functionAllVariables.iterator().next();
                functionAllVariables.remove(v);

                if (log.isTraceEnabled()) {
                    log.trace("\tRemoving " + v.getName() + " from " + inFunction);
                }
                inFunction = inFunction.summarize(functionAllVariables.toArray(new Variable[0]));
                if (log.isTraceEnabled()) {
                    log.trace("\t-> " + inFunction);
                }
            }

            // Check if there's a suitable existing part where we can merge
            // inFunction
            boolean merged = false;

            final Collection<Variable> functionEdgeVariables = new HashSet<Variable>(Arrays.asList(e.getVariables()));
            functionEdgeVariables.retainAll(functionAllVariables);
            
            for (int i=0, len=partitions.size(); i<len; i++) {
                final Collection<Variable> partitionAllVariables  = partitionsAllVariables.get(i);
                final Collection<Variable> partitionEdgeVariables = partitionsAllVariables.get(i);

                // Tmp/tmp2 is to avoid editing the original set
                Collection<Variable> tmp = new HashSet<Variable>(partitionAllVariables);
                tmp.addAll(functionAllVariables);
                Collection<Variable> tmp2 = new HashSet<Variable>(partitionEdgeVariables);
                tmp2.addAll(functionEdgeVariables);

                //log.trace("\t\t(" + i + ") tmp size: " + tmp.size());
                if (tmp.size() <= s && tmp2.size() <= r) {

                    if (log.isTraceEnabled()) {
                        log.trace("\tP(" + i + ") += " + inFunction);
                    }

                    partitions.get(i).add(inFunction);
                    partitionsAllVariables.set(i, tmp);
                    partitionsEdgeVariables.set(i, tmp2);
                    merged = true;
                    break;
                }

            }

            // If the function could not be merged, create a new part
            if (!merged) {

                if (log.isTraceEnabled()) {
                    log.trace("\tP(" + partitions.size() + ")  = " + inFunction);
                }

                ArrayList<CostFunction> newPartition = new ArrayList<CostFunction>();
                newPartition.add(inFunction);
                partitions.add(newPartition);
                partitionsAllVariables.add(functionAllVariables);
                partitionsEdgeVariables.add(functionEdgeVariables);
            }
        }

        // Now that we have all the parts, summarize and add them
        log.trace("-- Resulting partitions");
        Collection<Variable> edgeVariables = Arrays.asList(e.getVariables());
        for (int i=0, len=partitions.size(); i<len; i++) {
            if (log.isTraceEnabled()) {
                log.trace("\t" + partitions.get(i));
            }
            partitionsAllVariables.get(i).retainAll(edgeVariables);
            final Variable[] vars = partitionsAllVariables.get(i).toArray(new Variable[0]);
            final ArrayList<CostFunction> partition = partitions.get(i);
            final CostFunction f = partition.remove(partition.size()-1).combine(partition).summarize(vars);
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
