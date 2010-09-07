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

package es.csic.iiia.dcop.vp;

import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.mp.DefaultResults;
import java.util.ArrayList;

/**
 * Value Propagation algorithm's collection of results.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VPResults extends DefaultResults<VPResult> {
    
    public ArrayList<VariableAssignment> getMappings() {

        // Simple result collection
        ArrayList<VariableAssignment> results = new ArrayList<VariableAssignment>();
        for (VPResult r : getResults()) {
            int i=0;
            for (VariableAssignment map : r.getMappings()) {
                
                VariableAssignment result;
                if (i == results.size()) {
                    result = new VariableAssignment();
                    results.add(result);
                } else {
                    result = results.get(i);
                }

                if (map!=null) {
                    result.putAll(map);
                }
                i++;
            }
        }

        return results;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString().split("\\n", 2)[0]);
        buf.append("\nGlobal : ");
        for (VariableAssignment map : getMappings()) {
            buf.append("\n\t").append(map);
        }
        /*for (VPResult r : getResults()) {
            buf.append("\n\t").append(r);
        }*/

        return buf.toString();
    }

}
