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

package es.csic.iiia.dcop.gdlf.strategies.control;

import es.csic.iiia.dcop.cli.*;
import java.util.logging.Level;

/**
 * Singleton control strategy builder.
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public enum ControlStrategies {
    TOP_DOWN (UnlimitedComputationTopDownControlStrategy.class),
    LIMITED_BOTTOM_UP (LimitedComputationBottomUpControlStrategy.class),
    UNLIMITED_BOTTOM_UP (UnlimitedComputationBottomUpControlStrategy.class),
    MIXED_NOSLICE (MixedWithoutSliceControlStrategy.class),
    MIXED_SLICE (MixedWithSliceControlStrategy.class),
    MIXED_USLICE (MixedWithUSliceControlStrategy.class),
    ;

    private ControlStrategy instance;
    
    ControlStrategies(Class<? extends ControlStrategy> c) {
        try {
            instance = c.newInstance();
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CliApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CliApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ControlStrategy getInstance(int maxr) {
        instance.setMaxR(maxr);
        return instance;
    }
}
