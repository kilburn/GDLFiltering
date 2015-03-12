# GDL with function filtering DCOP solver

This is an implementation of the GDL with function filtering algorithm for DCOP solving. The algorithm has many variations and several tunable parameters, all of which can be experimented with using this solver.

## Building the solver

The solver is implemented in `Java 1.7`. You need a suitable JDK and (Apache Ant)[http://ant.apache.org/] to compile the project. Once compiled, the solver will run fine on any system with a `JRE 1.7` or newer installed.

To compile the project, clone it to your local machine, and from a terminal run:
```
ant jar
```

This will create a `dist/dcop.jar` file with the compiled solver ready to experiment with.

## Solving problems

You can see all the solver's options by executing it with a "-h" option:
```
% java -jar dist/dcop.jar -h
dhcp10-046:GDLFiltering kilburn$ java -jar dist/dcop.jar  -h
Usage: dcop[options] [input] [output]
Solves the DCOP problem described in [input] file (stdin if not specified)
and stores the found solution to [output] file (stdout if not specified).
Options:
  -h, --help
    Displays this help message.
  -r [variance], --random-noise[=variance]
    Adds random noise with <variance> variance, or 0.001 if unspecified.

-- Algorithm selection
  -a algorithm, --algorithm=algorithm (gdl)
    Uses the specified algorithm, where algorithm is one of: 
      - gdl : GDL over junction tree optimal solver.
      - figdl : Filtered IGDL over junction tree solver.
      - max-sum : max-sum approximation.
      - dsa : dsa approximation (p=0.9).

-- Commutative semiring specification
  -c operation, --combine=operation (sum)
    Uses the specified combining operator, where operator is one of: 
      - sum : combine using addition.
      - prod : combine using product.
  -s operation, --summarize=operation (max)
    Uses the specified summarizing operator, where operator is one of: 
      - min : summarizes using the minimum value (for costs).
      - max : summarizes using the maximum value (for utilites).
  -n mode, --normalize=mode (none)
    Uses the specified normalization mode, where it is one of: 
      - none : do not perform any normalization (default for junction tree algorithm).
      - sum0 : values are normalized to sum zero (default for max-sum).
      - sum1 : values are normalized to sum one.

-- Performance tracking options
  --compress=method (none)
    Uses the specified compression method for sent messages, method iis one of: 
      - arith  : compress using an arithmetic compressor with a 8-byte PPM model.
      - bz2    : compress using a bzip2 compressor.
      - sparse : compress by sending sparse functions when possible (<idx,value> tuples).
      - none   : do not perform any compression of sent messages.

-- Logging and output formatting
  --export-tree[=treeFile]
    Exports the clique graph to <graphFile>, so that it can be reused later
    with the --load-tree argument.
  -f [graphFile], --factor-graph[=graphFile]
    Outputs the factor graph representation in .dot format to [graphFile],
    or "fgraph.dot" if unspecified.
  -g [graphFile], --clique-graph[=graphFile]
    Outputs the clique graph representation in .dot format to [graphFile],
    or "cgraph.dot" if unspecified.
  -o format, --output-format=format (custom)
    Uses the specified output format, where format is one of: 
      - uai        : uses the UAI competition output format.
      - custom     : uses the custom output format.
  -t [file], --trace[=file]
    Save algorithms' traces in [file], or "trace.txt" if unspecified.
  --evidence-file=<file>
    Load the evidence file <file> (uai stuff).

-- Junction tree building options
  -e heuristic, --heuristic=heuristic (random)
    Uses the specified heuristic function to build the Junction Tree,
    where heuristic is one of: 
      - mcn    : chooses the most connected node next, randomly breaking ties.
      - mcs    : chooses the most related node next, then mcn to break ties.
      - random : randomly picks any of the candidates as the next node for the tree.
  -m variables, --max-clique-size=variables (14)
    Don not try to solve problems with cliques of more than <variables> variables.
  -j tries, --jt-tries tries (30)
    Number of junction trees to build trying to minimize the maximum clique size.
  -l file, --load-tree=file
    Use the tree definition found in <file>.

-- GDL with filtering settings
  -i value, --igdl-r value (2)
    Sets the 'r' to value in igdl.
  --nsols=<number> (1)
    Sets figdl to test <number> number of possible solutions at each iteration
  --old-style-filtering
    Uses the old-style filtering method (filter using outgoing functions only).
  -p strategy, --approximation-strategy=strategy (dcr-bottom-up)
    Uses the specified approximation strategy, where strategy is one of: 
      - aamas-top-down  : zero-tracking decomposition unlimited memory
      - aamas-bottom-up : scope-based partitioning unlimited memory
      - dcr-bottom-up   : scope-based partitioning limited memory
      - mixed-noslice   : mixed (r+delta, r, r) scope-based partitioning.
      - mixed-slice     : mixed (r+delta, r+delta, r) scp + zero-decomposition.
      - mixed-uslice    : mixed (inf, r+delta, r) scp + zero-decomposition.
  --delta=<value> (0)
    Sets the delta parameter for the mixed approximation strategies.
  --probability=<value> (0.9)
    Sets the probability parameter of both DSA algorithm and stochastical solution expansion.
  --solution-expansion=strategy (root)
    Uses the specified solution expansion strategy, where strategy is one of: 
      - root        : root expands all solutions (up to its own maximum)
      - greedy      : each node expands as much solutions as possible
      - stochastic  : each solution is expanded with a probability (p=0.9)
      - information : expansion based on the information loss observed during utility propagation.
  --solution-solving=strategy (optimal)
    Uses the specified solution solving strategy, where strategy is one of: 
      - optimal     : solutions are computed optimaly
      - stochastic  : solutions are computed using dsa
```

As you can see, the solver provides many options. For instance, to run the solver on a maximization DCOP problem using zero decomposition you should execute the following:
```
java -jar dist/dcop.jar -s max -c sum -a gdlf -p aamas-top-down --compress arith <problem_file>
```

You can change `-s max` by `-s min` if you want to minimize instead of maximize the DCOP (it depends on wether you define the problem as utilities or costs).

The problem must be in either the format of the [Teamcore DCOP repository datasets](http://teamcore.usc.edu/dcop/), or the [UAI file format (MARKOV problems)](http://graphmod.ics.uci.edu/uai08/FileFormat).

Notice that (complete) GDL algorithms need to build a Junction Tree (JT) of the problem to solve it. The solver will (by default) build 30 random JTs and use the one with the lowest treewidth. However, you can select different heuristics to build these trees, as well as export the best tree for a problem and then tell subsequent executions to use that same one. See the "Junction tree building options" section in the `-h` help.
