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

package es.csic.iiia.dcop.igdl.strategy;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.igdl.IGdlMessage;
import es.csic.iiia.dcop.igdl.IGdlNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.CombinationGenerator;
import es.csic.iiia.dcop.util.CostFunctionStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class AllCombStrategy extends IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);

    private IGdlPartitionStrategy subStrategy;

    @Override
    public void initialize(IGdlNode node) {
        super.initialize(node);
        subStrategy = new RankDownStrategy();
        subStrategy.initialize(node);
    }

    public IGdlMessage getPartition(ArrayList<CostFunction> fs,
            UPEdge<IGdlNode, IGdlMessage> e) {

        
        // Free factors, that do *not* contain any separator variable.
        ArrayList<CostFunction> ff = new ArrayList<CostFunction>();
        // Bound factors, that contain at least one separator variable.
        ArrayList<CostFunction> bf = new ArrayList<CostFunction>();

        log.trace("- Node functions");
        for (CostFunction f : fs) {
            log.trace("\t " + f);
        }
        bf.addAll(fs);
        // Obtain separate lists for bound and free factors
        //computeFreeAndBoundFactors(fs, ff, bf, e.getVariables());

        // Merge the free factors to bound ones
        //mergeFreeFactors(ff, bf, e.getVariables());

        // Build and return the message from the constructed.
        IGdlMessage msg = new IGdlMessage();

        // Separate bound factors in set of functions that do *not* share
        // any variable
        ArrayList<CFSet> fsets = computeDisjointSets(bf);
        log.trace("- " + fsets.size() + " disjoint BF sets");
        for(int i=0; i<fsets.size(); i++) {
            CFSet set = fsets.get(i);
            
            set.variables.retainAll(Arrays.asList(e.getVariables()));
            int nBoundVariables = set.variables.size();
            int nCombinations = CombinationGenerator.binom(nBoundVariables, node.getR());
            log.trace("Number of combinations: " + nCombinations);

            /*
            PartitionGenerator pgen = new PartitionGenerator(set.variables, node.getR());
            while(pgen.hasNext()) {
                pgen.next()
             }*/

            IGdlMessage pmsg = subStrategy.getPartition(set.functions, e);
            for(CostFunction f : pmsg.getFactors()) {
                msg.addFactor(f);
            }
        }
        
        return msg;
    }

    private ArrayList<CFSet> computeDisjointSets(ArrayList<CostFunction> bf) {
        ArrayList<CFSet> fsets = new ArrayList<CFSet>();

        for (CostFunction f : bf) {
            
            // Start by creating a new set that only contains this function
            CFSet fset = new CFSet(f);
            final Collection<Variable> vset = f.getVariableSet();

            // Merge any intersecting set with this function's one
            for (int i = fsets.size()-1; i>=0; i--){
                CFSet set = fsets.get(i);
                if (set.intersects(vset)) {
                    fset.merge(set);
                    fsets.remove(i);
                }
            }

            // fset is now either a single-function set (because f did not
            // interect with any pre-existing set), or a merge of previous
            // sets that were already removed from fsets in the above loop.
            fsets.add(fset);
        }

        return fsets;
    }

    private class CFSet {
        private HashSet<Variable> variables = new HashSet<Variable>();
        private ArrayList<CostFunction> functions = new ArrayList<CostFunction>();
        public CFSet(CostFunction f) {
            functions.add(f);
            variables.addAll(f.getVariableSet());
        }
        public boolean intersects(Collection<Variable> variables) {
            for (Variable v : variables) {
                if (this.variables.contains(v)) {
                    return true;
                }
            }
            return false;
        }
        public void merge(CFSet set) {
            variables.addAll(set.variables);
            functions.addAll(set.functions);
        }
    }

}
