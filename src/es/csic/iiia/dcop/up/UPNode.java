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

package es.csic.iiia.dcop.up;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.mp.AbstractNode;
import es.csic.iiia.dcop.mp.Edge;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Utility Propagation message passing algorithm node.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class UPNode<E extends Edge, R extends UPResult> extends
        AbstractNode<E, R> {

    /**
     * Clique's member variables.
     */
    protected HashSet<Variable> variables;

    /**
     * Potential of this clique.
     */
    protected CostFunction potential;

    /**
     * Belief of this clique.
     */
    protected CostFunction belief;

    /**
     * Factor factory to use.
     */
    protected CostFunctionFactory factory;

    /**
     * Relations assigned to this clique.
     */
    protected ArrayList<CostFunction> relations;

    /**
     * Converged flag.
     */
    protected boolean converged = false;

    /**
     * Constructs a new clique with the specified member variable and null
     * potential.
     *
     * @param variable member variable of this clique.
     */
    public UPNode(Variable variable) {
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
    public UPNode(CostFunction potential) {
        super();
        this.relations = new ArrayList<CostFunction>(1);
        this.relations.add(potential);
        this.variables = new HashSet<Variable>(potential.getVariableSet());
    }

    /**
     * Constructs a new empty clique.
     */
    public UPNode() {
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
     * Checks if this clique has converged in the last iteration (it's current
     * and previous beliefs are equal).
     *
     * @return true if this clique has converged or false otherwise.
     */
    public boolean isConverged() {
        return converged;
    }

    public int getSize() {
        // Now we have to do it the hard way...
        int size = 1;
        for (Variable v : variables) {
            size *= v.getDomain();
        }
        return size;
    }

    /**
     * @return the factory
     */
    public CostFunctionFactory getFactory() {
        return factory;
    }

    /**
     * @param factory the factory to set
     */
    public void setFactory(CostFunctionFactory factory) {
        this.factory = factory;
    }

    public CostFunction getPotential() {
        return potential;
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
    
}