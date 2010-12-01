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

package es.csic.iiia.dcop;

import es.csic.iiia.dcop.util.CostFunctionStats;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Cost Function implementation that stores the whole hypercube of values in
 * a linearized array of doubles. Therefore, this is the fastest implementation
 * for algorithms that always work over the complete hypercubes. In contrast,
 * algorithms that use filtering techniques will prefer to use other (sparse)
 * representations of the hypercubes.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public final class SparseCostFunction extends AbstractCostFunction implements Serializable {

    /**
     * Function's values storage array.
     */
    private double[] values;

    /**
     * Function's key storage array.
     */
    private int[] keys;

    /**
     * Function's used positions.
     */
    private int used;

    /**
     * "Zero" value (value of the missing elements)
     */
    private double zero;

    /**
     * Compactness flag. It's true if there are "zero" values
     * stored as actual values.
     */
    private boolean compact;

    /**
     * Maximum stored key
     */
    private int maxKey;

    /**
     * Creates a new CostFunction, initialized to the zero value.
     *
     * @param variables involved in this factor.
     */
    protected SparseCostFunction(Variable[] variables, double zeroValue) {
        super(variables);
        
        used = 0;
        maxKey = -1;
        values = new double[2];
        keys = new int[2];
        zero = zeroValue;
        compact = true;
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    protected SparseCostFunction(CostFunction factor) {
        super(factor);
        
        if (factor instanceof SparseCostFunction) {
            final SparseCostFunction f = (SparseCostFunction)factor;
            values = f.values.clone();
            keys = f.keys.clone();
            used = f.used;
            zero = f.zero;
            compact = f.compact;
            maxKey = f.maxKey;
        } else {
            values = new double[2];
            keys = new int[2];
            zero = getFactory().getSummarizeOperation().getNoGood();
            setValues(factor.getValues());
            compact = true;
            maxKey = -1;
        }
    }

    /**
     * Resets the costFunction to zero values
     */
    private void reset() {
        values = new double[2];
        keys = new int[2];
        used = 0;
        compact = true;
    }

    /** {@inheritDoc} */
    public double[] getValues() {
        double[] v = new double[size];
        for (int i=0; i<size; i++) {
            v[i] = getValue(i);
        }
        return v;
    }

    /** {@inheritDoc} */
    public void setValues(double[] values) {
        reset();

        if (values.length != size) {
            throw new IllegalArgumentException("Invalid index specification");
        }

        for (int i=0; i<size; i++) {
            setValue(i, values[i]);
        }
    }

    /** {@inheritDoc} */
    public Iterator<Integer> iterator() {
        return new SparseCostFunctionIterator();
    }

    /** {@inheritDoc} */
    public double getValue(int index) {
        if (index < 0 || index >= size) 
            throw new IndexOutOfBoundsException(Integer.toString(index));

        int spot = Arrays.binarySearch(keys, 0, used, index);
        return spot < 0 ? zero : values[spot];
    }

    /** {@inheritDoc} */
    public void setValue(int index, double value) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(Integer.toString(index));

        if (index > maxKey) {
            maxKey = index;
            update(used, index, value);
            return;
        }

        int spot = Arrays.binarySearch(keys, 0, used, index);
        if (spot >= 0) {
            // If value == zero, then we lost compactness
            if (value == zero) compact = false;
            values[spot] = value;
        } else {
            // Put the value only if it's not the "zero" value.
            if (value != zero) {
                update(-1-spot, index, value);
            }
        }
    }

    private void update(int spot, int index, double value) {
        // grow if reaching end of capacity
        if (used == keys.length) {
            //int capacity = (keys.length * 3) / 2 + 1;
            int capacity = this.getSize();
            keys = Arrays.copyOf(keys, capacity);
            values = Arrays.copyOf(values, capacity);
        }
        // shift values if not appending
        if (spot < used) {
            System.arraycopy(keys, spot, keys, spot+1, used-spot);
            System.arraycopy(values, spot, values, spot+1, used-spot);
        }
        used++;
        keys[spot] = index;
        values[spot] = value;
    }

    /** {@inheritDoc} */
    public int getNumberOfNoGoods() {
        return size - used;
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return "S" + super.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append(" {");
        for(int i=0; i<used; i++) {
            buf.append(keys[i]);
            buf.append(":");
            buf.append(CostFunctionStats.formatValue(values[i]));
            buf.append(", ");
        }
        if (used>0)
            buf.delete(buf.length()-2, buf.length());
        buf.append("}");

        return buf.toString();
    }

    private void compact() {
        if (compact == true)
            return;

        // Advance the "copy from" pointer to the first zero value
        int from = 0;
        while(from < used && values[from] != zero) {
            from++;
        }

        // Set the "copy to" pointer to the first zero value
        int to = from;

        // While there are values left...
        for(;from<used; from++) {

            // Skip to the next non-zero value
            while(from < used && values[from] == zero) {
                from++;
            }
            if (from >= used) break;

            // Copyback
            values[to] = values[from];
            keys[to] = keys[from];
            to++;
        }

        used = to;
    }

    public int getNumberOfZeros() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Implements the Iterator interface for an hypercube, allowing to iterate
     * over its elements using the common java conventions.
     */
    protected class SparseCostFunctionIterator implements Iterator<Integer> {
        private int idx = 0;

        @Override
        public boolean hasNext() {
            return idx < used;
        }

        @Override
        public Integer next() {
            return keys[idx++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("You can not remove elements from a sparse function.");
        }

    }

}
