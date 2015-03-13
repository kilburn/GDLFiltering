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

package es.csic.iiia.dcop.gdlf.strategies.merge;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import java.util.Collection;
import java.util.List;

/**
 * Incoming message combination strategy.
 * 
 * A combination strategy defines how to combine incoming messages into a list
 * of "partitions". The combination may be limited by any of the two combination
 * bounds, namely:
 * 
 * <ol>
 * <li>The first stage bound, defining the maximum number of variables (of any
 * kind) in any of the "partitions" (functions) outputted by this strategy</li>
 * <li>The second-stage bound, defining the maximum number of separator
 * variables in any of the output partitions</li>
 * </ol>
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public interface MergeStrategy {
    
    /**
     * Combines the given incoming messages, taking into account which variables
     * are on the separator (edge variables) and the specified first and second 
     * stage bounds.
     * 
     * @param messages messages received by this node.
     * @param edgeVariables separator variables (with the destination node).
     * @param rComputation first-stage bound.
     * @param rCommunication second-stage bound.
     * @return list of partitions (cost functions)
     */
    public List<CostFunction> merge(List<CostFunction> messages, 
            Collection<Variable> edgeVariables, int rComputation, 
            int rCommunication);
    
}
