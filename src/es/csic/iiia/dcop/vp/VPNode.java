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

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.ValuesArray;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.mp.AbstractNode;
import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import es.csic.iiia.dcop.vp.strategy.VPStrategy.MappingResults;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value Propagation algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VPNode extends AbstractNode<VPEdge, VPResult> {

    private static Logger log = LoggerFactory.getLogger(VPGraph.class);
    
    private ArrayList<VariableAssignment> mappings;
    private VPStrategy strategy;
    private ArrayList<Integer> upMappings;

    private UPNode upnode;

    public VPNode(UPNode n) {
        upnode = n;
    }

    @Override
    public void initialize() {
        setMode(Modes.TREE_DOWN);
    }

    public long run() {
        long cc = 0;

        // Receive incoming messages
        mappings = new ArrayList<VariableAssignment>();
        for(VPEdge e : getEdges()) {
            VPMessage msg = e.getMessage(this);
            if (msg != null) {
                mappings = msg.getMappings();
                break;
            }
        }

        // Take our decision
        long time = System.currentTimeMillis();
        MappingResults r = strategy.getExtendedMappings(mappings, upnode);
        time = System.currentTimeMillis() - time;
        if (time > 100) {
            log.info("Solution expansion time: " + time);
        }
        mappings = r.getMappings();
        upMappings = r.getuMap();

        // Send messages
        for(VPEdge e : getEdges()) {
            if (!readyToSend(e))
                continue;

            VPMessage msg = new VPMessage(mappings);
            e.sendMessage(this, msg);
        }

        setUpdated(false);
        return cc;
    }


    public boolean isConverged() {
        // As convergence is checked after the first iteration and this is a
        // tree, it is granted.
        return true;
    }

    public VPResult end() {
        if (log.isTraceEnabled()) {
            log.trace("B:" + upnode.getBelief());
        }
        if (log.isDebugEnabled()) {
            boolean fail = false;
            Set<Variable> vars = upnode.getVariables();
            for (VariableAssignment a : mappings) {
                for (Variable v : vars) {
                    if (!a.containsKey(v)) {
                        fail = true;
                        break;
                    }
                }
                if (fail) {
                    System.out.println("Failed completess! " + a);
                    System.out.println("Variables: " + vars);
                }
            }
            if (!fail) {
                System.out.println("Checking map completeness: ok");
            }
        }
        return new VPResult(mappings);
    }

    public String getName() {
        return upnode.getName();
    }

    @Override
    public String toString() {
        return upnode.getName();
    }

    @Override
    protected boolean sentOrReceivedAnyEdge() {
        boolean res = super.sentOrReceivedAnyEdge();
        if (res && log.isTraceEnabled()) {
            log.trace(this + " done.");
        }
        return res;
    }
    
    public ValuesArray getGlobalValues() {
        final ValuesArray values = new ValuesArray(mappings.size());
        final CostFunction.Combine op = upnode.getFactory().getCombineOperation();
        Collection<CostFunction> fs = upnode.getRelations();

        for (VariableAssignment map : mappings) {
            if (log.isTraceEnabled()) {
                log.trace(map.toString());
            }
            
            double value = 0;
            for (CostFunction f : fs) {
                final double v = f.getValue(map);
                value = op.eval(value, v);
            }
            values.add(value);
        }
        return values;
    }

    public VariableAssignment getMapping(int index) {
        return mappings.get(index);
    }

    public ArrayList<Integer> getUpMappings() {
        return upMappings;
    }

    public UPNode getUPNode() {
        return upnode;
    }

    public CostFunctionFactory getFactory() {
        return upnode.getFactory();
    }

    /**
     * @param strategy the strategy to set
     */
    public void setStrategy(VPStrategy strategy) {
        this.strategy = strategy;
    }

}
