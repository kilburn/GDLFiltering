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

import es.csic.iiia.dcop.vp.strategy.expansion.ExpansionStrategy;
import es.csic.iiia.dcop.vp.strategy.solving.SolvingStrategy;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.gdlf.GdlFNode;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.MappingResults;
import es.csic.iiia.dcop.vp.VPGraph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VPStrategy {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);
    public static int numberOfSolutions = 1;
    
    private ExpansionStrategy expansion;
    private SolvingStrategy solving;

    public VPStrategy(ExpansionStrategy expansion, SolvingStrategy solving) {
        this.expansion = expansion;
        this.solving = solving;
    }

    public MappingResults getExtendedMappings(ArrayList<VariableAssignment> mappings, UPNode upnode) {

        if (mappings.isEmpty()) {
            mappings.add(new VariableAssignment());
        }


        int solutionsToExpand = 0;
        if (upnode instanceof UPNode) {
            UPNode finode = (UPNode)upnode;
            solutionsToExpand = expansion.getNumberOfSolutionsToExpand(mappings, finode);
        }
//        if (solutionsToExpand > 0) {
//            System.out.println("Node " + this + " expanding " + solutionsToExpand + " solutions.");
//        }
        
        SolutionExplorer solExplorer = new SolutionExplorer(upnode, mappings, solutionsToExpand);
        if (log.isTraceEnabled()) {
            log.trace(solExplorer.toString());
        }
        
        MappingResults results = new MappingResults(solExplorer.maps, solExplorer.upper);
        //int expandedSols = results.getMappings().size() - mappings.size();
        //System.out.println("[Info] Node " + upnode + " expanded " + expandedSols + " solutions");

        return results;
    }

    private class SolutionExplorer {
        private PriorityQueue<CandidateSolution> candidates = null;
        private ArrayList<VariableAssignment> maps;
        private ArrayList<Integer> upper;

        public SolutionExplorer(UPNode node,
                ArrayList<VariableAssignment> upMaps, int expand)
        {
            CostFunction.Summarize sum = node.getFactory().getSummarizeOperation();

            maps = new ArrayList<VariableAssignment>();
            upper = new ArrayList<Integer>();

            // Firstly, we need to expand the initial mappings
            int parent=0;
            for (VariableAssignment map : upMaps) {

                ArrayList<CostFunction> rb = node.getReducedBelief(map);
                CandidateSolution candidate = null;
                
                
                candidate = solving.getCandidateSolution(rb, parent, map);
                maps.add(candidate.getAssignment());
                upper.add(candidate.getParentIndex());

                if (expand > 0) {
                    if (candidates == null) {
                        candidates = new PriorityQueue<CandidateSolution>(
                                upMaps.size(), new SolutionComparator(sum)
                        );
                    }
                    final CandidateSolution nextCandidate = candidate.next();
                    if (nextCandidate != null)
                        candidates.add(nextCandidate);
                }

                parent++;
            }

            // And then we need to open new solutions
            for (int j=0; j<expand; j++) {
                if (candidates.isEmpty()) {
                    break;
                }

                // Fetch the best alternative
                CandidateSolution candidate = candidates.poll();

                // Add the corresponding mapping
                if (!maps.contains(candidate.getAssignment())) {
                    maps.add(candidate.getAssignment());
                    upper.add(candidate.getParentIndex());
                }

                // Re-introduce the subsequent alternative
                final CandidateSolution nextCandidate = candidate.next();
                if (nextCandidate != null)
                    candidates.add(nextCandidate);
            }

        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("Mappings:\n");
            for(VariableAssignment map : maps) {
                buf.append(map).append("\n");
            }
            return buf.toString();
        }

        private class SolutionComparator implements Comparator<CandidateSolution> {
            private CostFunction.Summarize sum;
            public SolutionComparator(CostFunction.Summarize sum) {
                this.sum = sum;
            }
            public int compare(CandidateSolution t, CandidateSolution t1) {
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
