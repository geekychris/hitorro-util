/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.basefile.tools.queue.writer.serializer;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writable for simple byte array buffers.  We dont care what is in them.  The caller assumes responsibility for them
 * being self contained and not dependent on some kind of bookends within the file format.
 */
public class SimpleByteBufferWriterWritable implements WriterWritableInterface<ByteArrayWrapper> {
    protected String extension = "ser";
    DataOutputStream dos;
    OutputStream oos;

    public SimpleByteBufferWriterWritable(String extension) {
        this.extension = extension;
    }

    public SimpleByteBufferWriterWritable() {

    }

    public void applyCloseToFile(BaseFile f) throws IOException {
        OutputStream os = new BufferedOutputStream(f.getDataOutputStreamAppend());
        os.write(HTObjectOutputStream.EndOfStream);
        os.flush();
        os.close();
    }

    public boolean open(BaseFile f) throws IOException {
        oos = new BufferedOutputStream(f.getDataOutputStream());
        dos = new DataOutputStream(oos);
        return true;
    }

    public boolean close() throws IOException {
        if (dos != null) {
            dos.flush();
            oos.close();
            dos = null;
            oos = null;
        }
        return true;
    }

    public boolean write(ByteArrayWrapper wrapper) throws IOException {
        dos.write(wrapper.buff, 0, wrapper.size);
        dos.flush();
        return true;
    }

    public int getBytes(ByteArrayWrapper wrapper) {
        return wrapper.size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String ext) {
        extension = ext;
    }
}
