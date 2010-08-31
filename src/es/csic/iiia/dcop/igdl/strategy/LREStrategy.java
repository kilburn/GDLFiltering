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
import es.csic.iiia.dcop.util.metrics.Metric;
import es.csic.iiia.dcop.util.metrics.Norm1;
import java.util.ArrayList;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class LREStrategy extends IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    @Override
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

        ArrayList<CostFunction> nfs = nextChild(fs, e);
        while(nfs != null) {
            fs = nfs;
            nfs = nextChild(fs, e);
        }

        // Build the actual message
        log.trace("-- Resulting partitions");
        IGdlMessage msg = new IGdlMessage();
        Variable[] evs = e.getVariables();
        for (int i=0, len=fs.size(); i<len; i++) {
            final CostFunction f = fs.get(i);
            if (log.isTraceEnabled()) {
                log.trace("\t" + f);
            }
            Variable[] svars = f.getSharedVariables(evs).toArray(new Variable[0]);
            final CostFunction fsum = f.summarize(svars);
            msg.addFactor(fsum);
            if (log.isTraceEnabled()) {
                log.trace("\tSummarizes to : " + fsum);
            }
        }

        return filterMessage(e, msg);
    }

    private ArrayList<CostFunction> nextChild(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {
        final int r = node.getR();
        Metric metric = new Norm1();

        HashSet<Variable> evs = new HashSet<Variable>();
        for (Variable v : e.getVariables()) {
            evs.add(v);
        }

        ArrayList<CostFunction> res = null;
        double maxGain = Double.MIN_VALUE;

        for (int i=0; i<fs.size(); i++) {
            final CostFunction f1 = fs.get(i);

            for (int j=i+1; j<fs.size(); j++) {
                // Try each possible combination
                final CostFunction f2 = fs.get(j);

                // Skip combinations where the r-bound is not satisfied
                HashSet<Variable> cvars = new HashSet<Variable>(f1.getVariableSet());
                cvars.addAll(f2.getVariableSet());
                cvars.retainAll(evs);
                if (cvars.size() > r) {
                    continue;
                }

                // Evaluate the gain w.r.t. not merging the functions
                CostFunction fprime = f1.combine(f2);
                double gain = metric.getValue(fprime.combine(f1.negate()));
                gain += metric.getValue(fprime.combine(f2.negate()));
                
                // Compute result size
                gain = gain / fprime.getSize();

                // Check if this child is better, or skip it altogether
                if (gain < maxGain) continue;

                // Actually build the new partition
                maxGain = gain;

                // Create the new child
                res = new ArrayList<CostFunction>(fs.size()-1);
                // Add all other previous partitions except the combined ones
                for (int k=0; k<fs.size(); k++) {
                    if (k==i || k==j) continue;
                    res.add(fs.get(k));
                }
                res.add(fprime);
            }
        }

        return res;
    }

}
