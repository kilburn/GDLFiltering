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

package es.csic.iiia.dcop.util;

import es.csic.iiia.dcop.CostFunction;
import es.csic.iiia.dcop.MapCostFunction;
import es.csic.iiia.dcop.Variable;
import es.csic.iiia.dcop.cli.CliApp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Compressor {

    public static int METHOD = CliApp.CO_ARITH;

    public static long getCompressedSizeF(CostFunction f) {
        switch(METHOD) {
            case CliApp.CO_BZIP2:
            case CliApp.CO_ARITH:
                return arithmeticCompress(f);
            default:
                return f.getSize() * 8;
        }
    }

    public static long getCompressedSizeFs(Collection<CostFunction> fs) {
        long sum = 0;
        for (CostFunction f : fs) {
            switch(METHOD) {
                case CliApp.CO_BZIP2:
                case CliApp.CO_ARITH:
                    sum += arithmeticCompressWithHeader(f);
                    break;
                default:
                    sum += f.getVariableSet().size()*4 + f.getSize()*8;
                    break;
            }
        }

        return sum;
    }

    private static long manualCompressWithHeader(CostFunction f) {

        long bytes = 0;
        bytes += f.getVariableSet().size() * 4;

        // State machine to count the number of elements that must be
        // captured.

        long els = 0; boolean hasZeros = false;
        final Iterator<Integer> it = f.iterator();
        int i = 0, n = 0, state = 0;
        while(it.hasNext()) {
            n = it.next();
            double v = f.getValue(n);
            if (i != n) {els++; state=2;}

            switch(state) {
                case 0:
                    els++;

                    if (v == 0)
                        state = 1;
                    else if (Double.isInfinite(v))
                        state = 2;

                    break;

                case 1:
                    hasZeros = true;
                    if (v == 0)
                        break;

                    els++;

                    if (Double.isInfinite(v))
                        state = 2;
                    else
                        state = 0;

                    break;

                case 2:
                    if (Double.isInfinite(v))
                        break;

                    els++;

                    if (v == 0)
                        state = 1;
                    else
                        state = 0;
                    break;
            }

            i = n+1;
        }
        if (i != n) {els++; state=2;}

        final long overheadBits = els * (hasZeros ? 2 : 1);
        bytes += els*8 + overheadBits/8;

        return bytes;
    }

//    public static long getCompressedSizeFs(Collection<CostFunction> fs) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//        for (CostFunction f : fs) {
//            try {
//                bos.write(toByteArrayWithHeader(f));
//            } catch (IOException ex) {
//                Logger.getLogger(Compressor.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        return compress(bos.toByteArray()).length;
//    }

//    public static long getCompressedSizeFs(Collection<CostFunction> fs) {
//        long sum = 0;
//        for (CostFunction f : fs) {
//            sum += f.getVariableSet().size();
//            sum += ((AbstractCostFunction)f).getRealSize() * 8;
//        }
//
//        return sum;
//    }

    private static int arithmeticCompressWithHeader(CostFunction f) {
        CompressOutputStream c = new CompressOutputStream();
        int bytes = 0;
        try {
            // Vars first
            for (Variable v : f.getVariableSet()) {
                c.write(toByteArray(v.getId()));
            }

            // Values next
            if (f instanceof MapCostFunction) {
                // Sparse functions are handled by key/value!
                Iterator<Integer> iter = f.iterator();
                while (iter.hasNext()) {
                    c.write(toByteArray(iter.next()));
                }
                iter = f.iterator();
                while (iter.hasNext()) {
                    c.write(toByteArray(f.getValue(iter.next())));
                }
            } else {
                // Dense functions simply send all values
                for (int i = 0; i < f.getSize(); i++) {
                    c.write(toByteArray(f.getValue(i)));
                }
            }

            // Get byte count
            c.close();
            bytes = c.getBytes();

        } catch (IOException ex) {
            Logger.getLogger(Compressor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bytes;
    }

    private static int arithmeticCompress(CostFunction f) {
        CompressOutputStream c = new CompressOutputStream();
        int bytes = 0;
        try {
            // Write values
            for (int i = 0; i < f.getSize(); i++) {
                c.write(toByteArray(f.getValue(i)));
            }

            // Get byte count
            c.close();
            bytes = c.getBytes();
        } catch (IOException ex) {
            Logger.getLogger(Compressor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bytes;
    }

    private static byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    private static byte[] toByteArray(double data) {
        long d = Double.doubleToRawLongBits(data);

        return new byte[] {
            (byte)((d >> 56) & 0xff),
            (byte)((d >> 48) & 0xff),
            (byte)((d >> 40) & 0xff),
            (byte)((d >> 32) & 0xff),
            (byte)((d >> 24) & 0xff),
            (byte)((d >> 16) & 0xff),
            (byte)((d >> 8) & 0xff),
            (byte)((d >> 0) & 0xff),
        };
    }

}
