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

package es.csic.iiia.dcop.vp.strategy;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.FactorGraph;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.dsa.DSA;
import es.csic.iiia.dcop.dsa.DSAResults;
import es.csic.iiia.dcop.figdl.FIGdlNode;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.VPGraph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class OptimalStrategy extends VPStrategy {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);

    public static int nMappings = 1;

    @Override
    public MappingResults getExtendedMappings(
            ArrayList<VariableAssignment> mappings,
            UPNode upnode
    ){


        int solutionsToTry = 1;
        if (true) {
            if (upnode instanceof FIGdlNode) {
                FIGdlNode finode = (FIGdlNode)upnode;
                int nBrokenLinks = finode.getnBrokenLinks();
                int maxBrokenLinks = finode.getMaxBrokenLinks();
                int remainingSlots = nMappings - mappings.size();

                if (nBrokenLinks == maxBrokenLinks) {

                    // Leaf node!
                    solutionsToTry = remainingSlots;

                } else {

                    double ns = nBrokenLinks/((double)maxBrokenLinks)*remainingSlots;
                    solutionsToTry = (int)ns;
                    ns -= (double)solutionsToTry;
                    if (log.isTraceEnabled()) {
                        log.trace("bl: " + nBrokenLinks + ", cb: " + maxBrokenLinks
                                + ", rs: " + remainingSlots + ", ns: " + ns);
                    }
                    if (Math.random() <= ns) {
                        solutionsToTry++;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Solutions to expand: " + solutionsToTry);
                }
            }
        } else {
            int remainingSlots = nMappings - mappings.size();
            double p = 1;
            for(int i=0; i<remainingSlots; i++) {
                if (Math.random() < p) {
                    solutionsToTry++;
                }
            }
        }

        if (mappings.isEmpty()) {
            mappings.add(new VariableAssignment());
        }

        long time = System.currentTimeMillis();
        AltCalculator c = new AltCalculator(upnode, mappings, solutionsToTry);
        time = System.currentTimeMillis() - time;
        if (time > 100) {
            log.info("End alt calculator: " + time);
        }

        if (log.isTraceEnabled()) {
            log.trace(c.toString());
        }

        return new MappingResults(c.maps, c.upper);
    }

    private class Alt {
        private double cost;
        private VariableAssignment parentAssignment;
        private VariableAssignment assignment;
        private int parentIndex;
        private CostFunction belief;
        private FactorGraph fg;

        public Alt(CostFunction belief, int parentIndex, VariableAssignment parentAssignment) {
            this.parentAssignment = parentAssignment;
            this.parentIndex = parentIndex;
            this.belief = belief;
            this.fg = null;
            this.assignment = null;

            this.assignment = belief.getOptimalConfiguration(null);
            this.assignment.putAll(parentAssignment);
            this.cost = belief.getValue(assignment);
        }

        public Alt(FactorGraph fg, int parentIndex, VariableAssignment parentAssignment) {
            this.parentAssignment = parentAssignment;
            this.parentIndex = parentIndex;
            this.belief = null;
            this.fg = fg;
            this.assignment = null;

            this.runDSA();
        }

        private void runDSA() {
            DSA dsa = new DSA(fg);
            DSAResults res = dsa.run(1000);
            this.assignment = res.getGlobalAssignment();
            this.assignment.putAll(parentAssignment);
            this.cost = fg.getValue(assignment);
        }

        public Alt next() {
            if (fg == null) {
                final int idx = belief.getIndex(assignment);
                final double ng = belief.getFactory().getSummarizeOperation().getNoGood();
                belief.setValue(idx, ng);
                return new Alt(belief, parentIndex, parentAssignment);
            } else {
                return new Alt(fg, parentIndex, parentAssignment);
            }
        }

        public double getCost() {
            return cost;
        }

        public VariableAssignment getAssignment() {
            return assignment;
        }

        public int getParentIndex() {
            return parentIndex;
        }
    }

    private class AltCalculator {
        private TreeSet<Alt> alts = null;
        private ArrayList<VariableAssignment> maps;
        private ArrayList<Integer> upper;

        public AltCalculator(UPNode node,
                ArrayList<VariableAssignment> upMaps, int expand)
        {
            CostFunction.Summarize sum = node.getFactory().getSummarizeOperation();

            maps = new ArrayList<VariableAssignment>();
            upper = new ArrayList<Integer>();

            // Firstly, we need to expand the initial mappings
            long time2 = System.currentTimeMillis();
            int parent=0;
            for (VariableAssignment map : upMaps) {

                long time = System.currentTimeMillis();
                ArrayList<CostFunction> rb = node.getReducedBelief(map);
                time = System.currentTimeMillis() - time;
                Alt alt = null;

                // Sparsity check!
                double sparsity = 0;
                HashSet<Variable> totalVars = new HashSet<Variable>();
                for (CostFunction f : rb) {
                    sparsity = Math.max(sparsity, f.getNumberOfNoGoods()/(double)f.getSize());
                    totalVars.addAll(f.getVariableSet());
                }
                if (time>100) {
                    log.info("Reduce time: " + time + ", " +
                            node.getVariables().size() + " to " + totalVars.size());
                }

                double size = 1; sparsity = 1 - sparsity;
                for (Variable v : totalVars) {
                    size *= v.getDomain();
                }

                if (size * sparsity > 1e4) {
                    
                    log.info("Goodtuples: " + (size*sparsity));
                    CostFunction[] factors = rb.toArray(new CostFunction[0]);
                    FactorGraph fg = new FactorGraph(factors);
                    alt = new Alt(fg, parent, map);

                } else {

                    // Fetch the belief associated to this mapping
                    if (rb.isEmpty()) {
                        System.err.println("Empty belief?!");
                        System.exit(0);
                    }
                    
                    ArrayList<CostFunction> rb2 = new ArrayList<CostFunction>(rb);
                    time = System.currentTimeMillis();
                    CostFunction belief = rb.remove(rb.size()-1).combine(rb);
                    time = System.currentTimeMillis() - time;
                    if (time>100) {
                        log.info("Combine time: " + time);
                        log.info("Thas was merging: ");
                        for(CostFunction f : rb2) {
                            log.info("\t" + f);
                        }
                    }

                    // Compute this alternative
                    time = System.currentTimeMillis();
                    alt = new Alt(belief, parent, map);
                    time = System.currentTimeMillis() - time;
                    if (time>100) {
                        log.info("Optimum extraction time: " + time);
                    }
                }
                maps.add(alt.getAssignment());
                upper.add(parent);

                if (expand > 0) {
                    if (alts == null) {
                        alts = new TreeSet<Alt>(new AltComparator(sum));
                    }
                    alts.add(alt.next());
                }

                parent++;
            }
            time2 = System.currentTimeMillis() - time2;
            if (time2>10) {
                log.info("Completing time: " + time2 + ", "
                        + upMaps.size() + " com., "
                        + expand + " exp. "
                        + (alts != null ? alts.size() + " alts." : ""));
            }

            // And then we need to open new solutions
            time2 = System.currentTimeMillis();
            for (int j=0; j<expand; j++) {
                // Fetch the best alternative
                long time = System.currentTimeMillis();
                Alt alt = alts.first();
                alts.remove(alt);
                time = System.currentTimeMillis() - time;
                if (time>10) {
                    log.info("Fetch-remove time: " + time);
                }

                // Add the corresponding mapping
                time = System.currentTimeMillis();
                if (!maps.contains(alt.getAssignment())) {
                    maps.add(alt.getAssignment());
                    upper.add(alt.getParentIndex());
                }
                time = System.currentTimeMillis() - time;
                if (time>10) {
                    log.info("Check time: " + time);
                }

                // Re-introduce the subsequent alternative
                time = System.currentTimeMillis();
                alts.add(alt.next());
                time = System.currentTimeMillis() - time;
                if (time>10) {
                    log.info("New alt time: " + time);
                }
            }
            time2 = System.currentTimeMillis() - time2;
            if (time2>10) {
                log.info("Expansion time: " + time2);
            }

//            if (expand > 0) {
//                time = System.currentTimeMillis() - time;
//                log.info("End expanding phase: " + time);
//            }

        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("Mappings:\n");
            for(VariableAssignment map : maps) {
                buf.append(map).append("\n");
            }
            return buf.toString();
        }

        private class AltComparator implements Comparator<Alt> {
            private CostFunction.Summarize sum;
            public AltComparator(CostFunction.Summarize sum) {
                this.sum = sum;
            }
            public int compare(Alt t, Alt t1) {
                final double c1 = t.getCost();
                final double c2 = t1.getCost();
                if (sum.isBetter(c1, c2)) {
                    return -1;
                }
                if (sum.isBetter(c2, c1)) {
                    return 1;
                }
                return 0;
            }
        }
    }

}
