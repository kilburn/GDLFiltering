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

package es.csic.iiia.dcop.dfs;

import es.csic.iiia.dcop.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class DFS {

    private static Logger log = LoggerFactory.getLogger(DFS.class);

    private CostFunction[] factors;
    private Variable[] variables;
    private HashSet<Variable> remainingVariables;
    private int[] nPlacedNeighs;
    private HashMap<Variable, HashSet<Variable>> neighboors;
    private VariableAssignment variableIndices;
    private VariableAssignment nConnections;
    private Random random;
    private Variable root;
    private char[][] adjacency = null;
    private VariableAssignment variableDepths;
    private int rootIndex = -1;

    public DFS(CostFunction[] factors) {
        this.factors = factors;
        this.initialize();
        random = new Random(System.nanoTime());
    }

    public int getRoot() {
        char[][] newAdjacency = new char[variables.length][variables.length];

        // We will use the node that minimizes tree height
        HashMap<Integer, HashSet<Integer>> unvisitedNodeNeighbors =
                new HashMap<Integer, HashSet<Integer>>();
        HashMap<Integer, HashSet<Integer>> nodeNeighbors =
                new HashMap<Integer, HashSet<Integer>>();

        // Initialize nodeNeighbors map
        for (int i=0; i<variables.length; i++) {
            nodeNeighbors.put(i, new HashSet<Integer>());
        }

        // For each variable...
        for (int i=0; i<variables.length; i++) {
            // Populate the nodeNeighbors map
            for (int j=i+1; j<variables.length; j++) {
                if (adjacency[i][j] > 0 || adjacency[j][i] > 0) {
                    nodeNeighbors.get(i).add(j);
                    nodeNeighbors.get(j).add(i);
                }
            }
        }

        // Now initialize the univisitedNodeNeighbors' buckets
        for (int i=0; i<variables.length; i++) {
            final int neighs = nodeNeighbors.get(i).size();
            HashSet<Integer> bucket = unvisitedNodeNeighbors.get(neighs);
            if (bucket == null) {
                bucket = new HashSet<Integer>();
            }
            bucket.add(i);
            unvisitedNodeNeighbors.put(neighs, bucket);
        }

        for (Integer nN : unvisitedNodeNeighbors.keySet()) {
            log.trace("Bucket " + nN);
        }

        // Now iteratively eliminate nodes from bucket (current iteration leaves)
        HashSet<Integer> bucket = unvisitedNodeNeighbors.remove(1);

        while (!bucket.isEmpty()) {

            if (log.isTraceEnabled())
                log.trace("chosen bucket: " + bucket);
            
            unvisitedNodeNeighbors.put(1, new HashSet<Integer>());
            // For each leaf...
            for (Integer i : bucket) {
                log.trace("Removing var " + i);

                // FALSE! When the selected node has no neighbors, it means that every-
                // one else has been eliminated and it is the root!
                HashSet<Integer> neighbors = nodeNeighbors.get(i);
                if (neighbors.isEmpty()) {
                    rootIndex = i;
                    root = variables[i];
                    if (log.isTraceEnabled()) {
                        log.trace("RootIndex: " + i);
                        log.trace("Root: " + root);
                    }
                    return rootIndex;
                }

                // Remove it from its neighbors list, and decrease the neighbors
                // bucket assignment.
                for (Integer j : neighbors) {

                    int neighs = nodeNeighbors.get(j).size();
                    nodeNeighbors.get(j).remove(i);
                    unvisitedNodeNeighbors.get(neighs).remove(j);
                    if (log.isTraceEnabled())
                        log.trace("updating neigh " + j + ", rem: " + (neighs-1) +
                            ", unvisited: " + nodeNeighbors.get(j));

                    HashSet<Integer> newBucket = unvisitedNodeNeighbors.get(neighs-1);
                    if (newBucket == null) {
                        newBucket = new HashSet<Integer>();
                        unvisitedNodeNeighbors.put(neighs-1, newBucket);
                    }
                    newBucket.add(j);
                }

            }

            bucket = unvisitedNodeNeighbors.remove(1);
            if (bucket == null || bucket.isEmpty()) {
                bucket = unvisitedNodeNeighbors.remove(0);
            }
        }
        
        return rootIndex;
    }

    private void initialize() {
        neighboors = new HashMap<Variable, HashSet<Variable>>();
        nConnections = new VariableAssignment();
        remainingVariables = new HashSet<Variable>();
        for (CostFunction f : factors) {
            Set<Variable> vars = f.getVariableSet();
            remainingVariables.addAll(f.getVariableSet());
            for (Variable v : vars) {
                // Get the previously found neighboors
                HashSet<Variable> vn = neighboors.get(v);
                if (vn == null) {
                    vn = new HashSet<Variable>();
                }
                neighboors.put(v, vn);

                // Update it's number of connections
                Integer nc = nConnections.get(v);
                if (nc == null) nc = 0;
                nConnections.put(v, ++nc);

                // Add the variables as neighboors
                vn.addAll(vars);
            }
        }
        variables = remainingVariables.toArray(new Variable[]{});

        variableIndices = new VariableAssignment(variables.length);
        for(int i=0; i<variables.length; i++) {
            variableIndices.put(variables[i], i);
        }
        nPlacedNeighs = new int[variables.length];
    }

    protected HashMap<Variable, char[]> getDFS() {
        computeDFS();

        // And finally, the results
        HashMap<Variable, char[]> result =
                new HashMap<Variable, char[]>(variables.length);
        for (int i=0; i<adjacency.length; i++) {
            result.put(variables[i], adjacency[i]);
        }

        return result;
    }

    private void computeDFS() {
        if (adjacency != null) {
            return;
        }

        variableDepths = new VariableAssignment(variables.length);
        adjacency = new char[variables.length][variables.length];

        // Choose root node
        HashSet<Variable> next = getMostConnectedNodes(remainingVariables);
        root = this.pickRandomly(next);
        rootIndex = variableIndices.get(root);

        // Now build the tree
        int nNodes = buildTree(adjacency, root, 0);

        // On some strange cases, the problem is effectively split in two
        // separate subproblems, so the built tree may not contain all variables.
        // In this cases, we keep building new "trees" and merging them
        // to the root (with an empty separator) so we are able to solve all
        // subproblems at once.
        int subtrees = 1;
        ArrayList<Integer> subtreeNodes = new ArrayList<Integer>();
        subtreeNodes.add(nNodes);
        while(remainingVariables.size() > 0) {
            subtrees++;
            next = getMostConnectedNodes(remainingVariables);
            Variable n = this.pickRandomly(next);
            System.err.print("Warning: disconnected primal graph, next var: ");
            System.err.println(n);
            nNodes = buildTree(adjacency, n, 1);
            subtreeNodes.add(nNodes);
            // Connect n to the root
            adjacency[variableIndices.get(root)][variableIndices.get(n)] = 1;
        }

        /*StringBuilder buf = new StringBuilder("Subtrees: [");
        for(Integer i : subtreeNodes) {
            buf.append(i).append(", ");
        }
        System.out.println(buf.append("]").toString());*/
    }

    private void chosenVariable(Variable variable) {
        // Remove the variable from candidates list.
        remainingVariables.remove(variable);

        // Increase the number of placed neighboors for each neighboor of the
        // chosen variable.
        HashSet<Variable> neighs = neighboors.get(variable);
        for(Variable v : neighs) {
            nPlacedNeighs[variableIndices.get(v)]++;
        }
    }

    /**
     *
     * @param adjacency
     * @param currentNode
     * @param depth
     * @return number of nodes in the tree.
     */
    private int buildTree(char[][] adjacency, Variable currentNode, int depth) {
        int res = 1;
        
        // Store the depth of this variable
        if (currentNode == null) {
            System.out.println("ouch");
        }
        variableDepths.put(currentNode, depth);

        // Ending condition
        if (remainingVariables.isEmpty()) {
            return res;
        }

        HashSet<Variable> next = neighboors.get(currentNode);
        next.retainAll(remainingVariables);
        // Iterate while there are candidate childs for this node
        while (next.size() > 0) {

            // Choose one child amongst candidates
            HashSet<Variable> selectedCandidates = selectCandidates(next);
            Variable v = pickRandomly(selectedCandidates);

            // Link both nodes
            adjacency[variableIndices.get(v)][variableIndices.get(currentNode)] = 1;
             
            // Recurse
            res += buildTree(adjacency, v, depth+1);

            // Update the lits because some childs may have been placed.
            next.retainAll(remainingVariables);
        }

        return res;
    }

    public HashMap<Variable, CostFunction[]> getFactorAssignments() {
        if (adjacency == null) {
            computeDFS();
        }

        CostFunction[][] flist = assignFactors();
        HashMap<Variable, CostFunction[]> results =
                new HashMap<Variable, CostFunction[]>();

        for(int i=0; i<variables.length; i++) {
            results.put(variables[i], flist[i]);
        }

        return results;
    }

    private CostFunction[][] assignFactors() {
        HashMap<Variable, ArrayList<CostFunction>> factorList =
                new HashMap<Variable, ArrayList<CostFunction>>(variables.length);

        // Instantiate lists, so there are no "null" values
        for (Variable v : variables) {
            ArrayList<CostFunction> l = new ArrayList<CostFunction>();
            factorList.put(v, l);
        }


        // Assign the factors
        for (CostFunction f : factors) {

            // Find the most deep variable, to assign this factor to it's
            // corresponding node.
            int md = -1;                      // Maximum depth found
            Variable mv = null;              // Corresponding variable
            for (Variable v : f.getVariableSet()) {
                int d = variableDepths.get(v);
                if (d > md) {
                    mv = v;
                    md = d;
                }
            }

            // Now assign the factor
            ArrayList<CostFunction> fl = factorList.get(mv);
            fl.add(f);
        }

        // Convert to array of arrays
        CostFunction[][] result = new CostFunction[variables.length][];
        for (int i=0; i<variables.length; i++) {
            ArrayList<CostFunction> l = factorList.get(variables[i]);
            result[i] = new CostFunction[l.size()];
            result[i] = l.toArray(result[i]);
        }

        return result;
    }

    private Variable pickRandomly(HashSet<Variable> alternatives) {

        if (alternatives.isEmpty()) {
            return null;
        }

        Variable result = null;
        Iterator<Variable> iter = alternatives.iterator();

        int n = random.nextInt(alternatives.size());
        for (int i=0; i<=n; i++) {
            result = iter.next();
        }

        chosenVariable(result);
        return result;
    }

    protected HashSet<Variable> getMostConnectedNodes(HashSet<Variable> alternatives) {

        if (alternatives == null) {
            return null;
        }

        if (alternatives.size() == 1) {
            return new HashSet(alternatives);
        }

        // Now break ties by number of connections
        HashSet<Variable> bestCandidates = new HashSet<Variable>();
        int maxConnections = 0;
        for(Variable v : alternatives) {
            final int nc = nConnections.get(v);
            if (nc >= maxConnections) {
                if (nc > maxConnections) {
                    bestCandidates.clear();
                    maxConnections = nc;
                }
                bestCandidates.add(v);
            }
        }

        return bestCandidates;
    }

    protected HashSet<Variable> getMostPlacedNeighsNodes(HashSet<Variable> alternatives) {
        if (alternatives == null) {
            return null;
        }

        if (alternatives.size() == 1) {
            return new HashSet(alternatives);
        }

        // Create a list holding nodes with maximal number of placed neighs.
        HashSet<Variable> bestCandidates = new HashSet<Variable>();
        int maxPlacedNeighs = 0;
        for(Variable v : alternatives) {
            final int np = nPlacedNeighs[variableIndices.get(v)];
            if (np >= maxPlacedNeighs) {
                if (np > maxPlacedNeighs) {
                    bestCandidates.clear();
                    maxPlacedNeighs = np;
                }
                bestCandidates.add(v);
            }
        }

        return bestCandidates;
    }

    protected void printDFS() {
        computeDFS();
        recursePrintDFS(root, 0);
    }
    private void recursePrintDFS(Variable node, int depth) {
        for (int i=0; i<depth; i++) {
            System.out.print("  ");
        }
        System.out.println("|- " + node.getName());
        int nodeNum = variableIndices.get(node);
        for (int i=0; i<variables.length; i++) {
            if (adjacency[i][nodeNum] > 0) {
                recursePrintDFS(variables[i], depth+1);
            }
        }
    }

    public char[][] getAdjacency() {
        if (adjacency == null) {
            computeDFS();
        }

        return adjacency;
    }

    public CostFunction[][] getFactorDistribution() {
        if (adjacency == null) {
            computeDFS();
        }
        
        return assignFactors();
    }

    protected abstract HashSet<Variable> selectCandidates(HashSet<Variable> next);

}
