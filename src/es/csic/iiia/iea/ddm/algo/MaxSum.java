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

package es.csic.iiia.iea.ddm.algo;

import es.csic.iiia.iea.ddm.*;
import es.csic.iiia.iea.ddm.cg.CgEdge;
import es.csic.iiia.iea.ddm.cg.CliqueGraph;
import es.csic.iiia.iea.ddm.cg.CgNode;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Max-Sum initialization algorithm.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class MaxSum {

    /**
     * This object should not be constructed.
     */
    private MaxSum(){};

    /**
     * Initializes a clique graph for the specified factors using the max-sum
     * algorithm (that also creates one new clique for each variable involved
     * in the graph).
     *
     * @param factors List of factors describing the problem.
     * @return 
     */
    public static CliqueGraph buildGraph(CostFunction[] factors) {

        CliqueGraph cg = new CliqueGraph();

        // Build a clique for each factor;
        ArrayList<CgNode> cliques = new ArrayList<CgNode>();
        HashSet<Variable> varSet = new HashSet<Variable>();
        for(CostFunction f : factors) {
            cliques.add(new CgNode(f));
            varSet.addAll(f.getVariableSet());
        }

        // Build a clique for each variable and connect every previous clique
        // containing the same variable with it.
        for(Variable v : varSet) {
            CgNode cv = new CgNode(v);
            cg.addNode(cv);
            for (CgNode c : cliques) {
                if (c.contains(v)) {
                    cg.addEdge(new CgEdge(c, cv, v));
                }
            }
        }

        // Add the original factor cliques
        for (CgNode c : cliques) {
            cg.addNode(c);
        }

        return cg;
    }

}
