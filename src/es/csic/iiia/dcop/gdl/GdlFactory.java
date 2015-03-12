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

package es.csic.iiia.dcop.gdl;

import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPFactory;

/**
 * Factory for the Utility Propagation GDL implementation.
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class GdlFactory implements UPFactory<GdlGraph, GdlNode, UPEdge<GdlNode, GdlMessage>,
    UPResult, UPResults> {

    private Modes mode = Modes.GRAPH;

    public GdlFactory() {};

    public GdlGraph buildGraph() {
        return new GdlGraph();
    }

    public GdlNode buildNode() {
        GdlNode n = new GdlNode();
        n.setMode(mode);
        return n;
    }

    public UPEdge<GdlNode, GdlMessage> buildEdge(GdlNode node1, GdlNode node2) {
        return new UPEdge<GdlNode, GdlMessage>(node1, node2);
    }

    public UPResult buildResult(GdlNode node) {
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
