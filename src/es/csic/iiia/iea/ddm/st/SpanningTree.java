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

package es.csic.iiia.iea.ddm.st;

import es.csic.iiia.iea.ddm.cg.CgEdge;
import es.csic.iiia.iea.ddm.cg.CliqueGraph;
import es.csic.iiia.iea.ddm.cg.CgNode;
import es.csic.iiia.iea.ddm.mp.AbstractTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class SpanningTree extends AbstractTree<StNode,StEdge,StResults> {

    private Random random = new Random();
    private Hashtable<CgNode, StNode> nodes;

    public SpanningTree(CliqueGraph cg) {
        super();
        buildRandomSpanningTree(cg);
    }

    private void buildRandomSpanningTree(CliqueGraph cg) {
        // Get the list of nodes and setup the visited node list
        ArrayList<CgNode> remainingNodes = new ArrayList<CgNode>(cg.getNodes());
        HashSet<CgNode> visitedNodes = new HashSet<CgNode>();

        // Add all the nodes
        nodes = new Hashtable<CgNode, StNode>(remainingNodes.size());
        for(CgNode n : remainingNodes) {
            StNode stn = new StNode(n);
            addNode(stn);
            nodes.put(n, stn);
        }

        // Choose the first one randomly
        visitedNodes.add(remainingNodes.remove(random.nextInt(remainingNodes.size())));

        // Now recursively make random walks until all nodes are in the tree
        walkTree(visitedNodes, remainingNodes);
    }

    private void walkTree(HashSet<CgNode> visitedNodes, ArrayList<CgNode> remainingNodes) {

        // Ending condition
        if (remainingNodes.size() == 0) {
            return;
        }

        // Randomly choose a node
        CgNode node = remainingNodes.remove(random.nextInt(remainingNodes.size()));
        
        // Perform an unlooped random walk from node to any location on the tree.
        ArrayList<CgNode> path = new ArrayList<CgNode>();
        CgEdge previousEdge = null;
        while (!visitedNodes.contains(node)) {
            path.add(node);

            // Randomly choose one of his edges
            CgEdge edge;
            ArrayList<CgEdge> destinations;
            do {
                destinations = new ArrayList<CgEdge>(node.getEdges());
                edge = destinations.get(random.nextInt(destinations.size()));
            } while (destinations.size() > 1 && edge == previousEdge);
            previousEdge = edge;

            // See where it takes us
            CgNode dest = edge.getDestination(node);

            // Remove any loops
            if (path.contains(dest)) {
                for(int i=path.size()-1; i>=0; i--) {
                    CgNode prev = path.remove(i);
                    if (prev == dest) {
                        break;
                    }
                }
            }
            
            node = dest;
        }

        // Ok, we have a path from an unplaced node to the tree, so create
        // the corresponding edges
        for(int i=0;i<path.size(); i++) {
            visitedNodes.add(path.get(i));
            remainingNodes.remove(path.get(i));
            if (i>0) {
                StNode n1 = nodes.get(path.get(i-1));
                StNode n2 = nodes.get(path.get(i));
                addEdge(new StEdge(n1,n2));
            }
        }
        StNode n1 = nodes.get(path.get(path.size()-1));
        StNode n2 = nodes.get(node);
        addEdge(new StEdge(n1,n2));

        walkTree(visitedNodes, remainingNodes);
    }

    @Override
    protected StResults buildResults() {
        return new StResults();
    }

}
