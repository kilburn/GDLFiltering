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

package es.csic.iiia.iea.ddm.cli;

import es.csic.iiia.iea.ddm.CostFunction;
import gnu.getopt.Getopt;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Cli {

    private static final String programName = "ddm";

    public static void showUsage() {
        System.err.println("Usage: " + programName + " [input] [output]");
        System.exit(0);
    }

    public static void main(String[] argv) {

        // Argument parsing...
        Getopt g = new Getopt(programName, argv, "a:c:e:f::g::hm:n:r::s:t:");
        CliApp cli = new CliApp();

        int c=0;
        String arg;
        while ((c = g.getopt()) != -1) {
            switch(c) {

                case 'a':
                    arg = g.getOptarg();
                    if (arg.equals("jt"))
                        cli.setAlgorithm(CliApp.ALGO_JUNCTION_TREE);
                    else if (arg.equals("maxsum"))
                        cli.setAlgorithm(CliApp.ALGO_MAX_SUM);
                    else {
                        System.err.println("Error: invalid algorithm \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'c':
                    arg = g.getOptarg();
                    if (arg.equals("sum"))
                        cli.setCombineOperation(CostFunction.COMBINE_SUM);
                    else if (arg.equals("prod"))
                        cli.setCombineOperation(CostFunction.COMBINE_PRODUCT);
                    else {
                        System.err.println("Error: invalid combine operation \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'e':
                    arg = g.getOptarg();
                    if (arg.equals("mcn"))
                        cli.setHeuristic(CliApp.JT_HEURISTIC_MCN);
                    else if (arg.equals("mcs"))
                        cli.setHeuristic(CliApp.JT_HEURISTIC_MCS);
                    else {
                        System.err.println("Error: invalid heuristic \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'f':
                    cli.setCreateFactorGraph(true);
                    arg = g.getOptarg();
                    if (arg != null) {
                        cli.setFactorGraphFile(arg);
                    }
                    break;

                case 'g':
                    cli.setCreateCliqueGraph(true);
                    arg = g.getOptarg();
                    if (arg != null) {
                        cli.setCliqueGraphFile(arg);
                    }
                    break;


                case 's':
                    arg = g.getOptarg();
                    if (arg.equals("min"))
                        cli.setSummarizeOperation(CostFunction.SUMMARIZE_MIN);
                    else if (arg.equals("max"))
                        cli.setSummarizeOperation(CostFunction.SUMMARIZE_MAX);
                    else {
                        System.err.println("Error: invalid summarize operation \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'm':
                    arg = g.getOptarg();
                    int max = Integer.parseInt(arg);
                    if (max < 1) {
                        System.err.println("Error: you need to specify a maximum number variables per clique.");
                        System.exit(0);
                    }
                    cli.setMaxCliqueVariables(max);
                    break;

                case 'r':
                    arg = g.getOptarg();
                    if (arg == null) {
                        arg = "0.001";
                    }
                    double variance = Double.parseDouble(arg);
                    cli.setRandomVariance(variance);
                    break;


                case 'n':
                    arg = g.getOptarg();
                    if (arg.equals("none"))
                        cli.setNormalization(CostFunction.NORMALIZE_NONE);
                    else if (arg.equals("sum1"))
                        cli.setNormalization(CostFunction.NORMALIZE_SUM1);
                    else if (arg.equals("sum0"))
                        cli.setNormalization(CostFunction.NORMALIZE_SUM0);
                    else {
                        System.err.println("Error: invalid heuristic \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 't':
                    arg = g.getOptarg();
                    max = Integer.parseInt(arg);
                    if (max < 1) {
                        System.err.println("Error: you need to specify a maximum number of tries.");
                        System.exit(0);
                    }
                    cli.setMaxJunctionTreeTries(max);
                    break;

                case 'h':
                    System.err.println("Usage: " + programName + "[options] [input] [output]");
                    System.err.println("Solves the DCOP problem described in <input> file and stores the found solution to <output> file.");
                    System.err.println("Options:");
                    System.err.println("  -a algorithm, --algorithm=algorithm");
                    System.err.println("    Uses the specified algorithm, where algorithm is one of: ");
                    System.err.println("      - jt : junction tree optimal solver.");
                    System.err.println("      - maxsum : max-sum approximation.");
                    System.err.println("  -c operation, --combine=operation");
                    System.err.println("    Uses the specified combining operator, where operator is one of: ");
                    System.err.println("      - sum : combine using addition.");
                    System.err.println("      - prod : combine using product.");
                    System.err.println("  -e heuristic, --heuristic=heuristic");
                    System.err.println("    Uses the specified heuristic function to build the Junction Tree,\n    where it is one of: ");
                    System.err.println("      - mcn : chooses the most connected node next, randomly breaking ties.");
                    System.err.println("      - mcs : chooses the most related node next, then mcn to break ties.");
                    System.err.println("  -f [graphFile], --factor-graph[=graphFile]");
                    System.err.println("    Outputs the factor graph representation in .dot format to [graphFile],\n    or \"fgraph.dot\" if unspecified.");
                    System.err.println("  -g [graphFile], --clique-graph[=graphFile]");
                    System.err.println("    Outputs the clique graph representation in .dot format to [graphFile],\n    or \"cgraph.dot\" if unspecified.");
                    System.err.println("  -h, --help");
                    System.err.println("    Displays this help message.");
                    System.err.println("  -n mode, --normalize=mode");
                    System.err.println("    Uses the specified normalization mode, where it is one of: ");
                    System.err.println("      - none : do not perform any normalization (default for junction tree algorithm).");
                    System.err.println("      - sum0 : values are normalized to sum zero (default for maxsum).");
                    System.err.println("      - sum1 : values are normalized to sum one.");
                    System.err.println("  -r [variance]");
                    System.err.println("    Adds random noise with <variance> variance, or 0.001 if unspecified.");
                    System.err.println("  -s operation, --summarize=operation");
                    System.err.println("    Uses the specified summarizing operator, where operator is one of: ");
                    System.err.println("      - min : summarizes using the minimum value (for costs).");
                    System.err.println("      - max : summarizes using the maximum value (for utilites).");
                    System.err.println("  -t <tries>");
                    System.err.println("    Maximum number of junction trees built trying to minimize complexity.");
                    System.err.println();
                    System.exit(0);
                    break;
                    
                default:
                    showUsage();
                    break;
            }
        }

        // Get the input/output file(s)
        c = g.getOptind();
        try {
            
            // No data in stdin and no input file -> error
            if (c >= argv.length && System.in.available() == 0) {
                showUsage();
            }

            if (c < argv.length) {
                cli.setInputFile(new File(argv[c++]));
            }

            if (c < argv.length) {
                cli.setOutputFile(new File(argv[c++]));
            }

        } catch(IOException ex) {
            System.err.println(ex.getLocalizedMessage());
        }

        // All ready, now run!
        cli.run();
    }

}
