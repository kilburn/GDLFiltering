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
import java.util.Iterator;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CostFunctionStats {

    private CostFunction f;
    
    public CostFunctionStats(CostFunction f) {
        this.f = f;
    }
    @Override public String toString() {

        // Gather statistics
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        for (Iterator<Integer> i = f.iterator(); i.hasNext(); ) {
            final double v = f.getValue(i.next());
            min = Math.min(min, v);
            max = Math.max(max, v);
            sum = sum + v;
        }
        final double size = f.getSize();
        final double avg = size == 0 ? 0 : sum/size;

        // Output them
        StringBuffer buf = new StringBuffer();
        buf.append("Min: ").append(formatValue(min));
        buf.append(", Avg: ").append(formatValue(avg));
        buf.append(", Max: ").append(formatValue(max));
        buf.append(", Dif: ").append(formatValue(max-min));
        buf.append(", Tot: ").append(formatValue(sum));

        return buf.toString();
    }

    public static String formatValue(double value) {
        String res = String.valueOf(value);
        if (Math.abs(value) < 1e-5) {
            return "0";
        }
        final int idx = res.indexOf('.');
        if (idx > 0) {
            res = res.substring(0, Math.min(res.length(), idx+4));
        }
        return res;
    }

    public static double getRank(CostFunction f) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Iterator<Integer> i = f.iterator(); i.hasNext();) {
            final double v = f.getValue(i.next());
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return max-min;
    }

}
