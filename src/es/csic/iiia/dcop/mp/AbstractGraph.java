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

package es.csic.iiia.dcop.mp;

import es.csic.iiia.dcop.util.BytesSent;
import es.csic.iiia.dcop.util.ConstraintChecks;
import es.csic.iiia.dcop.util.MemoryTracker;
import java.util.ArrayList;

/**
 *
 * @param <N>
 * @param <E>
 * @param <R>
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public abstract class AbstractGraph<N extends Node,E extends Edge,R extends Results>
        implements Graph<N, E, R> {
    private ArrayList<N> nodes;
    private ArrayList<E> edges;
    private R results;
    private int root = -1;

    public AbstractGraph() {
        nodes = new ArrayList<N>();
        edges = new ArrayList<E>();
    }

    public ArrayList<E> getEdges() {
        return edges;
    }

    public ArrayList<N> getNodes() {
        return nodes;
    }

    public void addEdge(E edge) {
        edges.add(edge);
    }

    public void addNode(N node) {
        nodes.add(node);
    }

    public R getResults() {
        if (results == null) {
            results = buildResults();
        }
        return results;
    }
    protected void setResults(R results) {
        this.results = results;
    }

    protected abstract R buildResults();

    protected void initialize() {
        // Algorithm initialization
        results = buildResults();
        
        long mcc = 0, tcc = 0, mbytes = 0, tbytes = 0, mmem = 0;
        for(Node n : nodes) {
            ConstraintChecks.addTracker(n);
            BytesSent.addTracker(n);
            MemoryTracker.addTracker(n);
            
            n.initialize();
            
            long cc = ConstraintChecks.removeTracker(n);
            long bytes = BytesSent.removeTracker(n);
            long mem = MemoryTracker.removeTracker(n);
            
            tcc += cc;
            tbytes += bytes;
            mcc = Math.max(mcc, cc);
            mbytes = Math.max(mbytes, bytes);
            mmem = Math.max(mmem, mem);
        }
        results.addCycle(mcc, tcc, mbytes, tbytes, mmem);
    }

    protected void end() {
        for (Node n : nodes) {
            results.add(n.end());
        }
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        if (this.root >= 0)
            nodes.get(this.root).unsetRoot();
        this.root = root;
        nodes.get(root).setRoot();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("graph G {\n");
        if (root >= 0) {
            buf.append("// Root: ");
            buf.append(nodes.get(root));
            buf.append("\n");
        }
        for (N n : nodes) {
            buf.append("  ");
            buf.append(n.getName());
            buf.append( "[label=\"");
            buf.append(n.toString());
            buf.append("\"];");
            buf.append("\n");
        }

        for (Edge e : edges) {
            buf.append("  ");
            buf.append(e.toString());
            buf.append("\n");
        }
        buf.append("}");

        return buf.toString();
    }

}
