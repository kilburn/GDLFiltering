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
import es.csic.iiia.dcop.figdl.FIGdlNode;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    protected IUPNode node;
    
    private double bound;
    private ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges;


    public IGdlPartitionStrategy() {
        bound = Double.NaN;
        previousEdges = null;
    }

    public void setFilteringOptions(double bound,
            ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges)
    {
        this.bound = bound;
        this.previousEdges = previousEdges;
    }

    public void initialize(IUPNode node) {
        this.node = node;
    }

    protected CostFunction filterFactor(UPEdge<? extends IUPNode, IGdlMessage> e,
            CostFunction factor) {

        if (Double.isNaN(bound)) {
            return factor;
        }

        IGdlMessage prev = fetchPreviousMessage(e);
        //ArrayList<CostFunction> fs = prev.getFactors();

        
        CostFunction prevf = null;
        for(CostFunction f : prev.getFactors()) {
            prevf = f.combine(prevf);
        }

        CostFunction res = factor.filter(prevf, bound);
        
        return res;
    }

    protected IGdlMessage filterMessage(UPEdge<? extends IUPNode, IGdlMessage> e,
            IGdlMessage msg) {

        if (Double.isNaN(bound)) {
            return msg;
        }


        IGdlMessage res = new IGdlMessage();
        IGdlMessage prev = fetchPreviousMessage(e);
        ArrayList<CostFunction> fs = prev.getFactors();

        /*
        CostFunction prevf = null;
        for(CostFunction f : prev.getFactors()) {
            prevf = f.combine(prevf);
        }*/

        // Now, every previously incoming factor is used to filter the outgoing
        // factor.
        for (CostFunction outf : msg.getFactors()) {
            if (log.isTraceEnabled()) {
                log.trace("Input b:" + bound + " f:" + outf);
            }
            outf = outf.filter(fs, bound);
            res.addFactor(outf);
        }

        res.cc = msg.cc;
        return res;
    }

    /**
     * Fetch the previous message, used to filter.
     *
     * Caveat:   it may be from the current r-iteration or from the previous one.
     * Solution: fetch from the current r-iteration if possible, or from the
     *           previous otherwise.
     *
     * @param e
     * @return
     */
    private IGdlMessage fetchPreviousMessage(UPEdge<? extends IUPNode, IGdlMessage> e) {
        IGdlMessage prev = e.getMessage(node);
        if (prev == null) {
            // Fetch the "previous" copy of the current edge
            UPEdge<? extends IUPNode, IGdlMessage> pe = fetchPreviousEdge(e);
            prev = pe.getMessage(node);
            if (prev == null) {
                pe.tick();
                prev = pe.getMessage(node);
            }
        }
        return prev;
    }

    private UPEdge<? extends IUPNode, IGdlMessage> fetchPreviousEdge(UPEdge<? extends IUPNode, IGdlMessage> edge) {

        for (UPEdge<? extends IUPNode, IGdlMessage> e : previousEdges) {
            if (e.getNode1() == edge.getNode1() && e.getNode2() == edge.getNode2()) {
                return e;
            }
        }
        System.err.println("Unable to find previous cost function(s)!");
        System.exit(1);

        return null;
    }




    public abstract IGdlMessage getPartition(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e);

    protected void computeFreeAndBoundFactors(ArrayList<CostFunction> fs,
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

    protected void mergeFreeFactors(ArrayList<CostFunction> ffs, ArrayList<CostFunction> bfs, Variable[] variables) {
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

    protected CostFunction getMostSuitable(CostFunction ff, ArrayList<CostFunction> bfs, Variable[] variables) {
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

    protected int getSuitability(CostFunction ff, CostFunction bf, Variable[] variables) {
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
}
