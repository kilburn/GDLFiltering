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

package es.csic.iiia.dcop.gdlf;

import es.csic.iiia.dcop.gdlf.strategies.control.ControlStrategy;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.gdlf.strategies.filter.FilterStrategy;
import es.csic.iiia.dcop.gdlf.strategies.merge.MergeStrategy;
import es.csic.iiia.dcop.gdlf.strategies.slice.SliceStrategy;
import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.up.UPResult;
import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPFactory;

/**
 * Factory for the Utility Propagation GDL implementation.
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class GdlFFactory implements UPFactory<GdlFGraph, GdlFNode, UPEdge<GdlFNode, GdlFMessage>,
    UPResult, UPResults> {

    private final Modes mode = Modes.TREE_UP;
    
    private final ControlStrategy controlStrategy;
    private final MergeStrategy mergeStrategy;
    private final FilterStrategy filterStrategy;
    private final SliceStrategy sliceStrategy;
    
    /*
     * Required to invert maximization problems to minimization ones, and
     * to handle negative values.
     */
    private final CostFunction constant;
    private final boolean inverted;

    public GdlFFactory(CostFunction constant, boolean inverted, 
            ControlStrategy control, MergeStrategy merge, FilterStrategy filter,
            SliceStrategy slice)
    {
        this.constant = constant;
        this.inverted = inverted;
        this.controlStrategy = control;
        this.mergeStrategy = merge;
        this.filterStrategy = filter;
        this.sliceStrategy = slice;
    }

    public GdlFGraph buildGraph() {
        GdlFGraph g = new GdlFGraph(constant, inverted, controlStrategy);
        return g;
    }

    public GdlFNode buildNode() {
        GdlFNode n = new GdlFNode();
        n.setMode(mode);
        n.setMergeStrategy(mergeStrategy);
        n.setFilterStrategy(filterStrategy);
        n.setSliceStrategy(sliceStrategy);
        return n;
    }

    public UPEdge<GdlFNode, GdlFMessage> buildEdge(GdlFNode node1, GdlFNode node2) {
        return new UPEdge<GdlFNode, GdlFMessage>(node1, node2);
    }

    public UPResult buildResult(GdlFNode node) {
        return new UPResult(node);
    }

    public UPResults buildResults() {
        return new UPResults();
    }

}
