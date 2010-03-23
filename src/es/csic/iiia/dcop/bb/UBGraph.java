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

package es.csic.iiia.dcop.bb;

import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.mp.DefaultGraph;
import es.csic.iiia.dcop.vp.VPEdge;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.VPNode;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class UBGraph extends DefaultGraph<UBNode, UBEdge, UBResults> {

    private static Logger log = LoggerFactory.getLogger(UBGraph.class);

    public UBGraph(VPGraph vg) {
        setMode(Modes.TREE_UP);
        HashMap<VPNode, UBNode> nodes = new HashMap<VPNode, UBNode>();

        // Clone the nodes
        for (VPNode vpn : vg.getNodes()) {
            UBNode ubn = new UBNode(vpn);
            addNode(ubn);
            nodes.put(vpn, ubn);
        }

        // And the edges
        for (VPEdge vpe : vg.getEdges()) {
            final UBNode n1 = nodes.get(vpe.getNode1());
            final UBNode n2 = nodes.get(vpe.getNode2());
            UBEdge ube = new UBEdge(n1, n2);
            addEdge(ube);
        }

        // And the root (necessary? I don't think so, but not harmful either)
        setRoot(vg.getRoot());
    }

    @Override
    protected UBResults buildResults() {
        return new UBResults();
    }

    @Override
    public void reportIteration(int i) {
        log.trace("============== Iter " + i + " ===============");
    }

}
