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

package es.csic.iiia.dcop.mp;

import java.util.ArrayList;

/**
 * Cycle Based Runtime result collector.
 *
 * @param <R>
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class DefaultResults<R extends Result> implements Results<R> {


    /**
     * Resulting beliefs
     */
    private ArrayList<R> results;

    /**
     * Number of iterations consumed.
     */
    private int iterations;

    /**
     * List of maximal cycle checks for each cycle.
     */
    private ArrayList<Long> maximalCc;

    /**
     * List of total cycle checks per cycle.
     */
    private ArrayList<Long> totalCc;

    /**
     * List of maximal bytes sent for each cycle.
     */
    private ArrayList<Long> maximalBytes;

    /**
     * List of total bytes sent per cycle.
     */
    private ArrayList<Long> totalBytes;
    
    /**
     * List of maximal memory bytes for each cycle.
     */
    private ArrayList<Long> maximalMemory;

    /**
     * Cumulative maximal cycle checks.
     */
    private long maximalCcc;

    /**
     * Cumulative total cycle checks.
     */
    private long totalCcc;

    /**
     * Cumulative maximal sent bytes.
     */
    private long maximalBytesc;

    /**
     * Cumulative total sent bytes.
     */
    private long totalBytesc;
    
    /**
     * Cumulative maximal memory bytes.
     */
    private long maximalMemoryc;
    

    public long getMaximalCcc() {
        return maximalCcc;
    }

    public long getTotalCcc() {
        return totalCcc;
    }

    public long getMaximalBytesc() {
        return maximalBytesc;
    }

    public long getTotalBytesc() {
        return totalBytesc;
    }
    
    public long getMaximalMemoryc() {
        return maximalMemoryc;
    }

    /**
     * Gets the number of iterations used by the algorithm run.
     * @return number of iterations.
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Constructs a new GDL results holder object.
     */
    public DefaultResults() {
        this.results       = new ArrayList<R>();
        this.totalCc       = new ArrayList<Long>();
        this.maximalCc     = new ArrayList<Long>();
        this.totalBytes    = new ArrayList<Long>();
        this.maximalBytes  = new ArrayList<Long>();
        this.maximalMemory = new ArrayList<Long>();
    }

    /**
     * Adds a new cycle to the results.
     *
     * @param maxCc Maximal cycle checks for a single agent during this cycle.
     * @param totalCc Total cycle checks amongs all agents during this cycle.
     */
    public void addCycle(long maxCc, long totalCc, long maxBytes, long totalBytes,
            long maxMemory) {
        iterations++;

        this.maximalCc.add(maxCc);
        this.totalCc.add(totalCc);
        this.maximalBytes.add(maxBytes);
        this.totalBytes.add(totalBytes);
        this.maximalMemory.add(maxMemory);

        this.maximalCcc += maxCc;
        this.totalCcc += totalCc;
        this.maximalBytesc += maxBytes;
        this.totalBytesc += totalBytes;
        this.maximalMemoryc += maxMemory;
    }

    /**
     * Merges the given results object with this one.
     *
     */
    public void mergeResults(DefaultResults other) {
        for (int i=0; i<other.iterations; i++) {
            addCycle(
                    (Long)other.maximalCc.get(i),
                    (Long)other.totalCc.get(i),
                    (Long)other.maximalBytes.get(i),
                    (Long)other.totalBytes.get(i),
                    (Long)other.maximalMemory.get(i)
            );
        }
    }

    /**
     * Returns the <abbr title="Cycle Based Runtime">CBR</abbr> time of the
     * algorithm run, using <em>L = communicationCost * t</em>.
     *
     * @param communicationCost communication cost in respect to cycle check
     * cost.
     * @return cycle based runtime cost (in cycle checks).
     */
    public long getCBR(int communicationCost) {
        return maximalCcc + communicationCost * iterations;
    }

    /**
     * Adss a new resulting belief to the structure.
     *
     * @param result resulting belief.
     */
    public void add(R result) {
        results.add(result);
    }

    public double getLoadFactor() {
        return maximalCcc/(double)totalCcc;
    }

    /**
     * Gets the list of resulting beliefs.
     *
     * @return list of resulting beliefs.
     */
    public ArrayList<R> getResults() {
        return results;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("R: ");
        buf.append(iterations);
        buf.append("i, ");
        buf.append(maximalCcc);
        buf.append("ccc, ");
        buf.append(maximalCcc/(double)totalCcc);
        buf.append("lf");
        for(Result r : results) {
            buf.append("\n");
            buf.append(r);
        }
        return buf.toString();
    }
    
}
