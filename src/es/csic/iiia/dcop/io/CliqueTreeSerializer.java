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

package es.csic.iiia.dcop.io;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class CliqueTreeSerializer {

    private HashMap<UPNode, Integer> nodeIndexes;
    private StringBuilder buf;
    
    public String serializeTreeStructure(UPGraph graph) {
        buf = new StringBuilder();
        nodeIndexes = new HashMap<UPNode, Integer>();
        int i = 0;
        ArrayList<UPNode> nodes = graph.getNodes();
        for (UPNode n : nodes) {
            buf.append("NODE n" + i + "\n");
            nodeIndexes.put(n, i);
            ArrayList<CostFunction> rs = n.getRelations();
            for (CostFunction f : rs) {
                buf.append("F");
                for (Variable v : f.getVariableSet()) {
                    buf.append(" " + v.getName());
                }
                buf.append("\n");
            }
            i++;
        }

        // DFS graph walking
        int root = graph.getRoot();
        this._dfs_walk(nodes.get(root), null);

        return buf.toString();
    }

    private void _dfs_walk(UPNode node, UPEdge incomingEdge) {
        Collection<UPEdge> edges = node.getEdges();
        for (UPEdge e : edges) {
            if (e == incomingEdge) {
                buf.append("LINK n")
                   .append(nodeIndexes.get(e.getDestination(node)))
                   .append(" n")
                   .append(nodeIndexes.get(node))
                   .append("\n");
            } else {
                this._dfs_walk(e.getDestination(node), e);
            }
        }
    }

}
