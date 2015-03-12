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

package es.csic.iiia.dcop.vp;

import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.mp.DefaultGraph;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value Propagation graph.
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class VPGraph extends DefaultGraph<VPNode,VPEdge,VPResults> {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);

    private Random random = new Random();
    private HashMap<UPNode, VPNode> nodes;
    private int root = -1;
    private VPStrategy strategy;

    public VPGraph(UPGraph cg, VPStrategy strategy) {
        super();
        this.strategy = strategy;
        setMode(Modes.TREE_DOWN);
        buildSpanningTree(cg);
    }

    private void buildSpanningTree(UPGraph cg) {
        // Get the list of nodes and setup the visited node list
        ArrayList<UPNode> remainingNodes = new ArrayList<UPNode>(cg.getNodes());
        HashSet<UPNode> visitedNodes = new HashSet<UPNode>();

        // Add all the nodes
        nodes = new HashMap<UPNode, VPNode>(remainingNodes.size());
        for(UPNode n : remainingNodes) {
            VPNode stn = new VPNode(n);
            stn.setStrategy(strategy);
            addNode(stn);
            nodes.put(n, stn);
        }


        root = cg.getRoot();
        if (root < 0) {
            // It's not a tree, so we just choose a random node as root and the
            // tree will be automatically built.
            root = random.nextInt(remainingNodes.size());

            // Now construct the VPTree from the UPGraph
            visitedNodes.add(remainingNodes.remove(root));

            // Now recursively make random walks until all nodes are in the tree
            walkGraph(visitedNodes, remainingNodes);
        } else {
            // It is already a tree and we have a root node, so walk the graph
            walkTree(remainingNodes.get(root), visitedNodes);
            //System.out.println("Root:" + root + ", node: " + remainingNodes.get(root));
        }
        setRoot(root);
    }

    private void walkTree(UPNode node, HashSet<UPNode> visitedNodes) {

        visitedNodes.add(node);
        
        Collection<UPEdge> destinations = node.getEdges();
        for (UPEdge edge : destinations) {
            // Check that this edge leads to a child (unvisited node)
            UPNode other = edge.getDestination(node);
            if (visitedNodes.contains(other)) {
                continue;
            }

            // If so, add the edge and walk to the child
            VPNode n1 = nodes.get(node);
            VPNode n2 = nodes.get(other);
            addEdge(new VPEdge(n1,n2));
            walkTree(other, visitedNodes);
        }

    }

    private void walkGraph(HashSet<UPNode> visitedNodes, ArrayList<UPNode> remainingNodes) {

        // Ending condition
        if (remainingNodes.isEmpty()) {
            return;
        }

        // Randomly choose a node
        UPNode node = remainingNodes.remove(random.nextInt(remainingNodes.size()));
        
        // Perform an unlooped random walk from node to any location on the tree.
        ArrayList<UPNode> path = new ArrayList<UPNode>();
        UPEdge previousEdge = null;
        while (!visitedNodes.contains(node)) {
            path.add(node);

            // Randomly choose one of his edges
            UPEdge edge;
            ArrayList<UPEdge> destinations;
            do {
                destinations = new ArrayList<UPEdge>(node.getEdges());
                edge = destinations.get(random.nextInt(destinations.size()));
            } while (destinations.size() > 1 && edge == previousEdge);
            previousEdge = edge;

            // See where it takes us
            UPNode dest = edge.getDestination(node);

            // Remove any loops
            if (path.contains(dest)) {
                for(int i=path.size()-1; i>=0; i--) {
                    UPNode prev = path.remove(i);
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
                VPNode n1 = nodes.get(path.get(i-1));
                VPNode n2 = nodes.get(path.get(i));
                addEdge(new VPEdge(n1,n2));
            }
        }
        VPNode n1 = nodes.get(path.get(path.size()-1));
        VPNode n2 = nodes.get(node);
        addEdge(new VPEdge(n1,n2));

        walkGraph(visitedNodes, remainingNodes);
    }

    @Override
    protected VPResults buildResults() {
        return new VPResults();
    }

    @Override
    public void reportIteration(int i) {
        log.trace("------- Iter " + i);
    }

    @Override
    public void reportStart() {
        log.debug("\n======= ACCORDING A GLOBAL SOLUTION");
    }

    @Override
    public void reportResults(VPResults results) {
        if (log.isTraceEnabled()) {
            log.trace("------- " + results);
        }
        if (log.isDebugEnabled()) {
            log.debug(results.toString());
        }
    }

}
