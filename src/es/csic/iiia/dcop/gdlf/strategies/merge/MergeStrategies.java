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

package es.csic.iiia.dcop.gdlf.strategies.merge;

import es.csic.iiia.dcop.util.metrics.Metric;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton control strategy builder.
 * 
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public enum MergeStrategies {
    CONTENT_BASED (ContentBasedMergeStrategy.class, true),
    SCOPE_BASED (ScopeBasedMergeStrategy.class, false),
    ;
    private static final Logger LOG = Logger.getLogger(MergeStrategies.class.getName());

    private final Class<? extends MergeStrategy> strategy;
    private final boolean usesMetric;
    
    private MergeStrategy instance;
    private static Metric metric;
    
    MergeStrategies(Class<? extends MergeStrategy> c, boolean usesMetric) {
        this.strategy = c;
        this.usesMetric = usesMetric;
    }

    public boolean usesMetric() {
        return usesMetric;
    }

    public Metric getMetric() {
        return metric;
    }
    
    public static void setMetric(Metric m) {
        metric = m;
    }
    
    public MergeStrategy getInstance() {
        if (instance == null) {
            instance = buildInstance();
        }
        
        return instance;
    }
    
    private MergeStrategy buildInstance() {
        MergeStrategy result = null;
        
        if (usesMetric && metric == null) {
            System.err.println("Error: you must specify a metric for the \""
                    + toString().toLowerCase().replace('_', '-') + 
                    "\" merge strategy.");
            System.exit(1);
        }
        
        try {
            if (usesMetric) {
                result = strategy.getDeclaredConstructor(Metric.class).newInstance(metric);
            } else {
                result = strategy.newInstance();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
}
