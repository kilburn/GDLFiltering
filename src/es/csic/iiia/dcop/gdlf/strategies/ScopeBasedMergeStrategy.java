/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2011, IIIA-CSIC, Artificial Intelligence Research Institute
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

package es.csic.iiia.dcop.gdlf.strategies;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class ScopeBasedMergeStrategy implements MergeStrategy {
    
    private static Logger log = LoggerFactory.getLogger(ScopeBasedMergeStrategy.class);

    @Override
    public List<CostFunction> merge(
            List<CostFunction> fs,
            Collection<Variable> edgeVariables, 
            int rComputation, int rCommunication
    ) {

        // Partitions is a list of functions that will be output
        // (after summarizing to the edge's variables)
        ArrayList<ArrayList<CostFunction>> partitions = new ArrayList<ArrayList<CostFunction>>();

        // PartitionsVariables is a list containing the sets of variables present
        // in the corresponding approximate.
        ArrayList<Collection<Variable>> partitionsVariables = new ArrayList<Collection<Variable>>();
        ArrayList<Collection<Variable>> partitionsEdgeVariables = new ArrayList<Collection<Variable>>();
        
        log.trace("-- Calculating partitions (rComp=" + rComputation + ", rComm=" + rCommunication + ")");
        for (CostFunction inFunction : fs) {
            
            // Obtain a set of variables in inFunction
            Set<Variable> variableSet     = new HashSet<Variable>(inFunction.getVariableSet());
            Set<Variable> edgeVariableSet = inFunction.getSharedVariables(edgeVariables);

            // Check if the source function is already bigger than what we can manage
            // computationally.
            while (variableSet.size() > rComputation) {
                // Remove one variable
                Variable v = variableSet.iterator().next();
                variableSet.remove(v);
                edgeVariableSet.remove(v);

                if (log.isTraceEnabled()) {
                    log.trace("\tRemoving " + v.getName() + " from " + inFunction);
                }
                
                inFunction = inFunction.summarize(variableSet.toArray(new Variable[0]));
                if (log.isTraceEnabled()) {
                    log.trace("\t-> " + inFunction);
                }
            }
            
            // Check if the source function is already bigger than we can manage
            // in the communication front.
            while (edgeVariableSet.size() > rCommunication) {
                // Remove one variable
                Variable v = edgeVariableSet.iterator().next();
                variableSet.remove(v);
                edgeVariableSet.remove(v);

                if (log.isTraceEnabled()) {
                    log.trace("\tRemoving " + v.getName() + " from " + inFunction);
                }
                
                inFunction = inFunction.summarize(variableSet.toArray(new Variable[0]));
                if (log.isTraceEnabled()) {
                    log.trace("\t-> " + inFunction);
                }
            }

            // Check if there's a suitable existing part where we can merge
            // inFunction
            boolean merged = false;
            
            for (int i=0, len=partitions.size(); i<len; i++) {
                final Collection<Variable> partitionVariables = partitionsVariables.get(i);
                final Collection<Variable> partitionEdgeVariables = partitionsEdgeVariables.get(i);

                // Tmp is to avoid editing the original set
                Collection<Variable> tmp = new HashSet<Variable>(partitionVariables);
                Collection<Variable> edgeTmp = new HashSet<Variable>(partitionEdgeVariables);
                tmp.addAll(variableSet);
                edgeTmp.addAll(edgeVariableSet);
                
                //log.trace("\t\t(" + i + ") tmp size: " + tmp.size());
                if (tmp.size() <= rComputation && edgeTmp.size() <= rCommunication) {

                    if (log.isTraceEnabled()) {
                        log.trace("\tP(" + i + ") += " + inFunction);
                    }

                    partitions.get(i).add(inFunction);
                    partitionsVariables.set(i, tmp);
                    partitionsEdgeVariables.set(i, edgeTmp);
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
                partitionsVariables.add(variableSet);
                partitionsEdgeVariables.add(edgeVariableSet);
            }
        }
        
        // And return a list after combining the functions inside each partition
        // Now that we have all the parts, summarize and add them
        ArrayList<CostFunction> result = new ArrayList<CostFunction>();
        log.trace("-- Resulting partitions");
        for (int i=0, len=partitions.size(); i<len; i++) {
            if (log.isTraceEnabled()) {
                log.trace("\t" + partitions.get(i));
            }
            final ArrayList<CostFunction> partition = partitions.get(i);
            final CostFunction f = partition.remove(partition.size()-1).combine(partition);
            result.add(f);
        }
        
        return result;
    }

}
