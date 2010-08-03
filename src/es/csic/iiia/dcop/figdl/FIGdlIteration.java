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

import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Propagation message passing algorithm implementation using the GDL
 * algorithm as described in the Action-GDL paper.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class FIGdlIteration extends UPGraph<FIGdlNode,UPEdge<FIGdlNode, IGdlMessage>,UPResults> {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    /**
     * Set of variables involved in this graph.
     */
    private HashSet<Variable> variableSet;

    /**
     * Constructs a clique graph that uses the specified {@code EdgeFactory} to
     * create it's edges.
     */
    public FIGdlIteration() {
        super();
        variableSet = new HashSet<Variable>();
    }

    public FIGdlIteration(FIGdlIteration prev) {
        this();

        HashMap<FIGdlNode, FIGdlNode> map = new HashMap<FIGdlNode, FIGdlNode>(
            prev.getNodes().size()
        );

        for(FIGdlNode nold : prev.getNodes()) {
            //FIGdlNode nnew = new FIGdlNode(nold);
            //map.put(nold, nnew);
            //this.addNode(nnew);
        }

        for(UPEdge<FIGdlNode, IGdlMessage> eold : prev.getEdges()) {
            FIGdlNode n1 = map.get(eold.getNode1());
            FIGdlNode n2 = map.get(eold.getNode2());
            UPEdge<FIGdlNode, IGdlMessage> enew = new UPEdge<FIGdlNode, IGdlMessage>(n1, n2);
            enew.setVariables(eold.getVariables());
            this.addEdge(enew);
            n1.addEdge(enew);
            n2.addEdge(enew);
        }
    }

    @Override
    public void addNode(FIGdlNode clique) {
        super.addNode(clique);
        variableSet.addAll(clique.getVariables());
    }

    

    @Override
    protected UPResults buildResults() {
        return new UPResults();
    }

    @Override
    protected void end() {
        super.end();
    }

    public void setR(int r) {
        for(FIGdlNode n : getNodes()) {
            n.setR(r);
        }
    }

    @Override
    public void reportIteration(int i) {
        log.trace("------- Iter " + i);
    }

    @Override
    public void reportStart() {
        log.trace("\n======= PROPAGATING UTILITIES");
    }

    @Override
    public void reportResults(UPResults results) {
        log.trace("------- " + results);
    }

}
