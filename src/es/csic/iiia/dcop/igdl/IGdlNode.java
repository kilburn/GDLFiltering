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

import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * GDL algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class IGdlNode extends UPNode<UPEdge<IGdlNode, IGdlMessage>, UPResult> {

    /**
     * Maximum cost function arity
     */
    private int r;

    /**
     * Tolerance to use when comparing the previous and current beliefs.
     */
    private double tolerance = 0.0001;

    /**
     * Cost Functions known by this node.
     */
    private ArrayList<CostFunction> costFunctions;

    /**
     * Mini-monster
     */
    private EdgeTuples edgeTuples;

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public IGdlNode(Variable variable) {
        super(variable);
    }

    /**
     * Constructs a new clique with the specified potential.
     *
     * The potential variables are automatically extracted and added as
     * member variables of this clique.
     *
     * @param potential potential of the clique.
     */
    public IGdlNode(CostFunction potential) {
        super(potential);
    }

    /**
     * Constructs a new empty clique.
     */
    public IGdlNode() {
        super();
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    public void initialize() {
        // Tree-based operation
        setMode(Modes.TREE);
        costFunctions = new ArrayList<CostFunction>();
        edgeTuples = new EdgeTuples();

        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }

        Collection<UPEdge<IGdlNode, IGdlMessage>> edges = getEdges();
        // Initialization: for each edge, create a list of tuples (max size r)
        // of variables that we are going to send.
        for (UPEdge<IGdlNode, IGdlMessage> e : edges) {
            // Fetch this edge's variables
            LinkedList<Variable> evs = new LinkedList<Variable>(variables);
            final int numEdgeVariables = evs.size();
            // Filter out unused ones
            evs.retainAll(Arrays.asList(e.getVariables()));
            Collections.shuffle(evs);
            // Create floor(nev/r) tuples of size r
            final int nv = numEdgeVariables / r + (numEdgeVariables % r > 0 ? 1 : 0);
            for (int i=0; i<nv && evs.size()>0; i++) {
                ArrayList<Variable> vs = new ArrayList<Variable>(r);
                for (int j=0; j<r && evs.size()>0; j++) {
                    vs.add(evs.pop());
                }
                edgeTuples.add(e, vs);
            }
        }

        // Send initial messages
        sendMessages();
    }

    /**
     * Performs one "step" of the GDL algorithm, updating the clique's belief
     * and sending new messages to it's neighboors.
     *
     * @return number of constraint checks consumed.
     */
    public long run() {

        // CC count
        long cc = 0;

        // Rebuild cost function list
        ArrayList<CostFunction> previousCostFunctions = costFunctions;
        costFunctions = new ArrayList<CostFunction>();
        // Populate with our assigned relations
        for (CostFunction f : relations) {
            costFunctions.add(factory.buildCostFunction(f));
        }
        // And the received messages
        Collection<UPEdge<IGdlNode, IGdlMessage>> edges = getEdges();
        for (UPEdge<IGdlNode, IGdlMessage> e : edges) {
            IGdlMessage msg = e.getMessage(this);
            if (msg != null)
                costFunctions.addAll(msg.getFactors());
        }

        // Compute our belief
        // ?

        // Send updated messages
        sendMessages();
        setUpdated(false);
        return cc;
    }

    public UPResult end() {
        return new UPResult(this);
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    void setR(int r) {
        this.r = r;
    }

    @Override
    public CostFunction getBelief() {
        CostFunction belief = null;
        for (CostFunction c : costFunctions) {
            belief = c.combine(belief);
        }
        return belief;
    }

    private void sendMessages() {
        for (UPEdge<IGdlNode, IGdlMessage> e : getEdges()) {
            if (!readyToSend(e)) {
                continue;
            }

            ArrayList<CostFunction> ff = new ArrayList<CostFunction>();
            ArrayList<CostFunction> bf = new ArrayList<CostFunction>();
            computeFreeAndBoundFactors(costFunctions, ff, bf, e.getVariables());
            //bf.addAll(getNegatedReceivedFunctions(e));
            if (e.getMessage(this) != null)
                bf.removeAll(e.getMessage(this).getFactors());
            CostFunction belief = null;
            for (CostFunction f : ff) {
                belief = f.combine(belief);
            }
            for (CostFunction f : bf) {
                belief = f.combine(belief);
            }
            /*
            System.out.println("Free functions:");
            for (CostFunction f : ff) {
                System.out.println("\t" + f);
            }
            System.out.println("Bound functions:");
            for (CostFunction f : bf) {
                System.out.println("\t" + f);
            }
            mergeFreeFactors(ff, bf, e.getVariables());
            System.out.println("Merged functions:");
            for (CostFunction f : bf) {
                System.out.println("\t" + f);
            }
            */

            IGdlMessage msg = new IGdlMessage();
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
            e.sendMessage(this, msg, belief.summarize(e.getVariables()));
        }
    }

    private ArrayList<CostFunction> getNegatedReceivedFunctions(UPEdge<IGdlNode, IGdlMessage> e) {
        ArrayList<CostFunction> cfs = new ArrayList<CostFunction>();
        IGdlMessage msg = e.getMessage(this);
        if (msg == null)
            return cfs;
        for (CostFunction f : msg.getFactors()) {
            CostFunction nf = factory.buildCostFunction(f);
            nf.negate();
            cfs.add(nf);
        }
        return cfs;
    }

    private void computeFreeAndBoundFactors(ArrayList<CostFunction> fs,
            ArrayList<CostFunction> ffs, ArrayList<CostFunction> bfs,
            Variable[] variables)
    {
        for (CostFunction f : fs) {
            if (f.getSharedVariables(variables).size() == 0) {
                ffs.add(f);
            } else {
                bfs.add(factory.buildCostFunction(f));
            }
        }
    }

    private void mergeFreeFactors(ArrayList<CostFunction> ffs, ArrayList<CostFunction> bfs, Variable[] variables) {
        System.out.println("Merging...");
        for (CostFunction ff : ffs) {
            CostFunction bf = getMostSuitable(ff, bfs, variables);
            System.out.println(ff + " + " + bf);
            ff = ff.summarize(bf.getVariableSet().toArray(new Variable[]{}));
            System.out.println("After summarize: " + ff);
            bfs.add(bf.combine(ff));
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
        if (cv.size() > this.r) {
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

    /* Never called because we never operate in graph mode */
    @Override
    public boolean isConverged() {
        return false;
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
