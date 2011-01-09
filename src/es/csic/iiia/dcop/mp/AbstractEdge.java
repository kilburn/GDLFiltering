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

/**
 * Abstract implementation of an undirected edge between two nodes that
 * exchange messages.
 *
 * @param <N> Connected nodes type.
 * @param <M> Message type.
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractEdge<N extends Node, M extends Message> implements Edge<N,M> {

    private N c1;
    private N c2;
    private M m1;
    private M m2;
    private M nm1;
    private M nm2;

    /**
     * Constructs a new edge connecting the specified Nodes.
     *
     * @param c1 node on one side of the edge.
     * @param c2 node on the other side of the edge.
     */
    public AbstractEdge(N c1, N c2) {
        this.c1 = c1;
        this.c2 = c2;
        c1.addEdge(this);
        c2.addEdge(this);
    }

    /**
     * Builds a new edge copying the given one.
     *
     * @param c1 node on one side of the edge.
     */
    public AbstractEdge(AbstractEdge<N,M> e) {
        copyFrom(e);
    }

    public void clear() {
        m1 = null;
        m2 = null;
        nm1 = null;
        nm2 = null;
    }

    public boolean sendMessage(N sender, M message) {
        if (sender == c1) {
            if (message.equals(m2))
                return false;
            nm2 = message;
        } else {
            if (message.equals(m1))
                return false;
            nm1 = message;
        }
        long bytes = message.getBytes();
        sender.addSentBytes(bytes);
        BytesSent.add(bytes);
        return true;
    }

    public M getMessage(N recipient) {
        if (recipient == c1) {
            return m1;
        }
        return m2;
    }

    public M getMessage(Object recipient) {
        if (recipient == c1) {
            return m1;
        }
        return m2;
    }

    /**
     * Propagates messages send during the previous tick to the current one,
     * notifiying Nodes as needed.
     */
    public void tick() {
        // Propagate new messages, notifying the nodes that have new msgs.
        if (nm1 != m1) {
            m1 = nm1;
            c1.setUpdated(true);
        }

        if (nm2 != m2) {
            m2 = nm2;
            c2.setUpdated(true);
        }
    }

    public N getNode1() {
        return c1;
    }

    public N getNode2() {
        return c2;
    }

    public boolean haveSentMessage(N sender) {
        if (sender == c1) {
            return m2 != null || nm2 != null;
        } else {
            return m1 != null || nm1 != null;
        }
    }

    public N getDestination(N node) {
        if (node == c1) return c2;
        return c1;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(c1.getName());
        buf.append(" -- ");
        buf.append(c2.getName());

        return buf.toString();
    }

    private void copyFrom(AbstractEdge<N,M> e) {
        c1 = e.c1;
        c2 = e.c2;
        m1 = e.m1;
        nm1 = e.nm1;
        m2 = e.m2;
        nm2 = e.nm2;
    }

}
