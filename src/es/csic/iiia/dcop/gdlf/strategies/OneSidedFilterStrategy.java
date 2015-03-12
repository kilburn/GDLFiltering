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
import es.csic.iiia.dcop.util.MemoryTracker;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class OneSidedFilterStrategy implements FilterStrategy {
    
    private static Logger log = LoggerFactory.getLogger(OneSidedFilterStrategy.class);

    // TODO: Memory tracking
    public List<CostFunction> filter(List<CostFunction> fs, List<CostFunction> pfs, double ub) {
        
        if (Double.isNaN(ub)) {
            return fs;
        }

        // Filtering requires copying the filtered function at each step,
        // so we account for the biggest one.
        long maxmem = Long.MIN_VALUE;
        ArrayList<CostFunction> res = new ArrayList<CostFunction>();
        for (int i=0, len=fs.size(); i<len; i++) {
            List<CostFunction> filterers = pfs;

            final CostFunction outf = fs.get(i);
            maxmem = Math.max(maxmem, MemoryTracker.getRequiredMemory(outf));
            
            final CostFunction filtered = outf.filter(filterers, ub);
            if (log.isTraceEnabled()) {
                log.trace("Input b:" + ub + " f:" + outf);
                log.trace("Filtered: " + filtered);
            }
            res.add(filtered);
        }
        
        MemoryTracker.add(maxmem);
        return res;        
    }

    

}
