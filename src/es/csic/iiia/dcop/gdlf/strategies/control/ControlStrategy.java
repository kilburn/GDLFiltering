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

import es.csic.iiia.dcop.gdlf.Limits;
import java.util.Enumeration;

/**
 * Filtering control strategy.
 * 
 * A strategy defines which limits to impose at each iteration and for each
 * of the three stages of computation:
 * <ol>
 * <li>Combine computation limit (maximum number of variables of any kind in a
 * single combined function</li>
 * <li>Combine "communication" limit (maximum number of separator variables in a
 * single combined function)</li>
 * <li>Slice communication limit (maximum number of variables in a single 
 * function to send)</li>
 * </ol>
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public interface ControlStrategy extends Enumeration<Limits> {
    
    /**
     * Set the maximum <em>r</em> value (the algorithm stops if this r-value is 
     * reached).
     * 
     * Typically <em>r</em> starts with value 2 and increases by 1 at each
     * iteration. This enfoces a strict limit on the number of iterations
     * (up and down passes on the tree) that the algorithm will perform.
     * 
     * @param maxR maximum r value.
     */
    public void setMaxR(int maxR);
    
    /**
     * Set the delta value.
     * 
     * Used only by mixed control strategies but defined here
     * to homogenize the handling of strategies.
     * 
     * @param delta 
     */
    public void setDelta(int delta);
}
