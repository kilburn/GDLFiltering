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

package es.csic.iiia.dcop.dsa;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.FactorGraph;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.mp.DefaultGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class DSA extends DefaultGraph<DSANode, DSAEdge, DSAResults> {

    private static Logger log = LoggerFactory.getLogger(DSA.class);

    private double p = 0.98;
    private Random random = new Random();

    /**
     * Creates a DSA Message passing graph to approximately solve the
     * given factor graph.
     *
     * @param cg
     */
    public DSA(FactorGraph fg) {

        // Variable -> node index map
        Variable[] vars = fg.getVariables();
        HashMap<Variable, Integer> varmap =
                new HashMap<Variable, Integer>(vars.length);

        // Create the nodes first
        for (int i=0; i<vars.length; i++) {
            final Variable v = vars[i];
            DSANode n = new DSANode(v, random, p);
            varmap.put(v, i);
            addNode(n);
        }
        
        ArrayList<DSANode> nodeList = getNodes();

        // node -> set(neighbors) map
        HashMap<Integer, HashSet<Integer>> neighs =
                new HashMap<Integer, HashSet<Integer>>();
        for (CostFunction f : fg.getFactors()) {

            // Compute an index list of all the nodes participating in this
            // factor.
            HashSet<Integer> ns = new HashSet<Integer>();
            for (Variable v : f.getVariableSet()) {
                ns.add(varmap.get(v));
            }

            for (Variable v : f.getVariableSet()) {
                // Add the factor to the corresponding node
                final int node = varmap.get(v);
                nodeList.get(node).addFactor(f);

                // And then all the neighbors
                HashSet<Integer> nns = neighs.get(node);
                if (nns == null) {
                    nns = new HashSet<Integer>(ns);
                } else {
                    nns.addAll(ns);
                }
                neighs.put(node, nns);
            }

        }

        // Add the actual communication edges
        for (int node : neighs.keySet()) {
            final DSANode n1 = nodeList.get(node);

            for (int neigh : neighs.get(node)) {
                // Avoid duplicating edges
                if (node >= neigh) continue;

                final DSANode n2 = nodeList.get(neigh);
                addEdge(new DSAEdge(n1, n2));
            }
        }

    }

    @Override
    protected DSAResults buildResults() {
        return new DSAResults();
    }

    @Override
    protected void end() {
        super.end();
    }

    @Override
    public void reportIteration(int i) {
        log.trace("------- Iter " + i);
    }

    @Override
    public void reportStart() {
        log.trace("\n======= INITIALIZING DSA");
    }

    @Override
    public void reportResults(DSAResults results) {
        if (log.isTraceEnabled()) {
            log.trace("\n======= DSA GRAPH");
            log.trace(this.toString());
        }
    }

}
