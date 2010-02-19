/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2009, IIIA-CSIC, Artificial Intelligence Research Institute
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

package es.csic.iiia.iea.ddm.cg;

import es.csic.iiia.iea.ddm.CostFunction;
import es.csic.iiia.iea.ddm.HypercubeCostFunction;
import es.csic.iiia.iea.ddm.Variable;
import es.csic.iiia.iea.ddm.mp.AbstractNode;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CgNode extends AbstractNode<CgEdge, CgResult> {

    /**
     * Helper array to get the correct type when using ArrayList.toArray(T[])
     */
    private static final Variable[] vars = new Variable[]{};

    /**
     * Clique's member variables.
     */
    private HashSet<Variable> variables;

    /**
     * Potential of this clique.
     */
    private CostFunction potential;

    public CostFunction getPotential() {
        return potential;
    }

    /**
     * Belief of this clique.
     */
    private CostFunction belief;

    /**
     * Summarize operation to use.
     */
    private CostFunction.Summarize summarizeOperation;

    /**
     * Combine operation to use.
     */
    private CostFunction.Combine combineOperation;

    /**
     * Normalization type to use.
     */
    private CostFunction.Normalize normalizationType;

    /**
     * Tolerance to use when comparing the previous and current beliefs.
     */
    private double tolerance = 0.0001;

    /**
     * Relations assigned to this clique.
     */
    private ArrayList<CostFunction> relations;


    private boolean converged = false;

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public CgNode(Variable variable) {
        super();
        this.relations = new ArrayList<CostFunction>(0);
        this.variables = new HashSet<Variable>(1);
        this.addVariable(variable);
    }

    /**
     * Constructs a new clique with the specified potential.
     *
     * The potential variables are automatically extracted and added as
     * member variables of this clique.
     *
     * @param potential potential of the clique.
     */
    public CgNode(CostFunction potential) {
        super();
        this.relations = new ArrayList<CostFunction>(1);
        this.relations.add(potential);
        this.variables = new HashSet<Variable>(potential.getVariableSet());
    }

    /**
     * Constructs a new empty clique.
     */
    public CgNode() {
        super();
        this.relations = new ArrayList<CostFunction>();
        this.variables = new HashSet<Variable>();
    }

    /**
     * Adds a new variable to the member variables of this class.
     *
     * @param variable new member variable.
     */
    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    /**
     * Checks if this clique contains the specified variable as a member.
     *
     * @param variable variable to look for.
     * @return true if this clique contains the specified variable or false
     * otherwise
     */
    public boolean contains(Variable variable) {
        return this.variables.contains(variable);
    }

    /**
     * Gets the whole member variables set.
     *
     * @return member variable set.
     */
    public HashSet<Variable> getVariables() {
        return this.variables;
    }

    /**
     * Adds a new relation to this clique.
     *
     * @param relation relation to add.
     */
    public void addRelation(CostFunction relation) {
        this.relations.add(relation);
        this.variables.addAll(relation.getVariableSet());
    }

    /**
     * Gets the relations assigned to this clique.
     *
     * @return clique's relations.
     */
    public ArrayList<CostFunction> getRelations() {
        return relations;
    }

    /**
     * "Initializes" this clique, setting the summarize, combine and
     * normalization operations to use as well as sending it's initial messages.
     */
    public void initialize() {

        // Calculate our potential
        potential = null;
        for (CostFunction f : relations) {
            potential = f.combine(potential, combineOperation);
        }
        if (potential == null) {
            potential = new HypercubeCostFunction(new Variable[0]);
        }

        belief = potential.buildCostFunction(variables.toArray(vars),
                combineOperation.getNeutralValue());
        belief = potential.combine(belief, combineOperation);

        // Send initial messages
        for (CgEdge e : getEdges()) {
            CostFunction msg;
            final Variable[] ev = e.getVariables();
            if (potential != null) {
                msg = belief.summarize(ev, summarizeOperation);
            } else {
                msg = potential.buildCostFunction(ev,
                        combineOperation.getNeutralValue());
            }
            e.sendMessage(this, new CgMessage(msg));
        }
    }

    /**
     * Performs one "step" of the GDL algorithm, updating the clique's belief
     * and sending new messages to it's neighboors.
     *
     * @return number of constraint checks consumed.
     */
    public long run() {

        // CC count
        long cc = 0;

        // Combine incoming messages
        CostFunction combi = potential.buildCostFunction(variables.toArray(vars),
                combineOperation.getNeutralValue());
        for (CgEdge e : getEdges()) {
            CostFunction msg = e.getMessage(this).getFactor();
            combi = combi.combine(msg, combineOperation);
            cc += combi.getSize();
        }

        // Compute our belief
        CostFunction previousBelief = belief;
        this.belief = combi.combine(this.potential, combineOperation);
        cc += belief.getSize();
        this.belief.normalize(normalizationType);
        cc += belief.getSize();

        // Send updated messages
        for (CgEdge e : getEdges()) {

            // Instead of multiplying all incoming messages except the one
            // from e, we "substract" the e message from the belief, which
            // has the same result but with fewer operations.
            CostFunction inMsg = potential.buildCostFunction(e.getMessage(this).getFactor());
            inMsg.negate(combineOperation);
            CostFunction msg = belief.combine(inMsg, combineOperation);
            cc += msg.getSize();
            msg = msg.summarize(e.getVariables(), summarizeOperation);
            cc += msg.getSize();

            e.sendMessage(this, new CgMessage(msg));
        }

        converged = belief.equals(previousBelief);
        setUpdated(false);
        return cc;
    }

    /**
     * Checks if this clique has converged in the last iteration (it's current
     * and previous beliefs are equal).
     *
     * @return true if this clique has converged or false otherwise.
     */
    public boolean isConverged() {
        return converged;
    }

    /**
     * Retrieves the belief of this clique.
     *
     * @return belief of this clique.
     */
    public CostFunction getBelief() {
        return belief;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(getName());
        
        buf.append("[");
        int i=0;
        for (CostFunction f : relations){
            if (i++>0) buf.append(",");
            buf.append(f.getName());
        }
        buf.append("] P:");
        buf.append(potential == null ? "null" : potential.toString());
        buf.append(" - B:");
        buf.append(belief == null ? "null" : belief.toString());
        buf.append(")");

        return buf.toString();
    }

    public String getName() {
        StringBuffer buf = new StringBuffer();

        buf.append("");
        int i=0;
        for (Variable v : variables) {
            if (i++>0) buf.append(".");
            buf.append(v.getName());
        }
        buf.append("");

        return buf.toString();
    }

    public CgResult end() {
        return new CgResult(this);
    }

    public CostFunction.Combine getCombineOperation() {
        return combineOperation;
    }

    public void setCombineOperation(CostFunction.Combine combineOperation) {
        this.combineOperation = combineOperation;
    }

    public CostFunction.Normalize getNormalizationType() {
        return normalizationType;
    }

    public void setNormalizationType(CostFunction.Normalize normalizationType) {
        this.normalizationType = normalizationType;
    }

    public CostFunction.Summarize getSummarizeOperation() {
        return summarizeOperation;
    }

    public void setSummarizeOperation(CostFunction.Summarize summarizeOperation) {
        this.summarizeOperation = summarizeOperation;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public int getSize() {
        // Now we have to do it the hard way...
        int size = 1;
        for (Variable v : variables) {
            size *= v.getDomain();
        }
        return size;
    }

}
