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
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.util.metrics.Metric;
import es.csic.iiia.dcop.util.metrics.Norm0;
import es.csic.iiia.dcop.util.metrics.Norm1;
import es.csic.iiia.dcop.util.metrics.Norm2;
import es.csic.iiia.dcop.util.metrics.NormInf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CostFunctionStats {

    public static int getExp(CostFunction o1) {
        int sid = 0;
        for (Variable v :o1.getVariableSet()) {
            final int id = v.getId();
            sid += id;
        }
        return sid;
    }

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

    /**
     * Test stuff
     */
    public static double getEntropy(CostFunction f) {
        // Normalize to probabilities
        final CostFunctionFactory factory = f.getFactory();
        CostFunction f2 = factory.buildCostFunction(f);
        CostFunction.Normalize n = factory.getNormalizationType();
        factory.setNormalizationType(CostFunction.Normalize.SUM1);
        f2.normalize();
        System.out.println(f2);
        factory.setNormalizationType(n);

        // Compute entropy
        final double log2 = Math.log(2);
        double e = 0;
        for (Iterator<Integer> i = f2.iterator(); i.hasNext();) {
            double p = f2.getValue(i.next());
            if (p != 0) {
                e += p * (Math.log(p)/log2);
            }
        }

        System.out.println(e);
        return -e;
    }
    
    public static double getSum(CostFunction f) {
        double s = 0;
        for (Iterator<Integer> i = f.iterator(); i.hasNext();) {
            s += f.getValue(i.next());
        }
        return s;
    }

    public static double[] getInformationGains(CostFunction f) {
        Set<Variable> vars = f.getVariableSet();
        if (vars.size() < 2) {
            return new double[]{Double.NaN};
        }

        double[] gains = new double[vars.size()];
        Variable[] vs = vars.toArray(new Variable[0]);

        final double orig = getSum(f);
        for (int i=0; i<vs.length; i++) {
            vars.remove(vs[i]);
            double proj = getSum(f.summarize(vars.toArray(new Variable[0])));
            proj = proj * vs[i].getDomain();

            switch(f.getFactory().getSummarizeOperation()) {
                case MIN:
                    gains[i] = orig - proj;
                    break;
                case MAX:
                    gains[i] = proj - orig;
                    break;
            }

            vars.add(vs[i]);
        }

        return gains;
    }

    public static double getGain(CostFunction f, Variable[] vars) {
        Metric m = new Norm1();
        CostFunction res = f.summarize(vars);
        res = res.summarize(f.getVariableSet().toArray(new Variable[0]));
        res.negate();
        res = f.combine(res);
        return m.getValue(res);
    }

    public static CostFunction[] getBestApproximation(CostFunction f, 
            int r, Metric m, int n) {
        ArrayList<CostFunction> res = new ArrayList<CostFunction>();
        Variable[] vars = f.getVariableSet().toArray(new Variable[0]);

        if (vars.length <= r) {
            res.add(f);
            res.add(f.getFactory().buildCostFunction(vars));
            return res.toArray(new CostFunction[0]);
        }

        //System.out.println("    f: " + f);

        // Up to a maximum of n functions...
        for (int i=0; i<n; i++) {
            CombinationGenerator g = new CombinationGenerator(vars, r);
            Variable[] vs = new Variable[r];

            // For each possible combination
            int j = 0, len = g.size();
            CostFunction[] cfl = new CostFunction[len];
            TreeMap<Double, Integer> cfm = new TreeMap<Double, Integer>();
            while(g.hasNext()) {

                // Fetch the summarization
                Set<Variable> vss = g.next();
                vs = vss.toArray(new Variable[0]);
                cfl[j] = f.summarize(vs);

                // Compute the metric of the difference and store it (sorted)
                CostFunction neg = f.getFactory().buildCostFunction(cfl[j]);
                neg.negate();
                neg = neg.summarize(vars);
                double v = m.getValue(f.combine(neg));
                cfm.put(v, j);
                //System.out.println("V: " + formatValue(v) + "\tC: " + cfl[j]);

                j++;
            }

            // Extract the best (lowest score) candidate
            Double score = cfm.firstKey();
            CostFunction chosen = cfl[cfm.get(score)];

            // If the chosen function is all zeros, we can not approximate
            // any further
            if (new Norm1().getValue(chosen) < 1e-5) {
                break;
            }

            // Recalculate the original function
            res.add(chosen);
            //System.out.println("Chose: " + chosen);
            CostFunction neg = chosen.summarize(vars);
            neg.negate();
            //System.out.println("Chose: " + neg);
            f = f.combine(neg);
            //System.out.println("New f: " + f);
            
            if (score < 1e-5) {
                // Got an exact value! :)
                break;
            }
        }

        res.add(f);
        return res.toArray(new CostFunction[0]);
    }

    public static CostFunction[] getVotedBestApproximation(CostFunction f,
            int r, int n) {

        Metric[] metrics = new Metric[]{
            new Norm0(),
            new Norm1(),
            new Norm2(),
            new NormInf(),
        };
        final int len = metrics.length;
        int[] votes = new int[len];
        double mins[] = new double[]{Double.POSITIVE_INFINITY, 
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY};
        double ds[][] = new double[len][len];
        CostFunction res[][] = new CostFunction[len][];

        for (int i=0; i<len; i++) {
            // Get and evaluate possible approximations
            res[i] = getBestApproximation(f, r, metrics[i], n);
            final CostFunction rem = res[i][res[i].length-1];
            for (int j=0; j<len; j++) {
                ds[i][j] = metrics[j].getValue(rem);
            }
        }

        // Calculate the minimums
        for (int i=0; i<len; i++) {
            for (int j=0; j<len; j++) {
                mins[i] = Math.min(mins[i], ds[j][i]);
            }
        }

        // Vote
        for (int i=0; i<len; i++) {
            for (int j=0; j<len; j++) {
                if (ds[i][j] == mins[j])
                    votes[i]++;
            }
        }

        // Retrieve winner
        int max = 0, idx = 0;
        for (int i=0; i<len; i++) {
            if (votes[i] > max) {
                max = votes[i];
                idx = i;
            }
        }

        return res[idx];
    }

}
