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

package es.csic.iiia.dcop.util;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class KWayPartitioner {

    private int k;
    private List<Node> nodes;
    private Set<Variable> evs;

    public KWayPartitioner(List<CostFunction> fs, Set<Variable> evs, int k) {
        this.k = k;
        this.evs = evs;
        this.nodes = new ArrayList<Node>(fs.size());

        // Create one node for each function
        // ... and compute the appearing edge variables
        for (CostFunction f : fs) {
            nodes.add(new Node(f));
        }

        // Now create the edges
        for (int i=0, len=nodes.size(); i<len-1; i++) {
            Node n1 = nodes.get(i);
            for (int j=i+1; j<len; j++) {
                Node n2 = nodes.get(j);
                Set<Variable> vs = new HashSet(n1.cf.getVariableSet());
                vs.retainAll(n2.cf.getVariableSet());
                vs.removeAll(evs);
                //if (vs.size() > 0) {
                    double w = CostFunctionStats.getGain(n1.cf.combine(n2.cf),
                            vs.toArray(new Variable[0]));
                    Edge e = new Edge(n1, n2, w);
                    n1.edges.add(e);
                    n2.edges.add(e);
                //}
            }
        }

    }

    public IGdlMessage getPartitions() {

        IGdlMessage m = new IGdlMessage();
        CostFunction belief = null;

        Set<Node> candidates = new HashSet<Node>();
        Set<Node> remaining = new HashSet<Node>(nodes);

        while(remaining.size() > 0) {
            List<Node> p = new ArrayList<Node>();
            Node n = chooseCandidate(p, remaining);

            while (n != null) {
                p.add(n);
                remaining.remove(n);
                updateCandidates(p, candidates, remaining, n);
                n = chooseCandidate(p, remaining);
            }
            
            // Build and send the resulting factor
            CostFunction f = null;
            for (Node n2 : p) {
                f = n2.cf.combine(f);
            }
            HashSet<Variable> fevs = new HashSet<Variable>(f.getVariableSet());
            fevs.retainAll(evs);
            f = f.summarize(fevs.toArray(new Variable[0]));
            m.addFactor(f);
            
            // Track the belief
            belief = f.combine(belief);
        }

        m.setBelief(belief);
        return m;
    }

    private Node chooseCandidate(List<Node> partition, Set<Node> candidates) {
        Node sel = null;
        double minw = Double.POSITIVE_INFINITY;

        //System.out.println("Round, " + candidates.size());
        for (Node n : candidates) {
            double w = Diff(n, partition);
            //System.out.println("Diff: " + w);
            if (w < minw) {
                minw = w;
                sel = n;
            }
        }

        candidates.remove(sel);
        return sel;
    }

    private void updateCandidates(List<Node> p, Set<Node> c, Set<Node> r,  Node n) {
        // Add remaining n's neighbors as candidates
        ArrayList<Node> neighs = new ArrayList<Node>();
        for (Edge e : n.edges) {
            Node n2 = e.Neigh(n);
            if (r.contains(n2)) {
                neighs.add(n2);
            }
        }
        c.addAll(neighs);
    }

    private double Int(Node n, List<Node> p) {
        double res = 0;
        for (Edge e : n.edges) {
            Node n2 = e.Neigh(n);
            if (p.contains(n2)) {
                res += e.w;
            }
        }
        //System.out.println("Int: " + res);
        return res;
    }

    private double Ext(Node n, List<Node> p) {
        double res = 0;
        for (Edge e : n.edges) {
            Node n2 = e.Neigh(n);
            if (!p.contains(n2)) {
                res += e.w;
            }
        }
        //System.out.println("Ext: " + res);
        return res;
    }

    private double Diff(Node i, List<Node> p) {

        // Prevent adding a cf such that the new number of edge variables
        // is higher than k.
        Set<Variable> vs = new HashSet<Variable>(i.cf.getVariableSet());
        for (Node n : p) {
            vs.addAll(n.cf.getVariableSet());
        }
        vs.retainAll(evs);
        if (vs.size() > k) {
            return Double.POSITIVE_INFINITY;
        }

        return -Ext(i,p)-Int(i,p);
    }

    class Node {
        public CostFunction cf;
        public List<Edge> edges;
        public Node(CostFunction f) {
            cf = f;
            edges = new ArrayList<Edge>();
        }
        @Override
        public String toString() {return "N" + cf.toString();}
    }

    class Edge {
        private Node n1;
        private Node n2;
        public double w;
        public Edge(Node n1, Node n2, double w) {
            this.n1 = n1;
            this.n2 = n2;
            this.w = w;
        }
        public Node Neigh(Node n) {
            if (n == n1)
                return n2;
            else if (n == n2)
                return n1;
            else
                throw new RuntimeException("Invalid origin node!");
        }
    }

}
