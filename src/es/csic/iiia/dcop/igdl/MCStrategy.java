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

package es.csic.iiia.dcop.igdl;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class MCStrategy implements IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    private IGdlNode node;

    /**
     * Mini-monster
     */
    private EdgeTuples edgeTuples;

    public void initialize(IGdlNode node) {
        this.node = node;

        edgeTuples = new EdgeTuples();
        Collection<UPEdge<IGdlNode, IGdlMessage>> edges = node.getEdges();
        // Initialization: for each edge, create a list of tuples (max size r)
        // of variables that we are going to send.
        for (UPEdge<IGdlNode, IGdlMessage> e : edges) {
            // Fetch this edge's variables
            LinkedList<Variable> evs = new LinkedList<Variable>(node.getVariables());
            final int numEdgeVariables = evs.size();
            // Filter out unused ones
            evs.retainAll(Arrays.asList(e.getVariables()));
            Collections.shuffle(evs);
            // Create floor(nev/r) tuples of size r
            final int r = node.getR();
            final int nv = numEdgeVariables / r + (numEdgeVariables % r > 0 ? 1 : 0);
            for (int i=0; i<nv && evs.size()>0; i++) {
                ArrayList<Variable> vs = new ArrayList<Variable>(r);
                for (int j=0; j<r && evs.size()>0; j++) {
                    vs.add(evs.pop());
                }
                edgeTuples.add(e, vs);
            }
        }
    }

    public IGdlMessage getPartition(UPEdge<IGdlNode, IGdlMessage> e) {

        // List of all functions that would be sent (combined)
        ArrayList<CostFunction> fs = node.getCostFunctions();
        // Free factors, that do *not* contain any separator variable.
        ArrayList<CostFunction> ff = new ArrayList<CostFunction>();
        // Bound factors, that contain at least one separator variable.
        ArrayList<CostFunction> bf = new ArrayList<CostFunction>();
        
        // Remove the factors received through this edge
        if (e.getMessage(node) != null) {
            bf.removeAll(e.getMessage(node).getFactors());
        }

        // For research purposes, calculate the optimal belief
        CostFunction belief = null;
        for (CostFunction f : fs) {
            belief = f.combine(belief);
        }
        IGdlMessage msg = new IGdlMessage();
        msg.setBelief(belief.summarize(e.getVariables()));

        // Obtain separate lists for bound and free factors
        computeFreeAndBoundFactors(fs, ff, bf, e.getVariables());

        // Merge the free factors to bound ones
        mergeFreeFactors(ff, bf, e.getVariables());
        
        ArrayList<ArrayList<Variable>> ts = edgeTuples.getTuples(e);
        CostFunction[] msgcf = new CostFunction[ts.size()];
        for (CostFunction f : bf) {
            Collection<Variable> fvs = f.getVariableSet();
            for (int i=0; i < ts.size(); i++) {
                ArrayList<Variable> tfvs = new ArrayList<Variable>(ts.get(i));
                tfvs.retainAll(fvs);
                if (tfvs.size() > 0) {
                    // This function contains variables in this tuple
                    CostFunction tmp = f.summarize(tfvs.toArray(new Variable[]{}));
                    msgcf[i] = tmp.combine(msgcf[i]);
                    //System.out.println("(" + i + ") f " + f + " sum " + Arrays.toString(tfvs.toArray(new Variable[]{})));
                    //System.out.println("(" + i + ") c " + tmp + " = " + msgcf[i]);
                }
            }
        }
        for (int i=0; i<ts.size(); i++) {
            if (msgcf[i] != null) {
                msg.addFactor(msgcf[i]);
            }
        }
        return msg;
    }

    /* Unused
    private ArrayList<CostFunction> getNegatedReceivedFunctions(UPEdge<IGdlNode, IGdlMessage> e) {
        ArrayList<CostFunction> cfs = new ArrayList<CostFunction>();
        IGdlMessage msg = e.getMessage(node);
        if (msg == null)
            return cfs;
        for (CostFunction f : msg.getFactors()) {
            CostFunction nf = node.getFactory().buildCostFunction(f);
            nf.negate();
            cfs.add(nf);
        }
        return cfs;
    } */

    private void computeFreeAndBoundFactors(ArrayList<CostFunction> fs,
            ArrayList<CostFunction> ffs, ArrayList<CostFunction> bfs,
            Variable[] variables)
    {
        for (CostFunction f : fs) {
            if (f.getSharedVariables(variables).size() == 0) {
                ffs.add(f);
            } else {
                bfs.add(node.getFactory().buildCostFunction(f));
            }
        }

        log.trace("-- Free factors");
        for (CostFunction f : ffs) {
            log.trace(f.toString());
        }
        log.trace("-- Bound factors");
        for (CostFunction f : bfs) {
            log.trace(f.toString());
        }
    }

    private void mergeFreeFactors(ArrayList<CostFunction> ffs, ArrayList<CostFunction> bfs, Variable[] variables) {
        log.trace("-- Merging");
        for (CostFunction ff : ffs) {
            CostFunction bf = getMostSuitable(ff, bfs, variables);
            log.trace("   " + ff + " + " + bf + " = ");
            ff = ff.summarize(bf.getSharedVariables(ff).toArray(new Variable[]{}));
            log.trace(" = " + ff + " + " + bf + " = ");
            CostFunction res = bf.combine(ff);
            log.trace(" = " + res.toString());
            bfs.add(res);
        }

        log.trace("-- New bound factors");
        for (CostFunction f : bfs) {
            log.trace(f.toString());
        }
    }

    private CostFunction getMostSuitable(CostFunction ff, ArrayList<CostFunction> bfs, Variable[] variables) {
        if (bfs.size() == 0) {
            return ff;
        }

        final int nbfs = bfs.size();
        int max_s = -1;
        int max_i = (int) Math.random()*nbfs;
        for (int i=0; i<nbfs; i++) {
            CostFunction bf = bfs.get(i);
            int s = getSuitability(ff, bf, variables);
            if (s > max_s) {
                max_s = s;
                max_i = i;
            }
        }
        return bfs.remove(max_i);
    }

    private int getSuitability(CostFunction ff, CostFunction bf, Variable[] variables) {
        // Check if we can combine these factors
        Set<Variable> cv = new HashSet<Variable>(ff.getVariableSet());
        cv.addAll(bf.getVariableSet());
        if (cv.size() > node.getR()) {
            return -1;
        }

        // Now count the shared variables, checking if they appear in the
        // separator.
        Set<Variable> sv = ff.getSharedVariables(bf);
        int score = sv.size();
        for (Variable v : sv) {
            for (Variable v2 : variables) {
                if (v == v2) {
                    score += 9;
                    break;
                }
            }
        }

        return score;
    }

    private class EdgeTuples {
        private HashMap<UPEdge, ArrayList<ArrayList<Variable>>> edgeToList;

        public EdgeTuples() {
            edgeToList = new HashMap<UPEdge, ArrayList<ArrayList<Variable>>>();
        }

        public void add(UPEdge e, ArrayList<Variable> tuple) {
            ArrayList<ArrayList<Variable>> ets = edgeToList.get(e);
            if (ets == null) {
                ets = new ArrayList<ArrayList<Variable>>();
                edgeToList.put(e, ets);
            }
            ets.add(tuple);
        }

        public ArrayList<ArrayList<Variable>> getTuples(UPEdge e) {
            return edgeToList.get(e);
        }
    }

}
