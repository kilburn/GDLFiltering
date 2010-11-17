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
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.figdl.FIGdlNode;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.VPGraph;
import java.util.ArrayList;
import java.util.Comparator;
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
                if (log.isDebugEnabled()) {
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

        if (mappings.isEmpty()) {
            mappings.add(new VariableAssignment());
        }

//        long time = System.currentTimeMillis();
        AltCalculator c = new AltCalculator(upnode, mappings, solutionsToTry);
//        time = System.currentTimeMillis() - time;
//        System.out.println("End alt calculator: " + time);

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

        public Alt(CostFunction belief, int parentIndex, VariableAssignment parentAssignment) {
            this.parentAssignment = parentAssignment;
            this.parentIndex = parentIndex;
            this.belief = belief;
            this.assignment = belief.getOptimalConfiguration(null);
            this.assignment.putAll(parentAssignment);
            this.cost = belief.getValue(assignment);
        }

        public Alt next() {
            final int idx = belief.getIndex(assignment);
            final double ng = belief.getFactory().getSummarizeOperation().getNoGood();
            belief.setValue(idx, ng);
            return new Alt(belief, parentIndex, parentAssignment);
        }

        public double getCost() {
            return cost;
        }

        public VariableAssignment getAssignment() {
            return assignment;
        }

        public int getParent() {
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
            maps = new ArrayList<VariableAssignment>();
            upper = new ArrayList<Integer>();

            // Firstly, we need to expand the initial mappings
            int parent=0;
            for (VariableAssignment map : upMaps) {

                // Fetch the belief associated to this mapping
                long time = System.currentTimeMillis(), time2 = time;
                ArrayList<CostFunction> rb = node.getReducedBelief(map);
//                time2 = System.currentTimeMillis() - time2;
                if (rb.isEmpty()) {
                    System.err.println("Empty belief?!");
                    System.exit(0);
                }
                CostFunction belief = rb.remove(rb.size()-1).combine(rb);
//                time = System.currentTimeMillis() - time;
//                if (time > 80) {
//                    System.out.println("Map: " + map);
//                    rb = node.getReducedBelief(map);
//                    for (CostFunction f : rb) {
//                        System.out.println(f);
//                    }
//                    System.out.println("End calculating reduced belief: " + time + ", " + time2);
//                }

                // Compute this alternative
                time = System.currentTimeMillis();
                Alt alt = new Alt(belief, parent, map);
                maps.add(alt.getAssignment());
                upper.add(parent);

                if (expand > 0) {
                    if (alts == null) {
                        alts = new TreeSet<Alt>(new AltComparator(belief.getFactory().getSummarizeOperation()));
                    }
                    alts.add(alt.next());
//                    time = System.currentTimeMillis() - time;
//                    if (time > 80) {
//                        System.out.println("End calculating alternative: " + time);
//                    }
                }

                parent++;
            }

//            if (expand > 0) {
//                System.out.println("Solutions to expand: " + expand);
//            }
//
//            long time = System.currentTimeMillis();
            // And then we need to open new solutions
            for (int j=0; j<expand; j++) {

                // Fetch the best alternative
                Alt alt = alts.first();
                alts.remove(alt);

                // Add the corresponding mapping
                maps.add(alt.getAssignment());
                upper.add(alt.getParent());

                // Re-introduce the subsequent alternative
                alts.add(alt.next());
            }

//            if (expand > 0) {
//                time = System.currentTimeMillis() - time;
//                System.out.println("End expanding phase: " + time);
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
