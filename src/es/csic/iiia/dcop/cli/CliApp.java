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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import es.csic.iiia.dcop.FactorGraph;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.VariableAssignment;
import es.csic.iiia.dcop.algo.JunctionTreeAlgo;
import es.csic.iiia.dcop.algo.MaxSum;
import es.csic.iiia.dcop.algo.RandomNoiseAdder;
import es.csic.iiia.dcop.bb.UBGraph;
import es.csic.iiia.dcop.bb.UBResults;
import es.csic.iiia.dcop.up.UPResults;
import es.csic.iiia.dcop.dfs.DFS;
import es.csic.iiia.dcop.dfs.MCN;
import es.csic.iiia.dcop.dfs.MCS;
import es.csic.iiia.dcop.figdl.FIGdlFactory;
import es.csic.iiia.dcop.gdl.GdlFactory;
import es.csic.iiia.dcop.igdl.IGdlFactory;
import es.csic.iiia.dcop.igdl.strategy.IGdlPartitionStrategy;
import es.csic.iiia.dcop.io.CliqueTreeSerializer;
import es.csic.iiia.dcop.io.DatasetReader;
import es.csic.iiia.dcop.io.TreeReader;
import es.csic.iiia.dcop.jt.JTResults;
import es.csic.iiia.dcop.jt.JunctionTree;
import es.csic.iiia.dcop.mp.AbstractNode.Modes;
import es.csic.iiia.dcop.up.UPFactory;
import es.csic.iiia.dcop.up.UPGraph;
import es.csic.iiia.dcop.util.Compressor;
import es.csic.iiia.dcop.util.UnaryVariableFilterer;
import es.csic.iiia.dcop.vp.VPGraph;
import es.csic.iiia.dcop.vp.VPResults;
import es.csic.iiia.dcop.vp.strategy.OptimalStrategy;
import es.csic.iiia.dcop.vp.strategy.VPStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Line Interface application logic handler.
 *
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CliApp {

    private static Logger log = LoggerFactory.getLogger(CliApp.class);

    /**
     * Use GDL as solving algorithm.
     */
    public static final int ALGO_GDL = 0;
    /**
     * Use max-sum as solving algorithm.
     */
    public static final int ALGO_MAX_SUM = 1;
    /**
     * Use IGDL as solving algorithm.
     */
    public static final int ALGO_IGDL = 2;
    /**
     * Use Filtered-IGDL as solving algorithm.
     */
    public static final int ALGO_FIGDL = 3;

    /**
     * MCS junction tree building heuristic.
     */
    public static final int JT_HEURISTIC_MCS = 0;
    /**
     * MCN junction tree building heuristic.
     */
    public static final int JT_HEURISTIC_MCN = 1;

    public enum PS {
        SCP_C (es.csic.iiia.dcop.igdl.strategy.scp.SCPcStrategy.class),
        SCP_CC (es.csic.iiia.dcop.igdl.strategy.scp.SCPccStrategy.class),
        RANKUP (es.csic.iiia.dcop.igdl.strategy.RankUpStrategy.class),
        RANKDOWN (es.csic.iiia.dcop.igdl.strategy.RankDownStrategy.class),
        LRE_D (es.csic.iiia.dcop.igdl.strategy.gd.LREGreedyStrategy.class),
        LMRE_D (es.csic.iiia.dcop.igdl.strategy.gd.LMREGreedyStrategy.class),
        ZEROS_D (es.csic.iiia.dcop.igdl.strategy.ZerosDecompositionStrategy.class),
        LRE_C (es.csic.iiia.dcop.igdl.strategy.cbp.LREcStrategy.class),
        LRE_CC (es.csic.iiia.dcop.igdl.strategy.cbp.LREccStrategy.class),
        LMRE_C (es.csic.iiia.dcop.igdl.strategy.cbp.LMREcStrategy.class),
        LMRE_CC (es.csic.iiia.dcop.igdl.strategy.cbp.LMREccStrategy.class),
        SUPERSET (es.csic.iiia.dcop.igdl.strategy.scp.SCPSuperSetStrategy.class),
        ;

        private IGdlPartitionStrategy instance;
        PS(Class<? extends IGdlPartitionStrategy> c) {
            try {
                instance = c.newInstance();
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(CliApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(CliApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        IGdlPartitionStrategy getInstance() {
            return instance;
        }
    }

    /**
     * Solution propagation strategies
     */
    public static final int SS_OPTIMAL  = 0;
    public static final int SS_PARTIAL  = 1;

    /**
     * Compession methods
     */
    public static final int CO_ARITH    = 0;
    public static final int CO_BZIP2    = 1;
    public static final int CO_NONE     = 2;

    private int algorithm = ALGO_GDL;
    private int heuristic = JT_HEURISTIC_MCS;
    private CostFunction.Summarize summarizeOperation = CostFunction.Summarize.MIN;
    private CostFunction.Combine combineOperation = CostFunction.Combine.SUM;
    private CostFunction.Normalize normalization = CostFunction.Normalize.NONE;
    private int maxCliqueVariables = 14;
    private int maxJunctionTreeTries = 1;
    private double randomVariance = 0;

    /**
     * Tree/Graph import/export options
     */
    private boolean createCliqueTree = false;
    private boolean createCliqueGraph = false;
    private boolean createFactorGraph = false;
    private InputStream treeFile = null;
    private String cliqueGraphFile = "cgraph.dot";
    private String factorGraphFile = "fgraph.dot";
    private String cliqueTreeFile  = "problem.tree";


    private boolean createTraceFile = false;
    private String traceFile = "trace.txt";
    private int IGdlR = 2;
    private PS partitionStrategy = PS.RANKUP;
    private int solutionStrategy = SS_OPTIMAL;

    public String getCliqueTreeFile() {
        return cliqueTreeFile;
    }

    public void setCliqueTreeFile(String cliqueTreeFile) {
        this.cliqueTreeFile = cliqueTreeFile;
    }

    public boolean isCreateCliqueTree() {
        return createCliqueTree;
    }

    public void setCreateCliqueTree(boolean createCliqueTree) {
        this.createCliqueTree = createCliqueTree;
    }

    /**
     * Get the maximum number of junction tree's built trying to minimize the
     * maximal clique size.
     *
     * @return maximum number of junction tree building tries.
     */
    public int getMaxJunctionTreeTries() {
        return maxJunctionTreeTries;
    }

    /**
     * Set the maximum number of junction tree's built trying to minimize the
     * maximal clique size.
     *
     * @param maxJunctionTreeTries number of junction tree building tries.
     */
    public void setMaxJunctionTreeTries(int maxJunctionTreeTries) {
        this.maxJunctionTreeTries = maxJunctionTreeTries;
    }

    /**
     * Get the variance of the random gaussian noise adder.
     *
     * @return random noise variance.
     */
    public double getRandomVariance() {
        return randomVariance;
    }

    /**
     * Set the variance of the random gaussian noise adder.
     *
     * @param randomVariance random noise variance.
     */
    public void setRandomVariance(double randomVariance) {
        this.randomVariance = randomVariance;
    }

    /**
     * Get the hard maximum of variables in one clique.
     * 
     * If no junction tree with less than this maximum is found in the
     *
     * @return
     */
    public int getMaxCliqueVariables() {
        return maxCliqueVariables;
    }

    /**
     *
     * @param maxCliqueVariables
     */
    public void setMaxCliqueVariables(int maxCliqueVariables) {
        this.maxCliqueVariables = maxCliqueVariables;
    }

    /**
     *
     * @return
     */
    public int getCommunicationCost() {
        return communicationCost;
    }

    /**
     *
     * @param communicationCost
     */
    public void setCommunicationCost(int communicationCost) {
        this.communicationCost = communicationCost;
    }

    /**
     *
     * @return
     */
    public CostFunction.Normalize getNormalization() {
        return normalization;
    }

    /**
     *
     * @param normalization
     */
    public void setNormalization(CostFunction.Normalize normalization) {
        this.normalization = normalization;
    }
    private int communicationCost = 0;

    /**
     *
     * @return
     */
    public CostFunction.Combine getCombineOperation() {
        return combineOperation;
    }

    /**
     *
     * @param combineOperation
     */
    public void setCombineOperation(CostFunction.Combine combineOperation) {
        this.combineOperation = combineOperation;
    }

    /**
     *
     * @return
     */
    public CostFunction.Summarize getSummarizeOperation() {
        return summarizeOperation;
    }

    /**
     *
     * @param summarizeOperation
     */
    public void setSummarizeOperation(CostFunction.Summarize summarizeOperation) {
        this.summarizeOperation = summarizeOperation;
    }
    
    private InputStream input = System.in;


    private void outputVariableStatistics(CostFunction[] factors) {

        // Collect all variables
        HashSet<Variable> varSet = new HashSet<Variable>();
        for (CostFunction f : factors) {
            varSet.addAll(f.getVariableSet());
        }

        // Compute min/max/avg
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        double avg = 0;
        for (Variable v : varSet) {
            int domain = v.getDomain();
            if (domain < min) min = domain;
            if (domain > max) max = domain;
            avg += domain;
        }
        avg /= (double)varSet.size();

        // Output stats
        System.out.println("VARIABLES " + varSet.size());
        System.out.println("MIN_DOMAIN " + min);
        System.out.println("AVG_DOMAIN " + avg);
        System.out.println("MAX_DOMAIN " + max);
    }

    void run() {
        // Setup log handling
        setupLogHandling();

        // Parameter combination checks
        if (algorithm == ALGO_MAX_SUM && normalization == CostFunction.Normalize.NONE) {
            System.err.println("Warning: maxsum doesn't converge without normalization, using sum0.");
            normalization = CostFunction.Normalize.SUM0;
        }

        // Read the input file into factors
        DatasetReader r = new DatasetReader();
        CostFunctionFactory factory = new CostFunctionFactory();
        factory.setCombineOperation(combineOperation);
        factory.setNormalizationType(normalization);
        factory.setSummarizeOperation(summarizeOperation);
        CostFunction[] factors = r.read(input, factory);

        // Filter out unary variables
        VariableAssignment unaries = UnaryVariableFilterer.filterVariables(factors);
        if (unaries.size() > 0) {
            System.out.println(unaries.size() + " unary variables found.");
        }

        // Output factor graph
        createFactorGraphFile(new FactorGraph(factors));

        // Output total number of variables and min/avg/max domain
        outputVariableStatistics(factors);

        // Create the clique graph, using the specified algorithm
        UPGraph cg = createCliqueGraph(factors);

        // Add noise if requested
        if (randomVariance != 0) {
            RandomNoiseAdder rna = new RandomNoiseAdder(randomVariance);
            rna.addNoise(cg);
        }

        // Run the solving algorithm
        cg.setFactory(factory);
        UPResults results = cg.run(1000);
            
        System.out.println("ITERATIONS " + results.getIterations());
        System.out.println("CBR " + results.getCBR(communicationCost));
        System.out.println("BYTES " + results.getSentBytes());
        System.out.println("LOAD_FACTOR " + results.getLoadFactor());

        // Extract a solution
        VPStrategy sStrategy = null;
        switch (solutionStrategy) {
            case SS_OPTIMAL:
                sStrategy = new OptimalStrategy();
                break;

        }
        VPGraph st = new VPGraph(cg, sStrategy);
        VPResults res = st.run(10000);
        ArrayList<VariableAssignment> maps = res.getMappings();
        VariableAssignment map = maps.get(0);
        map.putAll(unaries);

        // Compute UB for IGdl
        if (algorithm == ALGO_IGDL || algorithm == ALGO_FIGDL) {
            UBGraph ub = new UBGraph(st);
            UBResults ubres = ub.run(1000);
            System.out.println("BOUND " + ubres.getBound());
        }

        SortedMap<Variable, Integer> foo = new TreeMap<Variable, Integer>(map);
        System.out.print("SOLUTION");
        for(Variable v : foo.keySet()) {
            System.out.print(" " + (map.get(v)));
        }
        System.out.println();

        // Evaluate solution
        double cost = 0;
        for (CostFunction f : factors) {
            cost += f.getValue(map);
        }
        System.out.println("COST " + cost);
    }

    void setCreateCliqueGraph(boolean create) {
        createCliqueGraph = create;
    }

    void setCreateFactorGraph(boolean create) {
        createFactorGraph = create;
    }

    void setHeuristic(int heuristic) {
        this.heuristic = heuristic;
    }

    private void createCliqueGraphFile(UPGraph cg) {
        if (!createCliqueGraph) {
            return;
        }

        FileWriter fw;
        try {
            fw = new FileWriter(new File(cliqueGraphFile), false);
            fw.write(cg.toString());
            fw.close();
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    private void createFactorGraphFile(FactorGraph fg) {
        if (!createFactorGraph) {
            return;
        }

        FileWriter fw;
        try {
            fw = new FileWriter(new File(factorGraphFile), false);
            fw.write(fg.toString());
            fw.close();
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    private void createCliqueTreeFile(UPGraph cg) {
        if (!createCliqueTree) {
            return;
        }

        FileWriter fw;
        try {
            fw = new FileWriter(new File(cliqueTreeFile), false);
            CliqueTreeSerializer serializer = new CliqueTreeSerializer();
            fw.write(serializer.serializeTreeStructure(cg));
            fw.close();
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    private UPGraph createCliqueGraph(CostFunction[] factors) {
        
        UPGraph cg = null;
        switch(algorithm) {

            case ALGO_GDL:
            case ALGO_IGDL:
            case ALGO_FIGDL:
                UPFactory factory = null;
                if (algorithm == ALGO_GDL) {
                    factory = new GdlFactory();
                    ((GdlFactory)factory).setMode(Modes.TREE_UP);
                } else {
                    IGdlPartitionStrategy pStrategy = partitionStrategy.getInstance();
                    factory = algorithm == ALGO_IGDL
                        ? new IGdlFactory(this.getIGdlR(), pStrategy)
                        : new FIGdlFactory(this.getIGdlR(), pStrategy);
                }
                int variables = 0;
                JTResults results = null;

                if (treeFile != null) {
                    TreeReader treeReader = new TreeReader();
                    treeReader.read(treeFile, factors);
                    cg = JunctionTreeAlgo.buildGraph(factory, treeReader.getFactorDistribution(), treeReader.getAdjacency());
                    cg.setRoot(treeReader.getRoot());
                    JunctionTree jt = new JunctionTree(cg);
                    results = jt.run(1000);
                    variables = results.getMaxVariables();
                } else {
                    int minVariables = Integer.MAX_VALUE;
                    for(int i=0; i < maxJunctionTreeTries; i++) {
                        DFS dfs = null;
                        switch(heuristic) {
                            case JT_HEURISTIC_MCS:
                                dfs = new MCS(factors);
                                break;
                            default:
                                System.err.println("Warning: invalid junction tree heuristic chosen, falling back to MCN.");
                            case JT_HEURISTIC_MCN:
                                dfs = new MCN(factors);
                                break;
                        }

                        UPGraph candidateCg = null;
                        candidateCg = JunctionTreeAlgo.buildGraph(factory, dfs.getFactorDistribution(), dfs.getAdjacency());
                        candidateCg.setRoot(dfs.getRoot());
                        JunctionTree jt = new JunctionTree(candidateCg);
                        JTResults candidateResults = jt.run(10000);
                        variables = candidateResults.getMaxVariables();

                        if (variables < minVariables) {
                            minVariables = variables;
                            cg = candidateCg;
                            results = candidateResults;
                        }
                    }
                }
                
                System.out.println("MAX_CLIQUE_VARIABLES " + results.getMaxVariables());
                System.out.println("MAX_CLIQUE_SIZE " + results.getMaxSize());
                System.out.println("MAX_EDGE_VARIABLES " + results.getMaxEdgeVariables());
                createCliqueGraphFile(cg);
                createCliqueTreeFile(cg);
                if (results.getMaxVariables() >= maxCliqueVariables) {
                    System.err.println("Error: minimum clique variables found is greater than the specified max.");
                    System.exit(1);
                }
                break;

            case ALGO_MAX_SUM:
                cg = MaxSum.buildGraph(factors);
                createCliqueGraphFile(cg);
                break;

        }
        
        return cg;
    }

    void setAlgorithm(int algo) {
        algorithm = algo;
    }

    void setInputFile(File file) throws FileNotFoundException {
        input = new FileInputStream(file);
    }

    void setOutputFile(File file) throws FileNotFoundException {
        System.setOut(new PrintStream(file));
    }

    void setFactorGraphFile(String fileName) {
        cliqueGraphFile = fileName;
    }

    void setCliqueGraphFile(String fileName) {
        cliqueGraphFile = fileName;
    }

    void setTreeFile(File fileName) throws FileNotFoundException {
        treeFile = new FileInputStream(fileName);
    }

    /**
     * @return the traceFile
     */
    public String getTraceFile() {
        return traceFile;
    }

    /**
     * @param traceFile the traceFile to set
     */
    public void setTraceFile(String traceFile) {
        
        this.traceFile = traceFile;
    }

    /**
     * @return the createTraceFile
     */
    public boolean isCreateTraceFile() {
        return createTraceFile;
    }

    /**
     * @param createTraceFile the createTraceFile to set
     */
    public void setCreateTraceFile(boolean createTraceFile) {
        this.createTraceFile = createTraceFile;
    }

    /**
     * @return the IGdlR
     */
    public int getIGdlR() {
        return IGdlR;
    }

    /**
     * @param IGdlR the IGdlR to set
     */
    public void setIGdlR(int IGdlR) {
        this.IGdlR = IGdlR;
    }

    private void setupLogHandling() {
        if (!isCreateTraceFile()) {
            return;
        }

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            if (this.createTraceFile) {
                lc.putProperty("file.name", traceFile);
                URL cURL = getClass().getClassLoader().getResource("logback-trace.xml");
                configurator.doConfigure(cURL);
            }
        } catch (JoranException je) {
            je.printStackTrace();
        }
    }

    void setPartitionStrategy(PS partitionStrategy) {
        this.partitionStrategy = partitionStrategy;
    }

    void setCompressionMethod(int method) {
        Compressor.METHOD = method;
    }

}