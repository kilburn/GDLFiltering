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

package es.csic.iiia.dcop.algo;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.up.UPFactory;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPNode;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class JunctionTreeAlgo {

    public static UPGraph buildGraph(UPFactory factory, CostFunction[][] factors, char[][] adjacency) {

        UPGraph cg = factory.buildGraph();
        UPNode[] nodes = new UPNode[factors.length];

        // Fetch a single relation to get the factor
        CostFunction sample = null;
        for (int i=0; i<factors.length; i++) {
            if (factors[i].length > 0) {
                sample = factors[i][0];
                break;
            }
        }

        // Add the nodes
        for (int i=0; i<factors.length; i++) {

            // If the node's potentital is empty and it only has one link, then
            // we have to add a constant neutral relation as its potential
            if (factors[i].length == 0) {
                int nlinks = 0;
                for (int j=0; j<factors.length; j++) {
                    if (adjacency[i][j] > 0 || adjacency[j][i] > 0) nlinks++;
                }


                if (nlinks < 2) {
                    factors[i] = new CostFunction[]{sample.getFactory().buildNeutralCostFunction(new Variable[0])};
                }
            }

            nodes[i] = factory.buildNode();
            for (CostFunction f : factors[i]) {
                nodes[i].addRelation(f);
            }
            cg.addNode(nodes[i]);
        }

        // And now edges
        for (int i=0; i<adjacency.length; i++) {
            
            for (int j=0; j<adjacency[i].length; j++) {
                if (adjacency[i][j] > 0 && nodes[i] != null && nodes[j] != null) {
                    cg.addEdge(factory.buildEdge(nodes[i], nodes[j]));
                }
            }
        }

        return cg;
    }

}
