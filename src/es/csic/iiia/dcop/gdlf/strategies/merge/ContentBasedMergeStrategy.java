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

package es.csic.iiia.dcop.gdlf.strategies.merge;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.util.metrics.Metric;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combines functions by taking into account the resulting partition's arities,
 * but disregarding their contents.
 * 
 * TODO: Track memory using memorytracker.
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class ContentBasedMergeStrategy implements MergeStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(ContentBasedMergeStrategy.class);
    
    private final Metric metric;
    
    /**
     * Build a new content based merger, that uses the given metric to evaluate
     * the gain when merging functions.
     * 
     * @param metric 
     */
    public ContentBasedMergeStrategy(Metric metric) {
        this.metric = metric;
    }
    
    public List<CostFunction> merge(List<CostFunction> messages, 
            Collection<Variable> edgeVariables, int rComputation, 
            int rCommunication)
    {
        List<CostFunction> partitions = merge_recursive(messages, 
                edgeVariables, rComputation, rCommunication);

        // Logging
        if (log.isTraceEnabled()) {
            log.trace("-- Resulting partitions");
            for (int i=0, len=partitions.size(); i<len; i++) {
                log.trace("\t" + partitions.get(i));
            }
        }

        return partitions;
    }

    /**
     * Returns the gain (according to an specific metric) obtained by
     * merging f1 and f2.
     *
     * @param merged
     * @param f1
     * @param f2
     * @param edgeVariables
     * @return
     */
    private double getGain(CostFunction merged, CostFunction f1, 
            CostFunction f2, Collection<Variable> edgeVariables)
    {
        Variable[] vars = merged.getSharedVariables(edgeVariables)
                .toArray(new Variable[0]);
        
        // Result of summarizing without combining
        CostFunction sc = f1.summarize(vars).combine(f2.summarize(vars));
        
        // Result of combining and then summarizing
        CostFunction cs = merged.summarize(vars);
        
        // Average gain
        double gain = metric.getValue(cs.combine(sc.negate()));
        gain /= (double)merged.getVariableSet().size();
        
        return gain;
    }
    
    private List<CostFunction> merge_recursive(List<CostFunction> fs,
            Collection<Variable> edgeVariables, int rComputation, int rCommunication)
    {
            final Comparator<Candidate> comparator = new CandidateComparatorMax();

            fs = new ArrayList<CostFunction>(fs);
            List<Candidate> candidates = expand(fs, edgeVariables,
                    rComputation, rCommunication);
            while (!candidates.isEmpty()) {
                Collections.sort(candidates, comparator);
                Candidate chosen = candidates.remove(candidates.size()-1);
                remove_candidates(candidates, chosen);
                expand(fs, chosen, candidates, edgeVariables, rComputation,
                        rCommunication);
            }

            return fs;
    }

    private void expand(List<CostFunction> fs, Candidate chosen,
            List<Candidate> candidates, Collection<Variable> edgeVariables, 
            int rComputation, int rCommunication)
    {
        final CostFunction f2 = chosen.merged;
        for (int i=fs.size()-1; i>=0; i--) {
            final CostFunction f1 = fs.get(i);

            if (f1 == chosen.f1 || f1 == chosen.f2) {
                fs.remove(i);
                continue;
            }

            // Skip combinations where the r-bound is not satisfied
            HashSet<Variable> cvars = new HashSet<Variable>(f1.getVariableSet());
            cvars.addAll(f2.getVariableSet());

            // First-stage bound
            if (cvars.size() > rComputation) {
                continue;
            }

            // Second-stage bound
            cvars.retainAll(edgeVariables);
            if (cvars.size() > rCommunication) {
                continue;
            }

            CostFunction fprime = f1.combine(f2);
            double gain = getGain(fprime, f1, f2, edgeVariables);
            candidates.add(new Candidate(gain, fprime, f1, f2));
        }

        fs.add(f2);
    }

    private void remove_candidates(List<Candidate> candidates, Candidate chosen) {
        for (int i=candidates.size()-1; i>=0; i--) {
            final Candidate c = candidates.get(i);
            if (c.f1 == chosen.f1 || c.f2 == chosen.f1 || c.f1 == chosen.f2 || c.f2 == chosen.f2) {
                candidates.remove(i);
            }
        }
    }

    private ArrayList<Candidate> expand(List<CostFunction> fs,
            Collection<Variable> edgeVariables, int rComputation, int rCommunication)
    {
        ArrayList<Candidate> res = new ArrayList<Candidate>();
        
        for (int i=0; i<fs.size()-1; i++) {
            final CostFunction f1 = fs.get(i);

            for (int j=i+1; j<fs.size(); j++) {
                final CostFunction f2 = fs.get(j);

                // Skip combinations where the bounds are not satisfied
                HashSet<Variable> cvars = new HashSet<Variable>(f1.getVariableSet());
                cvars.addAll(f2.getVariableSet());
                
                // First-stage bound
                if (cvars.size() > rComputation) {
                    continue;
                }
                
                // Second-stage bound
                cvars.retainAll(edgeVariables);
                if (cvars.size() > rCommunication) {
                    continue;
                }

                // Evaluate the gain w.r.t. not merging the functions
                CostFunction fprime = f1.combine(f2);
                double gain = getGain(fprime, f1, f2, edgeVariables);
                res.add(new Candidate(gain, fprime, f1, f2));
            }
        }

        return res;
    }

    private class Candidate {
        public final Double gain;
        public final CostFunction merged;
        public final CostFunction f1;
        public final CostFunction f2;
        public Candidate(Double gain, CostFunction m, CostFunction f1, CostFunction f2) {
            this.gain = gain;
            this.merged = m;
            this.f1 = f1;
            this.f2 = f2;
        }
    }
    
    private class CandidateComparatorMax implements Comparator<Candidate> {
        public int compare(Candidate o1, Candidate o2) {
            return o1.gain.compareTo(o2.gain);
        }
    }
    
}
