/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2011, IIIA-CSIC, Artificial Intelligence Research Institute
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

package es.csic.iiia.dcop.gdlf.strategies;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.gdlf.Limits;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractGdlFStrategy implements GdlFStrategy {
    
    private final ControlStrategy control;
    private final MergeStrategy merge;
    private final FilterStrategy filter;
    private final SliceStrategy slice;
    
    public AbstractGdlFStrategy(ControlStrategy control,
            MergeStrategy merge, FilterStrategy filter, SliceStrategy slice)
    {
        this.control = control;
        this.merge = merge;
        this.filter = filter;
        this.slice = slice;
    }
    
    protected ControlStrategy getControlStrategy() {
        return control;
    }

    public boolean hasMoreElements() {
        return control.hasMoreElements();
    }

    public Limits nextElement() {
        return control.nextElement();
    }

    public List<CostFunction> merge(List<CostFunction> fs, Collection<Variable> edgeVariables, int rComputation, int rCommunication) {
        return merge.merge(fs, edgeVariables, rComputation, rCommunication);
    }

    public List<CostFunction> filter(List<CostFunction> fs, List<CostFunction> pfs, double ub) {
        return filter.filter(fs, pfs, ub);
    }

    public List<CostFunction> slice(List<CostFunction> fs, int r) {
        return slice.slice(fs, r);
    }
    
    public void setMaxR(int r) {
        control.setMaxR(r);
    }
    
}
