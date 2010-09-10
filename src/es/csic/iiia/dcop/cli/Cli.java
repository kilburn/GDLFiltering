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

package es.csic.iiia.dcop.cli;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.vp.strategy.OptimalStrategy;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Cli {

    private static final String programName = "dcop";

    public static void showUsage() {
        System.err.println("Usage: " + programName + " [input] [output]");
        System.exit(0);
    }

    public static void showLongUsage() {
        System.err.println("Usage: " + programName + "[options] [input] [output]");
        System.err.println("Solves the DCOP problem described in [input] file (stdin if not specified)");
        System.err.println("and stores the found solution to [output] file (stdout if not specified).");
        System.err.println("Options:");
        System.err.println("  -a algorithm, --algorithm=algorithm (gdl)");
        System.err.println("    Uses the specified algorithm, where algorithm is one of: ");
        System.err.println("      - gdl : GDL over junction tree optimal solver.");
        System.err.println("      - igdl : Iterative GDL over junction tree solver.");
        System.err.println("      - figdl : Filtered IGDL over junction tree solver.");
        System.err.println("      - maxsum : max-sum approximation.");
        System.err.println("  -c operation, --combine=operation (sum)");
        System.err.println("    Uses the specified combining operator, where operator is one of: ");
        System.err.println("      - sum : combine using addition.");
        System.err.println("      - prod : combine using product.");
        System.err.println("  --compress=method (arith)");
        System.err.println("    Uses the specified compression method for sent messages, method iis one of: ");
        System.err.println("      - arith : compress using an arithmetic compressor with a 8-byte PPM model.");
        System.err.println("      - bz2   : compress using a bzip2 compressor.");
        System.err.println("      - none  : do not perform any compression of sent messages.");
        System.err.println("  -e heuristic, --heuristic=heuristic (mcs)");
        System.err.println("    Uses the specified heuristic function to build the Junction Tree,");
        System.err.println("    where heuristic is one of: ");
        System.err.println("      - mcn : chooses the most connected node next, randomly breaking ties.");
        System.err.println("      - mcs : chooses the most related node next, then mcn to break ties.");
        System.err.println("  -f [graphFile], --factor-graph[=graphFile]");
        System.err.println("    Outputs the factor graph representation in .dot format to [graphFile],");
        System.err.println("    or \"fgraph.dot\" if unspecified.");
        System.err.println("  -g [graphFile], --clique-graph[=graphFile]");
        System.err.println("    Outputs the clique graph representation in .dot format to [graphFile],");
        System.err.println("    or \"cgraph.dot\" if unspecified.");
        System.err.println("  -i value, --igdl-r value (2)");
        System.err.println("    Sets the 'r' to value in igdl.");
        System.err.println("  -h, --help");
        System.err.println("    Displays this help message.");
        System.err.println("  -l file, --load-tree=file");
        System.err.println("    Use the tree definition found in <file>.");
        System.err.println("  -m variables, --max-clique-size=variables (14)");
        System.err.println("    Don not try to solve problems with cliques of more than <variables> variables.");
        System.err.println("  -j tries, --jt-tries tries (1)");
        System.err.println("    Number of junction trees to build trying to minimize the maximum clique size.");
        System.err.println("  -n mode, --normalize=mode (none)");
        System.err.println("    Uses the specified normalization mode, where it is one of: ");
        System.err.println("      - none : do not perform any normalization (default for junction tree algorithm).");
        System.err.println("      - sum0 : values are normalized to sum zero (default for maxsum).");
        System.err.println("      - sum1 : values are normalized to sum one.");
        System.err.println("  --nsols=<number> (1)");
        System.err.println("    Sets figdl to test <number> number of possible solutions at each iteration");
        System.err.println("  -p strategy, --partition-strategy=strategy (rank)");
        System.err.println("    Uses the specified approximation strategy, where strategy is one of: ");
        System.err.println("      - scp-cc   : scope-based partitioning communication & computation bounded");
        System.err.println("      - scp-c    : scope-based partitioning communication bounded");
        System.err.println("      - lre-cc   : content-based local relative error strategy (communication and computation bounded).");
        System.err.println("      - lre-c    : content-based local relative error strategy (just communication bounded).");
        //System.err.println("      - rankdown : merges functions from highest ranked (max-min value) to lowest.");
        System.err.println("      - greedy-d : greedy decomposition.");
        System.err.println("      - zeros-d  : zero-avoiding decomposition.");
        System.err.println("  -r [variance], --random-noise[=variance]");
        System.err.println("    Adds random noise with <variance> variance, or 0.001 if unspecified.");
        System.err.println("  -s operation, --summarize=operation (min)");
        System.err.println("    Uses the specified summarizing operator, where operator is one of: ");
        System.err.println("      - min : summarizes using the minimum value (for costs).");
        System.err.println("      - max : summarizes using the maximum value (for utilites).");
        System.err.println("  -t [file], --trace[=file]");
        System.err.println("    Save algorithms' traces in [file], or \"trace.txt\" if unspecified.");
        System.err.println();
        System.exit(0);
    }

    public static void main(String[] argv) {
        Cli cli = new Cli();
        cli.launch(argv);
    }

    public void launch(String[] argv) {

        // Long options
        LongOpt[] longopts = new LongOpt[] {
            new LongOpt("algorithm", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
            new LongOpt("combine", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
            new LongOpt("compress", LongOpt.REQUIRED_ARGUMENT, null, 0),
            new LongOpt("heuristic", LongOpt.REQUIRED_ARGUMENT, null, 'e'),
            new LongOpt("factor-graph", LongOpt.OPTIONAL_ARGUMENT, null, 'f'),
            new LongOpt("clique-graph", LongOpt.OPTIONAL_ARGUMENT, null, 'g'),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("igdl-r", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
            new LongOpt("jt-tries", LongOpt.REQUIRED_ARGUMENT, null, 'j'),
            new LongOpt("load-tree", LongOpt.REQUIRED_ARGUMENT, null, 'l'),
            new LongOpt("max-clique-size", LongOpt.REQUIRED_ARGUMENT, null, 'l'),
            new LongOpt("normalize", LongOpt.REQUIRED_ARGUMENT, null, 'n'),
            new LongOpt("nsols", LongOpt.REQUIRED_ARGUMENT, null, 1),
            new LongOpt("partition-strategy", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
            new LongOpt("random-noise", LongOpt.OPTIONAL_ARGUMENT, null, 'r'),
            new LongOpt("summarize", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("trace", LongOpt.OPTIONAL_ARGUMENT, null, 't'),
        };
        Getopt g = new Getopt(programName, argv, "a:c:e:f::g::hi:j:l:m:n:p:r::s:t::", longopts);

        CliApp cli = new CliApp();
        int c=0;
        String arg;
        while ((c = g.getopt()) != -1) {
            switch(c) {
                case 0:
                    arg = g.getOptarg().toLowerCase();
                    if (arg.equals("arith"))
                        cli.setCompressionMethod(CliApp.CO_ARITH);
                    else if (arg.equals("bz2")) {
                        cli.setCompressionMethod(CliApp.CO_BZIP2);
                    } else if (arg.equals("none")) {
                        cli.setCompressionMethod(CliApp.CO_NONE);
                    } else {
                        System.err.println("Error: invalid compression method \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 1:
                    arg = g.getOptarg();
                    int nsols = Integer.parseInt(arg);
                    if (nsols < 1) {
                        System.err.println("Error: the number of solutions to propagate must be greater than 0.");
                        System.exit(0);
                    }
                    OptimalStrategy.nMappings = nsols;
                    break;

                case 'a':
                    arg = g.getOptarg().toLowerCase();
                    if (arg.equals("gdl"))
                        cli.setAlgorithm(CliApp.ALGO_GDL);
                    else if (arg.equals("maxsum"))
                        cli.setAlgorithm(CliApp.ALGO_MAX_SUM);
                    else if (arg.equals("figdl"))
                        cli.setAlgorithm(CliApp.ALGO_FIGDL);
                    else if (arg.equals("igdl")) {
                        cli.setAlgorithm(CliApp.ALGO_IGDL);
                    } else {
                        System.err.println("Error: invalid algorithm \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'c':
                    arg = g.getOptarg();
                    if (arg.equals("sum"))
                        cli.setCombineOperation(CostFunction.Combine.SUM);
                    else if (arg.equals("prod"))
                        cli.setCombineOperation(CostFunction.Combine.PRODUCT);
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

                case 'h':
                    showLongUsage();
                    break;

                case 'i':
                    arg = g.getOptarg();
                    int r = Integer.parseInt(arg);
                    if (r < 1) {
                        System.err.println("Error: the r value must be greater than 0.");
                        System.exit(0);
                    }
                    cli.setIGdlR(r);
                    break;

                case 'j':
                    arg = g.getOptarg();
                    int max = Integer.parseInt(arg);
                    if (max < 1) {
                        System.err.println("Error: you need to specify a maximum number of tries.");
                        System.exit(0);
                    }
                    cli.setMaxJunctionTreeTries(max);
                    break;

                case 'l':
                    arg = g.getOptarg();
                    if (arg != null) {
                        try {
                            cli.setTreeFile(new File(arg));
                        } catch (FileNotFoundException ex) {
                            System.err.println("Error loading the tree file: " + ex.getLocalizedMessage());
                            System.exit(0);
                        }
                    }
                    break;

                case 'm':
                    arg = g.getOptarg();
                    max = Integer.parseInt(arg);
                    if (max < 1) {
                        System.err.println("Error: you need to specify a maximum number variables per clique.");
                        System.exit(0);
                    }
                    cli.setMaxCliqueVariables(max);
                    break;

                case 'n':
                    arg = g.getOptarg();
                    if (arg.equals("none"))
                        cli.setNormalization(CostFunction.Normalize.NONE);
                    else if (arg.equals("sum1"))
                        cli.setNormalization(CostFunction.Normalize.SUM1);
                    else if (arg.equals("sum0"))
                        cli.setNormalization(CostFunction.Normalize.SUM0);
                    else {
                        System.err.println("Error: invalid heuristic \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'p':
                    arg = g.getOptarg();
                    if (arg.equals("scp-c"))
                        cli.setPartitionStrategy(CliApp.PS_SCP_C);
                    else if (arg.equals("scp-cc"))
                        cli.setPartitionStrategy(CliApp.PS_SCP_CC);
                    else if (arg.equals("rankup"))
                        cli.setPartitionStrategy(CliApp.PS_RANKUP);
                    else if (arg.equals("rankdown"))
                        cli.setPartitionStrategy(CliApp.PS_RANKDOWN);
                    else if (arg.equals("greedy-d"))
                        cli.setPartitionStrategy(CliApp.PS_GREEDY_D);
                    else if (arg.equals("zeros-d"))
                        cli.setPartitionStrategy(CliApp.PS_ZEROS_D);
                    else if (arg.equals("lre-cc"))
                        cli.setPartitionStrategy(CliApp.PS_LRE_CC);
                    else if (arg.equals("lre-c"))
                        cli.setPartitionStrategy(CliApp.PS_LRE_C);
                    else {
                        System.err.println("Error: invalid heuristic \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 'r':
                    arg = g.getOptarg();
                    if (arg == null) {
                        arg = "0.001";
                    }
                    double variance = Double.parseDouble(arg);
                    cli.setRandomVariance(variance);
                    break;

                case 's':
                    arg = g.getOptarg();
                    if (arg.equals("min"))
                        cli.setSummarizeOperation(CostFunction.Summarize.MIN);
                    else if (arg.equals("max"))
                        cli.setSummarizeOperation(CostFunction.Summarize.MAX);
                    else {
                        System.err.println("Error: invalid summarize operation \"" + arg + "\"");
                        System.exit(0);
                    }
                    break;

                case 't':
                    cli.setCreateTraceFile(true);
                    arg = g.getOptarg();
                    if (arg != null) {
                        cli.setTraceFile(arg);
                    }
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
            for (int i=0; i<10; i++) {
                if (c >= argv.length && System.in.available() == 0) {
                    Thread.sleep(100);
                }
            }
            if (c >= argv.length && System.in.available() == 0) {
                showUsage();
            }

            if (c < argv.length) {
                cli.setInputFile(new File(argv[c++]));
            }

            if (c < argv.length) {
                cli.setOutputFile(new File(argv[c++]));
            }

        } catch(Exception ex) {
            System.err.println(ex.getLocalizedMessage());
            System.exit(0);
        }

        // Track memory usage
        MemoryWatcher mw = new MemoryWatcher();
        Thread t = new Thread(mw);
        t.start();

        // All ready, now run!
        try {
            long t1 = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
            cli.run();
            t1 = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() - t1;
            System.out.println("TIME " + t1/(float)1000000000 + "s");

            t.interrupt();
            t.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            t.interrupt();
        }
        long bytes = mw.maxBytes;
        System.out.println("MEM " + bytes/(1024*1024) + "Mb");
    }

    public class MemoryWatcher implements Runnable {
        long maxBytes = 0;
        private boolean done = false;
        public void run() {
            while(!done) {
                long bytes = 0;
                for (MemoryPoolMXBean m : ManagementFactory.getMemoryPoolMXBeans()) {
                    MemoryUsage u = m.getPeakUsage();
                    bytes += u.getUsed();
                }
                if (bytes > maxBytes) maxBytes = bytes;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    done = true;
                }
            }
        }

    }

}
