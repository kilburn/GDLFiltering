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

package es.csic.iiia.dcop.figdl;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.figdl.strategy.ApproximationStrategy;
import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for the Utility Propagation GDL implementation.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class FIGdlFactory implements UPFactory<FIGdlGraph, FIGdlNode, UPEdge<FIGdlNode, FIGdlMessage>,
    UPResult, UPResults> {

    private Modes mode = Modes.TREE_UP;
    private int r = Integer.MAX_VALUE;
    private ApproximationStrategy approximationStrategy;
    private CostFunction constant;
    private boolean inverted;

    public FIGdlFactory(CostFunction constant, boolean inverted, int r, ApproximationStrategy approximationStrategy) {
        this.r = r;
        this.approximationStrategy = approximationStrategy;
        this.constant = constant;
        this.inverted = inverted;
    }

    public FIGdlGraph buildGraph() {
        FIGdlGraph g = new FIGdlGraph(constant, inverted);
        g.setMaxR(r);
        return g;
    }

    public FIGdlNode buildNode() {
        FIGdlNode n = new FIGdlNode();
        n.setMode(mode);
        n.setR(2);
        try {
            n.setApproximationStrategy(approximationStrategy.getClass().newInstance());
        } catch (InstantiationException ex) {
            Logger.getLogger(FIGdlFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FIGdlFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return n;
    }

    public UPEdge<FIGdlNode, FIGdlMessage> buildEdge(FIGdlNode node1, FIGdlNode node2) {
        return new UPEdge<FIGdlNode, FIGdlMessage>(node1, node2);
    }

    public UPResult buildResult(FIGdlNode node) {
        return new UPResult(node);
    }

    public UPResults buildResults() {
        return new UPResults();
    }

    /**
     * @return the mode
     */
    public Modes getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(Modes mode) {
        this.mode = mode;
    }

}
