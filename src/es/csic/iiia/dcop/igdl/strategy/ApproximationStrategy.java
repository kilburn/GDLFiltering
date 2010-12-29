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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class ApproximationStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    protected IUPNode node;
    
    private double bound;
    
    /**
     * For testing purpose, the optimal bound may be set from "outside"
     * and it will be used instead of the real one calculated by the algorithm.
     */
    private double optimalBound;


    private ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges;


    public ApproximationStrategy() {
        bound = Double.NaN;
        optimalBound = Double.NaN;
        previousEdges = null;
    }

    public void setOptimumValue(double optimum) {
        optimalBound = optimum;
    }
    
    public void initialize(IUPNode node) {
        this.node = node;
    }

    protected abstract IGdlMessage approximate(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e);

    public final IGdlMessage getApproximation(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e)
    {
        // Informational, just for debugging
        if (log.isTraceEnabled()) {
            StringBuilder buf = new StringBuilder();
            int i = e.getVariables().length;
            for (Variable v : e.getVariables()) {
                buf.append(v.getName());
                if (--i != 0) buf.append(",");
            }
            log.trace("-- Edge vars: {" + buf.toString() + "}, Functions:");
            for (CostFunction f : fs) {
                log.trace("\t" + f);
            }
        }

        return approximate(fs, e);
    }


    /*************************************************************************
     * FILTERING HELPERS STUFF...
     *************************************************************************/

    public void setFilteringOptions(double bound,
            ArrayList<UPEdge<FIGdlNode, IGdlMessage>> previousEdges)
    {
        if (Double.isNaN(optimalBound))
            this.bound = bound;
        else
            this.bound = optimalBound;
        this.previousEdges = previousEdges;
    }

    protected double getBound() {
        return bound;
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

        // 1. Every previously incoming factor is used to filter the outgoing factor.
        // 2. Other outgoing factors can *also* aid in filtering.
        ArrayList<CostFunction> outFunctions = msg.getFactors();
        for (int i=0, len=outFunctions.size(); i<len; i++) {
            ArrayList<CostFunction> filterers = new ArrayList<CostFunction>(fs);
            for (int j=0; j<len; j++) {
                if (i!=j) filterers.add(outFunctions.get(j));
            }
            final CostFunction outf = outFunctions.get(i);
            final CostFunction filtered = outf.filter(filterers, bound);
            if (log.isTraceEnabled()) {
                log.trace("Input b:" + bound + " f:" + outf);
                log.trace("Filtered: " + filtered);
            }
            res.addFactor(filtered);
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
    protected final IGdlMessage fetchPreviousMessage(UPEdge<? extends IUPNode, IGdlMessage> e) {
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

    protected final UPEdge<? extends IUPNode, IGdlMessage> fetchPreviousEdge(UPEdge<? extends IUPNode, IGdlMessage> edge) {

        for (UPEdge<? extends IUPNode, IGdlMessage> e : previousEdges) {
            if (e.getNode1() == edge.getNode1() && e.getNode2() == edge.getNode2()) {
                return e;
            }
        }
        System.err.println("Unable to find previous cost function(s)!");
        System.exit(1);

        return null;
    }

}