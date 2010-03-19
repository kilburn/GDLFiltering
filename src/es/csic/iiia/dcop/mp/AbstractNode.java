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

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @param <E>
 * @param <R>
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractNode<E extends Edge, R extends Result> implements Node<E,R> {

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

    /**
     * Nodes can operate either inside a tree or inside a graph.
     *
     * When in tree mode, they only operate after receiving messages from each
     * (and every) child. Contrastingly, graph nodes operate each time that
     * they receive a message.
     */
    public enum Modes {
        TREE, GRAPH
    }

    /**
     * Node operating mode
     */
    private Modes mode = Modes.GRAPH;

    /**
     * Edges associated to the edge, through which it will send and receive
     * messages.
     */
    private ArrayList<E> edges;

    /**
     * Flag to track if new messages have been received.
     */
    private boolean updated;

    /**
     * Returns true if the node has converged or false otherwise (only valid
     * for nodes operating in graph mode).
     */
    public abstract boolean isConverged();

    public AbstractNode() {
        edges = new ArrayList<E>();
    }

    /**
     * Checks if this clique has received new messages.
     *
     * @return true if this clique has received new messages or false otherwise.
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * Signals a new message arrival to this clique.
     * 
     * @param updated
     */
    public void setUpdated(boolean updated) {
        if (updated == true && getMode() == Modes.TREE) {
            // Check if we have received messages from at least all neighboors
            // but one (the parent), meaning that all childs have messaged us.
            int n_msgs = 0;
            for (E e : edges) {
                if (e.getMessage(this) != null)
                    n_msgs++;
            }
            if (n_msgs < edges.size()-1) {
                return;
            }
        }
        this.updated = updated;
    }

    /**
     * Check if we are ready to send the message of the given edge.
     *
     * @param edge
     * @return
     */
    protected boolean readyToSend(E edge) {
        // We are always ready to send in graph mode
        if (this.getMode() == Modes.GRAPH)
            return true;

        // In tree mode, we can send to one neighbor (edge) only if:
        for (E e : getEdges()) {
            // We have not yet sent a message to it
            if (e == edge && e.haveSentMessage(this))
                return false;
            // We have received messages from all other neighbors (edges)
            if (e != edge && e.getMessage(this) == null)
                return false;
        }
        return true;
    }

    /**
     * Adds a new edge to this clique.
     *
     * Cliques don't know anything about their neighboors but can send and
     * receive messages through their associated edges.
     *
     * @param edge edge to be added.
     */
    public void addEdge(E edge) {
        edges.add(edge);
    }

    /**
     * Retrieves the edges of this clique.
     * 
     * @return list of edges where this node is connected.
     */
    public Collection<E> getEdges() {
        return edges;
    }

    /**
     * Check if this node has finished operating.
     */
    public boolean isFinished() {
        return mode == Modes.GRAPH ? isConverged() : sentAndReceivedAllEdges();
    }

    public boolean sentAndReceivedAllEdges() {
        // We end after having received and sent exactly 1 message from/to each
        // neighbor (edge).
        for (E e : getEdges()) {
            if (e.getMessage(this) == null || !e.haveSentMessage(this))
                return false;
        }

        return true;
    }
}
