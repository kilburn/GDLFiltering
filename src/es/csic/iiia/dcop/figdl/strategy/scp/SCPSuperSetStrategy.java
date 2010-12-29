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

package es.csic.iiia.dcop.figdl.strategy.scp;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.figdl.FIGdlMessage;
import es.csic.iiia.dcop.figdl.strategy.ApproximationStrategy;
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
public class SCPSuperSetStrategy extends ApproximationStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    private boolean first;

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
        first = true;
    }

    private void initializePartitions(
            UPEdge<? extends IUPNode, FIGdlMessage> e,
            ArrayList<ArrayList<CostFunction>> partitions,
            ArrayList<Collection<Variable>> partitionsVariables)
    {

        // No initialization if there's no bound (so no incoming message either)
        if (Double.isNaN(getBound())) {
            return;
        }
        
        // Extract the base sets of variables from the incoming message
        ArrayList<CostFunction> prevfs = fetchPreviousMessage(e).getFactors();
        for (CostFunction f : prevfs) {
            ArrayList<Variable> partitionVariables =
                    new ArrayList<Variable>(f.getVariableSet());
            partitionsVariables.add(partitionVariables);
            partitions.add(new ArrayList<CostFunction>());
        }

    }

    @Override
    public FIGdlMessage approximate(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, FIGdlMessage> e) {
        // Message to be sent
        FIGdlMessage msg = new FIGdlMessage();
        
        // Partitions is a list of functions that will be sent through the edge
        // (after summarizing to the edge's variables)
        ArrayList<ArrayList<CostFunction>> partitions = new ArrayList<ArrayList<CostFunction>>();

        // PartitionsVariables is a list containing the sets of variables present
        // in the corresponding approximate.
        ArrayList<Collection<Variable>> partitionsVariables = new ArrayList<Collection<Variable>>();

        // Initialize the partitions to the variables of received functions
        initializePartitions(e, partitions, partitionsVariables);

        // Sort the input functions by decreasing arity, randomizing the order
        // of functions with the same arity.
        fs = sortByArityWithRandomness(fs);

        // Iterate over the functions, merging them whenever it's possible
        // or creating a new function when it's not.
        final int r = node.getR();
        log.trace("-- Calculating partitions (r=" + r + ")");
        for (CostFunction inFunction : fs) {
            // Obtain a set of variables in inFunction
            Collection<Variable> variableSet = new HashSet<Variable>(inFunction.getVariableSet());

            // Check if the source function is already bigger than what we
            // can manage.
            while (variableSet.size() > r) {
                // Remove one variable
                Variable v = variableSet.iterator().next();
                variableSet.remove(v);

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

                // Tmp is to avoid editing the original set
                Collection<Variable> tmp = new HashSet<Variable>(partitionVariables);
                tmp.addAll(variableSet);
                //log.trace("\t\t(" + i + ") tmp size: " + tmp.size());
                if (tmp.size() <= r) {

                    if (log.isTraceEnabled()) {
                        log.trace("\tP(" + i + ") += " + inFunction);
                    }

                    partitions.get(i).add(inFunction);
                    partitionsVariables.set(i, tmp);
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
            }
        }

        // Now that we have all the parts, summarize and add them
        log.trace("-- Resulting partitions");
        Collection<Variable> edgeVariables = Arrays.asList(e.getVariables());
        for (int i=0, len=partitions.size(); i<len; i++) {
            if (partitions.get(i).isEmpty()) {
                continue;
            }
            if (log.isTraceEnabled()) {
                log.trace("\t" + partitions.get(i));
            }
            partitionsVariables.get(i).retainAll(edgeVariables);
            final Variable[] vars = partitionsVariables.get(i).toArray(new Variable[0]);
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

    private ArrayList<CostFunction> sortByArityWithRandomness(ArrayList<CostFunction> fs) {
        TreeMap<Integer, ArrayList<CostFunction>> arityMap = new TreeMap<Integer, ArrayList<CostFunction>>();

        for(CostFunction f : fs) {
            final Integer arity = f.getSize();
            ArrayList<CostFunction> fsOfArity = null;
            if (arityMap.containsKey(arity)) {
                fsOfArity = arityMap.get(arity);
            } else {
                fsOfArity = new ArrayList<CostFunction>();
            }
            fsOfArity.add(f);
            arityMap.put(arity, fsOfArity);
        }

        ArrayList<CostFunction> result = new ArrayList<CostFunction>();
        for(Integer key : arityMap.keySet()) {
            ArrayList<CostFunction> fsOfArity = arityMap.get(key);
            //Collections.shuffle(fsOfArity);
            result.addAll(fsOfArity);
        }

        return result;
    }

}
