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

import com.colloquial.arithcode.ArithCodeOutputStream;
import com.colloquial.arithcode.PPMModel;
import es.csic.iiia.dcop.cli.CliApp;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.bzip2.CBZip2OutputStream;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class CompressOutputStream extends OutputStream {

    private OutputStream compressor;
    private CountOutputStream byteCounter;

    public CompressOutputStream() {
        byteCounter = new CountOutputStream();
        switch(Compressor.METHOD) {
            case CliApp.CO_ARITH:
                compressor = new ArithCodeOutputStream(byteCounter, new PPMModel(8));
                break;
            case CliApp.CO_BZIP2:
                try {
                    compressor = new CBZip2OutputStream(compressor);
                } catch (IOException ex) {
                    Logger.getLogger(CompressOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }

    }

    public int getBytes() throws IOException {
        compressor.close();
        return byteCounter.getBytes();
    }

    @Override
    public void close() throws IOException {
        compressor.close();
    }

    @Override
    public void flush() throws IOException {
        compressor.flush();
    }

    @Override
    public void write(int b) throws IOException {
        compressor.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        compressor.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        compressor.write(b, off, len);
    }

    private class CountOutputStream extends OutputStream {
        private int bytes = 0;

        @Override
        public void write(int b) throws IOException {
            bytes++;
        }

        public int getBytes() {
            return bytes;
        }

    }
}
