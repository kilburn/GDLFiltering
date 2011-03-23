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

package es.csic.iiia.dcop.cli;

import es.csic.iiia.dcop.vp.strategy.expansion.ExpansionStrategy;
import java.util.logging.Level;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public enum SolutionExpansionStrategies {
    ROOT (es.csic.iiia.dcop.vp.strategy.expansion.RootExpansion.class),
    GREEDY (es.csic.iiia.dcop.vp.strategy.expansion.GreedyExpansion.class),
    STOCHASTIC (es.csic.iiia.dcop.vp.strategy.expansion.StochasticalExpansion.class),
    INFORMATION_LOSS (es.csic.iiia.dcop.vp.strategy.expansion.InformationLossExpansion.class),
    ;

    private ExpansionStrategy instance;
    SolutionExpansionStrategies(Class<? extends ExpansionStrategy> c) {
        try {
            instance = c.newInstance();
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CliApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CliApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    ExpansionStrategy getInstance() {
        return instance;
    }
}
