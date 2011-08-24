package es.csic.iiia.dcop.gdlf.strategies;

import java.util.ArrayList;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.CostFunctionFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class ZeroDecompositionSliceStrategyTest {
    private final static double DELTA = 0.0001;
    
    private CostFunctionFactory factory;
    private Variable x,y,z,t,u;
    private List<Variable> edgeVariables;
    private List<CostFunction> originalFs;
    private ZeroDecompositionSliceStrategy instance;
    
    public ZeroDecompositionSliceStrategyTest() {
    }
    
    @Before
    public void setUp() {
        x = new Variable("x", 2);
        y = new Variable("y", 2);
        z = new Variable("z", 2);
        t = new Variable("t", 2);
        u = new Variable("u", 2);
        
        edgeVariables = new ArrayList(Arrays.asList(new Variable[]{x,y,z}));
        
        factory = new CostFunctionFactory();
        // Set operating mode
        CostFunction.Summarize summarize = CostFunction.Summarize.MIN;
        CostFunction.Combine combine = CostFunction.Combine.SUM;
        CostFunction.Normalize normalize = CostFunction.Normalize.NONE;
        factory.setMode(summarize, combine, normalize);
        
        CostFunction fxy,fxz,fxt,fyz,fyt,fyu,fzt,fzu;
        Random r = new Random(0L);
        fxy = factory.buildCostFunction(new Variable[]{x,y});
        fxy.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fxz = factory.buildCostFunction(new Variable[]{x,z});
        fxz.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fxt = factory.buildCostFunction(new Variable[]{x,t});
        fxt.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fyz = factory.buildCostFunction(new Variable[]{y,z});
        fyz.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fyt = factory.buildCostFunction(new Variable[]{y,t});
        fyt.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fyu = factory.buildCostFunction(new Variable[]{y,u});
        fyu.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fzt = factory.buildCostFunction(new Variable[]{z,t});
        fzt.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        fzu = factory.buildCostFunction(new Variable[]{z,u});
        fzu.setValues(new double[]{r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()});
        
        originalFs = new ArrayList(Arrays.asList(new CostFunction[]{
            fxy,fxz,fxt,fyz,fyt,fyu,fzt,fzu
        }));
        
        instance = new ZeroDecompositionSliceStrategy();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of slice method, of class ZeroDecompositionSliceStrategy.
     */
    @Test
    public void testSlice() {
        System.out.println("slice");
        List<CostFunction> fs = new ArrayList<CostFunction>();
        CostFunction combi = originalFs.remove(originalFs.size()-1).combine(originalFs);
        CostFunction summ  = combi.summarize(edgeVariables.toArray(new Variable[0]));
        fs.add(summ);
        
        testSliceR(fs, summ, 1, false);
        testSliceR(fs, summ, 2, false);
        testSliceR(fs, summ, 3, true);
    }
    
    private void testSliceR(List<CostFunction> fs, CostFunction summ, int r, boolean exact) {
        List<CostFunction> result = instance.slice(fs, r);
        CostFunction res = factory.buildCostFunction(
                summ.getVariableSet().toArray(new Variable[0])
        );
        for (CostFunction f : result) {
            res = res.combine(f);
        }
        
        for (long i=0; i<res.getSize(); i++) {
            final double v1 = summ.getValue(i);
            final double v2 = res.getValue(i);
            if (exact) {
                assertEquals("The result is exact.", v1, v2, DELTA);
            } else {
                assertTrue("The result is a lower bound.", summ.getValue(i) >= res.getValue(i)-DELTA);
            }
        }
    }
}
