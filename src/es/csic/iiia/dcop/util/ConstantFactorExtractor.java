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

package es.csic.iiia.dcop.util;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */

public class ConstantFactorExtractor {
    
    private static Logger log = LoggerFactory.getLogger(ConstantFactorExtractor.class);

    public static CostFunction extract(List<CostFunction> factors, CostFunction constant) {

        for (int i=factors.size()-1; i>=0; i--) {
            final CostFunction f = factors.get(i);
            if (f.getVariableSet().isEmpty()) {
                constant = constant.combine(f);
                factors.remove(i);
            }
        }
        
        if (log.isWarnEnabled() && constant.getValue(0) != 0) {
            log.warn("Extracted constant: " + constant);
        }
        
        return constant;
    }

    public static CostFunction positivize(List<CostFunction> factors, CostFunction constant) {
        
        VariableAssignment map = null;
        for (int i=factors.size()-1; i>=0; i--) {
            final CostFunction f = factors.get(i);
            map = f.getOptimalConfiguration(map);
            double v = f.getValue(map);
            if (v < 0) {
                CostFunction minf = f.getFactory().buildCostFunction(new Variable[0], v);
                factors.set(i, f.combine(minf.negate()));
                constant = constant.combine(minf);
            }
        }
        
        if (log.isWarnEnabled()) {
            log.warn("Positivized problem constant: " + constant);
        }
        
        return constant;
    }
    
}
