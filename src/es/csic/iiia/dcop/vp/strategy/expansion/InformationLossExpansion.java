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

package es.csic.iiia.dcop.vp.strategy.expansion;

import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.figdl.FIGdlNode;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class InformationLossExpansion implements ExpansionStrategy {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);

    public int getNumberOfSolutionsToExpand(ArrayList<VariableAssignment> mappings, UPNode upnode) {
        if (!(upnode instanceof FIGdlNode)) {
            throw new RuntimeException("Information loss expansion can only be used with the FIGDL algorithm.");
        }

        FIGdlNode finode = (FIGdlNode)upnode;
        double informationLoss = finode.getInformationLoss();
        double maxInformationLoss = finode.getMaxInformationLoss();
        int remainingSlots = VPStrategy.numberOfSolutions - mappings.size();

        int solutions = 0;
        if (informationLoss == maxInformationLoss) {

            // Leaf node!
            solutions = remainingSlots;

        } else {

            double ns = informationLoss/((double)maxInformationLoss)*remainingSlots;
            solutions = (int)ns;
            ns -= (double)solutions;
            if (log.isTraceEnabled()) {
                log.trace("bl: " + informationLoss + ", cb: " + maxInformationLoss
                        + ", rs: " + remainingSlots + ", ns: " + ns);
            }
            if (Math.random() < ns) {
                solutions++;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Solutions to expand: " + solutions);
        }

        return solutions;
    }
}