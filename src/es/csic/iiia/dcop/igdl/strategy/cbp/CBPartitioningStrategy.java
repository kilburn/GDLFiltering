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

package es.csic.iiia.dcop.igdl.strategy.cbp;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.strategy.ApproximationStrategy;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class CBPartitioningStrategy extends ApproximationStrategy {

    /**
     * Removes from the cvars set all these variables that do not count
     * towards the r-bound.
     * 
     * @param cvars
     * @param evs
     */
    protected abstract void filterVars(HashSet<Variable> cvars, HashSet<Variable> evs);

    /**
     * Returns the gain (according to an specific metric) obtained by
     * merging f1 and f2.
     *
     * @param merged
     * @param f1
     * @param f2
     * @return
     */
    protected abstract double getGain(CostFunction merged, 
            CostFunction f1, CostFunction f2, Variable[] vars);

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    @Override
    public void initialize(IUPNode node) {
        super.initialize(node);
    }

    @Override
    protected IGdlMessage approximate(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        fs = partition2(fs, e);

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

    private ArrayList<CostFunction> partition2(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {
            final Comparator<Candidate> comparator = new CandidateComparatorMax();

            fs = new ArrayList<CostFunction>(fs);
            ArrayList<Candidate> candidates = expand(fs, e);
            while (!candidates.isEmpty()) {
                Collections.sort(candidates, comparator);
                Candidate chosen = candidates.remove(candidates.size()-1);
                remove_candidates(candidates, chosen);
                expand(fs, chosen, candidates, e);
            }

            return fs;
    }

    private void expand (ArrayList<CostFunction> fs, Candidate chosen,
            ArrayList<Candidate> candidates,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        final int r = node.getR();
        HashSet<Variable> evs = new HashSet<Variable>();
        for (Variable v : e.getVariables()) {
            evs.add(v);
        }

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
            filterVars(cvars, evs);
            //cvars.retainAll(evs);
            if (cvars.size() > r) {
                continue;
            }

            CostFunction fprime = f1.combine(f2);
            double gain = getGain(fprime, f1, f2, e.getVariables());
            candidates.add(new Candidate(gain, fprime, f1, f2));
        }

        fs.add(f2);
    }

    private void remove_candidates(ArrayList<Candidate> candidates, Candidate chosen) {
        for (int i=candidates.size()-1; i>=0; i--) {
            final Candidate c = candidates.get(i);
            if (c.f1 == chosen.f1 || c.f2 == chosen.f1 || c.f1 == chosen.f2 || c.f2 == chosen.f2) {
                candidates.remove(i);
            }
        }
    }

    private ArrayList<Candidate> expand (ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        final int r = node.getR();

        HashSet<Variable> evs = new HashSet<Variable>();
        for (Variable v : e.getVariables()) {
            evs.add(v);
        }

        ArrayList<Candidate> res = new ArrayList<Candidate>();
        for (int i=0; i<fs.size()-1; i++) {
            final CostFunction f1 = fs.get(i);

            for (int j=i+1; j<fs.size(); j++) {
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
                double gain = getGain(fprime, f1, f2, e.getVariables());
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
