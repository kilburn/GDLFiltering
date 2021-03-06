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

package es.csic.iiia.dcop.up;

import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.mp.DefaultGraph;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility propagation graph.
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 * @param <N> Type of nodes
 * @param <E> Type of edges
 * @param <R> Type of per-agent result
 */
public abstract class UPGraph<N extends UPNode, E extends UPEdge, R extends UPResults>
        extends DefaultGraph<N,E,R> {
    
    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    private CostFunctionFactory factory;

    public void setFactory(CostFunctionFactory factory) {
        this.factory = factory;
        for (N clique : getNodes()) {
            clique.setFactory(factory);
        }
    }

    public CostFunctionFactory getFactory() {
        return factory;
    }

    @Override
    public void reportIteration(int i) {
        log.trace("------- Iter " + i);
    }

    @Override
    public void reportStart() {
        log.debug("\n======= PROPAGATING UTILITIES");
    }

    @Override
    public void reportResults(R results) {
        if (log.isTraceEnabled()) {
            log.trace("------- " + results);
        }
    }

    /*
     * More boilerplate code to make the compiler realize the types.
     */
    @Override public ArrayList<N> getNodes() {return super.getNodes();}
    @Override public ArrayList<E> getEdges() {return super.getEdges();}
    @Override public R run(int maxIterations) {return super.run(maxIterations);}
}
