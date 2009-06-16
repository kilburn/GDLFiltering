/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2009, IIIA-CSIC, Artificial Intelligence Research Institute
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

package es.csic.iiia.iea.ddm.jt;

import es.csic.iiia.iea.ddm.cg.CgEdge;
import es.csic.iiia.iea.ddm.cg.CliqueGraph;
import es.csic.iiia.iea.ddm.cg.CgNode;
import es.csic.iiia.iea.ddm.mp.DefaultGraph;
import java.util.Hashtable;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class JunctionTree extends DefaultGraph<JTNode, JTEdge, JTResults> {

    /**
     * Creates a JT Message passing graph to propagate the variables
     * of the given clique graph.
     *
     * @param cg
     */
    public JunctionTree(CliqueGraph cg) {
        Hashtable<CgNode, JTNode> nodes = new Hashtable<CgNode, JTNode>();

        for(CgNode cn : cg.getNodes()) {
            JTNode jn = new JTNode(cn);
            addNode(jn);
            nodes.put(cn, jn);
        }

        for(CgEdge e : cg.getEdges()) {
            addEdge(new JTEdge(nodes.get(e.getNode1()), nodes.get(e.getNode2()), e));
        }
    }

    @Override
    protected JTResults buildResults() {
        return new JTResults();
    }

}
