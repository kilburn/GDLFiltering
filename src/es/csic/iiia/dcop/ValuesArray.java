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

package es.csic.iiia.dcop;

import es.csic.iiia.dcop.CostFunction.Combine;
import es.csic.iiia.dcop.CostFunction.Summarize;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class ValuesArray extends ArrayList<Double> {

    public ValuesArray() {
        super();
    }

    public ValuesArray(int capacity) {
        super(capacity);
    }

    public ValuesArray(ValuesArray other) {
        super(other);
    }

    public ValuesArray extend(ArrayList<Integer> upMappings) {
        if (this.size() == upMappings.size())
            return this;

        if (this.size() > upMappings.size())
            throw new ArrayIndexOutOfBoundsException("Can not extend to a lower number of elements");

        final int len = upMappings.size();
        ValuesArray result = new ValuesArray(len);
        for(int i=0; i<len; i++) {
            final int j = upMappings.get(i);
            if (j >= this.size()) {
                System.out.println("hugh?");
            }
            result.add(this.get(j));
        }

        return result;
    }

    public ValuesArray reduce(ArrayList<Integer> upMappings,
            Summarize sum) {
        if (this.size() != upMappings.size())
            throw new ArrayIndexOutOfBoundsException("Can not reduce to a higher number of elements");

        TreeSet<Integer> reduced = new TreeSet<Integer>(upMappings);
        
        final int len = reduced.size();
        ValuesArray result = new ValuesArray(len);
        for(int i=0; i<len; i++) {
            // Fetch the best value that maps to i
            double bestValue = sum.getNoGood();

            for (int j=0, len2 = size(); j<len2; j++) {
                // Skip it if it doesn't map to i
                if (upMappings.get(j) != i) continue;

                final double value = get(j);
                if (sum.isBetter(value, bestValue))
                    bestValue = value;
            }

            // Set the result
            result.add(bestValue);
        }

        return result;
    }

    public double getBest(Summarize sum) {
        double res = sum.getNoGood();
        for(Double v : this) {
            if (sum.isBetter(v, res)) {
                res = v;
            }
        }
        return res;
    }

    public int getBestIndex(Summarize sum) {
        double best = sum.getNoGood();
        int idx = 0;
        for (int i=0; i<size(); i++) {
            final double v = get(i);
            if (sum.isBetter(v, best)) {
                best = v;
                idx = i;
            }
        }
        return idx;
    }

    public ValuesArray combine(ValuesArray other, Combine com) {
        final int size = size();

        if (size != other.size()) {
            throw new ArrayIndexOutOfBoundsException("Value arrays must be of the same size to be combined.\n"+this+"\n"+other);
        }

        ValuesArray res = new ValuesArray(this);
        for (int i=0; i<size; i++) {
            res.set(i, com.eval(get(i), other.get(i)));
        }

        return res;
    }

    public ValuesArray invert(Combine com) {
        final int size = size();
        final ValuesArray res = new ValuesArray(this);
        for (int i=0; i<size; i++) {
            res.set(i, com.negate(get(i)));
        }
        return res;
    }

}
