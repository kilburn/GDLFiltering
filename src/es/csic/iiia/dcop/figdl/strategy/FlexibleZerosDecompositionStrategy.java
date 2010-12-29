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

package es.csic.iiia.dcop.figdl.strategy;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.figdl.FIGdlMessage;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.CostFunctionStats;
import es.csic.iiia.dcop.util.metrics.Metric;
import es.csic.iiia.dcop.util.metrics.Norm0;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class FlexibleZerosDecompositionStrategy extends ApproximationStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    private Metric informationLossNorm = new Norm0();

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    @Override
    protected FIGdlMessage approximate(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, FIGdlMessage> e) {

        long cc = 0;

        // Message to be sent
        FIGdlMessage msg = new FIGdlMessage();

        // Tracking of broken variable links
        int nBrokenLinks = 0;

        // Partitions is a list of functions that will be sent through the edge
        // (after summarizing to the edge's variables)
        ArrayList<ArrayList<CostFunction>> partitions = new ArrayList<ArrayList<CostFunction>>();

        // PartitionsVariables is a list containing the sets of variables present
        // in the corresponding approximate.
        ArrayList<Collection<Variable>> partitionsVariables = new ArrayList<Collection<Variable>>();

        // Sort the input functions by decreasing arity, randomizing the order
        // of functions with the same arity.
        //fs = sortByArityWithRandomness(fs);

        // Iterate over the functions, merging them whenever it's possible
        // or creating a new function when it's not.
        final int s = node.getS();
        log.trace("-- Calculating partitions (s=" + s + ")");
        for (CostFunction inFunction : fs) {
            // Obtain a set of variables in inFunction
            Collection<Variable> variableSet = new HashSet<Variable>(inFunction.getVariableSet());

            // Check if the source function is already bigger than what we
            // can manage.
            while (variableSet.size() > s) {
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

                nBrokenLinks++;
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
                if (tmp.size() <= s) {

                    if (log.isTraceEnabled()) {
                        log.trace("\tP(" + i + ") += " + inFunction);
                    }

                    partitions.get(i).add(inFunction);
                    partitionsVariables.set(i, tmp);
                    merged = true;
                    break;
                } else {
                    tmp = new HashSet<Variable>(partitionVariables);
                    tmp.retainAll(variableSet);
                    nBrokenLinks += tmp.size();
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
        msg.setInformationLoss(nBrokenLinks);

        // Now that we have all the parts, summarize, decompose and add them
        log.trace("-- Resulting partitions");
        double informationLoss = 0;
        Collection<Variable> edgeVariables = Arrays.asList(e.getVariables());
        for (int i=0, len=partitions.size(); i<len; i++) {

            if (log.isTraceEnabled()) {
                log.trace("\t" + partitions.get(i));
            }

            // Summarize part
            partitionsVariables.get(i).retainAll(edgeVariables);
            final Variable[] vars = partitionsVariables.get(i).toArray(new Variable[0]);
            final ArrayList<CostFunction> partition = partitions.get(i);
            CostFunction f = partition.remove(partition.size()-1).combine(partition).summarize(vars);

            // Don't try to break a fitting factor into smaller pieces
            if (f.getVariableSet().size() <= node.getR()) {
                msg.addFactor(f);
                continue;
            }

            // Now, the factor f must be decomposed as functions of "r" variables
            msg.cc += f.getSize();
            
            // Remove the constant value (summarization to no variables)
            CostFunction cst = f.summarize(new Variable[0]);
            msg.addFactor(cst);
            f = f.combine(cst.negate());
            msg.cc += f.getSize();

            // Obtain the projection approximation
            CostFunction[] res =
                    CostFunctionStats.getZeroDecompositionApproximation(f, node.getR());
            for (int j=0; j<res.length-1; j++) {
                msg.addFactor(res[j]);
            }
            informationLoss += informationLossNorm.getValue(res[res.length-1]);

            if (log.isTraceEnabled()) {
                log.trace("\tSummarizes to : " + f);
            }
        }
        msg = filterMessage(e, msg);

        // And the total information lost
        msg.setInformationLoss(informationLoss);

        return msg;
    }

}