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

package es.csic.iiia.dcop.igdl;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.mp.AbstractMessage;
import es.csic.iiia.dcop.mp.Message.Encoding;
import es.csic.iiia.dcop.util.CostFunctionStats;
import java.util.ArrayList;

/**
 * GDL Utility message.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class IGdlMessage extends AbstractMessage {

    private ArrayList<CostFunction> factors;
    private CostFunction belief = null;

    public IGdlMessage(ArrayList<CostFunction> factors) {
        this.factors = factors;
    }

    public IGdlMessage() {
        this.factors = new ArrayList<CostFunction>();
    }

    public ArrayList<CostFunction> getFactors() {
        return this.factors;
    }

    public boolean addFactor(CostFunction factor) {
        return factors.add(factor);
    }

    public void setBelief(CostFunction belief) {
        this.belief = belief;
    }

    public int getBytes(Encoding encoding) {
        int size = 0;
        switch(encoding) {
            case NORMAL:
                for (CostFunction f : factors) {
                    size += f.getVariableSet().size()+1;
                    size += f.getSize()*4;
                }
                break;
            default:
                break;
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        CostFunction combi = null;
        for (CostFunction f : getFactors()) {
            buf.append("\n\t" + f);
            if (belief != null) {
                combi = f.combine(combi);
            }
        }
        if (belief != null) {
            Variable[] vars = belief.getVariableSet().toArray(new Variable[0]);
            if (combi == null) {
                combi = belief.getFactory().buildCostFunction(vars);
            } else {
                combi = combi.summarize(vars);
            }
        }
        if (belief != null) {
            buf.append("\n   Apr: ");
            buf.append(combi);
            buf.append("\n   Opt: ");
            buf.append(belief);
            combi.negate();
            buf.append("\n   Err: ");
            CostFunction err = belief.combine(combi);
            buf.append(err);
            buf.append("\n  Stat: ");
            buf.append(new CostFunctionStats(err));
        }
        return buf.toString();
    }
}
