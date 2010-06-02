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
import es.csic.iiia.dcop.up.IUPNode;
import es.csic.iiia.dcop.up.UPEdge;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.CostFunctionStats;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class EntropyStrategy extends IGdlPartitionStrategy {

    private static Logger log = LoggerFactory.getLogger(UPGraph.class);
    private IGdlPartitionStrategy strategy;

    @Override
    public void initialize(IUPNode node) {
        strategy = new LazyStrategy();
        strategy.initialize(node);
        super.initialize(node);
    }

    public IGdlMessage getPartition(ArrayList<CostFunction> fs,
            UPEdge<? extends IUPNode, IGdlMessage> e) {

        // Informational, just for debugging
        if (log.isTraceEnabled()) {
            StringBuffer buf = new StringBuffer();
            int i = e.getVariables().length;
            for (Variable v : e.getVariables()) {
                buf.append(v.getName());
                if (--i != 0) buf.append(",");
            }
            log.trace("-- Edge vars: {" + buf.toString() + "}, Functions:");
            for (CostFunction f : fs) {
                log.trace("\t" + f);
            }
        }

        // Sort the functions according to their rank (max - min)
        if (log.isTraceEnabled()) {
            ArrayList<CostFunction> prev = fs;
            fs = sortFunctions(fs);
            log.trace("-- Sorted:");
            for (CostFunction f : fs) {
                log.trace("\t" + CostFunctionStats.formatValue(CostFunctionStats.getEntropy(f)) + "\t" + f);
            }
            // Check that the list hasn't been modified
            for(CostFunction f : fs) {
                prev.remove(f);
            }
            if (prev.size() > 0) {
                System.err.println("List differs!");
                System.exit(0);
            }
        } else {
            fs = sortFunctions(fs);
        }

        return strategy.getPartition(fs, e);
    }

    private ArrayList<CostFunction> sortFunctions(ArrayList<CostFunction> fs) {
        ArrayList<CostFunction> res = new ArrayList<CostFunction>(fs);
        Collections.sort(res, new EntropyComparator());
        return res;
    }

    private class EntropyComparator implements Comparator<CostFunction> {
        public int compare(CostFunction o1, CostFunction o2) {
            double r1 = CostFunctionStats.getEntropy(o1);
            double r2 = CostFunctionStats.getEntropy(o1);
            return r1 == r2 ? 0 : (r1 < r2 ? -1 : 1);
        }
    }

}