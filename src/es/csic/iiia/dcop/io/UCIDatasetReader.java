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

package es.csic.iiia.dcop.io;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.Variable;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marc Pujol (mpujol at iiia.csic.es)
 */
public class UCIDatasetReader {
    
    private static Logger log = LoggerFactory.getLogger(UCIDatasetReader.class);
    
    Matcher agent = Pattern.compile("(?i)^AGENT\\s+(\\d+)\\s*$").matcher("");
    Matcher variable = Pattern.compile("(?i)^VARIABLE\\s+(\\w+)\\s+(\\w+)\\s+(\\d+)").matcher("");
    Matcher constraint = Pattern.compile("(?i)^CONSTRAINT(\\s+\\w+)+").matcher("");
    Matcher nogood = Pattern.compile("(?i)^NOGOOD\\s+(\\d+)\\s+(\\d+)\\s*$").matcher("");
    Matcher f = Pattern.compile("(?i)^F\\s+").matcher("");

    private HashMap<String, Variable> variables;
    private ArrayList<CostFunction> factors;
    private CostFunction lastFactor;
    private CostFunctionFactory factory;

    public UCIDatasetReader() {
        this.variables = new HashMap<String, Variable>();
        this.factors = new ArrayList<CostFunction>();
    }

    public List<CostFunction> read(BufferedReader input, CostFunctionFactory factory, String line) {
        this.factory = factory;

        try {

            try {

                this.parseLine(line.toLowerCase());
                /*
                * readLine is a bit quirky :
                * it returns the content of a line MINUS the newline.
                * it returns null only for the END of the stream.
                * it returns an empty String if two newlines appear in a row.
                */
                while (( line = input.readLine()) != null){
                    this.parseLine(line.toLowerCase());
                }
            } finally {
                input.close();
            }

        } catch (IOException ex){
            throw new RuntimeException(ex.getLocalizedMessage());
        }

        this.factory = null;
        return factors;
    }

    private void parseLine(String line) {

        Method m = getParsingMethod(line);
        if (m != null) {
            try {
                m.invoke(this, line);
            } catch (Exception ex) {
                System.err.println("Conflict line: " + line);
                ex.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("[WARNING] Ignored line: " + line);
        }
    }

    public void parseAgent(String line) {
        //if (agent.reset(line).find()) {}
    }

    public void parseVariable(String line) {
        if (!variable.reset(line).find()) {
            return;
        }
        Variable v = new Variable(variable.group(1),
                Integer.valueOf(variable.group(3)));
        variables.put(variable.group(1), v);
    }

    public void parseConstraint(String line) {
        if (!constraint.reset(line).find()) {
            return;
        }

        String[] parts = line.split("\\s+");
        ArrayList<Variable> vars = new ArrayList<Variable>(parts.length-1);
        for(int i=1; i<parts.length; i++) {
            final Variable var = variables.get(parts[i]);
            if (var != null) {
                vars.add(var);
            } else {
                log.warn("Found unknown variable \"" + parts[i] + "\" in constraint line: " + line);
            }
        }

        lastFactor = factory.buildCostFunction(vars.toArray(new Variable[0]), 0);
        factors.add(lastFactor);
    }

    public void parseNogood(String line) {
        if (!nogood.reset(line).find()) {
            return;
        }

        lastFactor.setValue(new int[] {
            Integer.valueOf(nogood.group(1)),
            Integer.valueOf(nogood.group(2)),
        }, 1);
    }

    public void parseF(String line) {
        if (!f.reset(line).find()) {
            return;
        }

        String[] parts = line.split("\\s+");
        int[] idx = new int[parts.length-2];
        for(int i=1; i<parts.length-1; i++) {
            idx[i-1] = Integer.valueOf(parts[i]);
        }

        if (lastFactor == null) return;
        lastFactor.setValue(idx, Double.valueOf(parts[parts.length-1]));
    }

    private Method getParsingMethod(String line) {
        // Construct parsing method name
        String mn = line.split("\\s+", 2)[0].toLowerCase();
        if (mn.length() == 0) return null;
        
        mn = "parse" + mn.substring(0, 1).toUpperCase() + mn.substring(1);

        // Look for it
        Method m = null;
        try {
            m = getClass().getMethod(mn, new Class[]{String.class});
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return m;
    }

}
