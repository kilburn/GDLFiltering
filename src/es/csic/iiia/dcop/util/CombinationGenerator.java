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

import es.csic.iiia.dcop.Variable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CombinationGenerator implements Iterator<Set<Variable>> {
    private int n;
    private int r;
    private int total;
    private int left;
    private int[] a;
    private Variable[] vars;
    private Set<Variable> set;

    public CombinationGenerator(Variable[] vars, int r) {
        this.n = vars.length;
        this.r = r;
        this.vars = vars;
        this.a = new int[r];
        this.total = binom(this.n, this.r);
        this.reset();
    }

    public void reset() {
        set = new HashSet<Variable>(r);
        for (int i=0; i<a.length; i++) {
            a[i] = i;
        }
        left = total;
    }

    public boolean hasNext() {
        return left > 0;
    }

    public Set<Variable> next() {
        if (left == total || left < 0) {
            left--;
            genSet();
            return set;
        }

        int i = r-1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i] = a[i] + 1;
        for (int j = i+1; j<r; j++) {
            a[j] = a[i] + j - i;
        }
        left--;

        genSet();
        return set;
    }

    private void genSet() {
        set.clear();
        for (int i=0; i<a.length; i++) {
            set.add(vars[a[i]]);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static int binom(int n, int r) {
        if (r > n) {
            return 1;
        }
        if (n < 1) {
            throw new IllegalArgumentException();
        }

        int[] b = new int[n+1];
        b[0] = 1;
        for (int i=1; i<=n; i++) {
            b[i] = 1;
            for (int j=i-1; j>0; j--) {
                b[j] += b[j-1];
            }
        }
        return b[r];
    }

    int size() {
        return total;
    }

}
