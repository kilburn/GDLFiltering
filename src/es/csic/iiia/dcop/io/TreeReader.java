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
import es.csic.iiia.dcop.Variable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class TreeReader {

    Matcher node = Pattern.compile("(?i)^NODE\\s+(\\w+)$").matcher("");
    Matcher f = Pattern.compile("(?i)^F(\\s+\\w+)+$").matcher("");
    Matcher link = Pattern.compile("(?i)^LINK\\s+(\\w+)\\s+(\\w+)").matcher("");

    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    private Node currentNode;
    private HashMap<String, String> links = new HashMap<String, String>();

    // tree results
    private CostFunction[][] factorDistribution;
    private char[][] adjacency;

    public void parseNode(String line) {
        if (!node.reset(line).find()) {
            return;
        }

        String id = node.group(1);
        currentNode = new Node(id);
        nodes.put(id, currentNode);
    }

    public void parseF(String line) {
        if (!f.reset(line).find()) {
            return;
        }

        currentNode.addFunction(line.trim());
    }

    public void parseLink(String line) {
        if (!link.reset(line).find()) {
            return;
        }

        links.put(link.group(1), link.group(2));
    }

    public void read(InputStream problem, CostFunction[] factors) {

        // Start by reading the files contents
        parse(problem);

        // And now map the obtained info to an array of costfunctions +
        // the adjacency matrix.
        HashMap<HashSet<String>, CostFunction> functions =
                new HashMap<HashSet<String>, CostFunction>();
        for (CostFunction factor : factors) {
            HashSet<String> vars = new HashSet<String>();
            for (Variable v : factor.getVariableSet()) {
                vars.add(v.getName());
            }
            functions.put(vars, factor);
        }

        factorDistribution = new CostFunction[nodes.size()][];
        HashMap<Node, Integer> nodeToIdx = new HashMap<Node, Integer>();
        int i=0;
        for (Node n : nodes.values()) {
            nodeToIdx.put(n, i);
            factorDistribution[i] = new CostFunction[n.fns.size()];
            for(int j=0; j<n.fns.size(); j++) {
                HashSet<String> foo = n.fns.get(j);
                if (functions.get(foo) == null) {
                    System.err.println("Function not found?");
                }
                factorDistribution[i][j] = functions.get(foo);
            }
            i++;
        }

        // And finally the adjacency matrix
        adjacency = new char[nodes.size()][nodes.size()];
        for (String n1 : links.keySet()) {
            String n2 = links.get(n1);
            int idx1 = nodeToIdx.get(nodes.get(n1));
            int idx2 = nodeToIdx.get(nodes.get(n2));
            adjacency[idx1][idx2] = 1;
        }

        return;
    }

    public CostFunction[][] getFactorDistribution() {
        return factorDistribution;
    }

    public char[][] getAdjacency() {
        return adjacency;
    }

    private void parse(InputStream problem) {
        try {

            //use buffering, reading one line at a time
            BufferedReader input =  new BufferedReader(new InputStreamReader(problem));
            try {

                String line = null; //not declared within while loop
                /*
                * readLine is a bit quirky :
                * it returns the content of a line MINUS the newline.
                * it returns null only for the END of the stream.
                * it returns an empty String if two newlines appear in a row.
                */
                while (( line = input.readLine()) != null){
                    this.parseLine(line);
                }

            } finally {
                input.close();
            }

        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void parseLine(String line) {
        Method m = getParsingMethod(line);
        if (m != null) {
            try {
                m.invoke(this, line);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("[WARNING] Ignored line: " + line);
        }
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

    private class Node {
        public final String id;
        public ArrayList<HashSet<String>> fns = new ArrayList<HashSet<String>>();
        public Node(String id) { this.id = id; }
        public void addFunction(String f) {
            String[] parts = f.split("\\s");
            HashSet<String> fn = new HashSet<String>(parts.length-1);
            for (int i=1; i<parts.length; i++) {
                fn.add(parts[i]);
            }
            fns.add(fn);
        }
    }
}
