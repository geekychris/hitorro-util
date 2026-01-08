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
package com.hitorro.util.io.largedata;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.FSInputStream;
import com.hitorro.util.io.largedata.compressedstreams.InputInputStream;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p/>
 * encoded log file one row at a time till you hit the null terminator.
 */
public class CompressedStreamIOIterator<T extends CompressedStreamIO> extends AbstractIterator<T> {
    private CInputStream blockInputStream;
    private BaseFileAccessingObjectFactory<T> factory;
    private T line;
    private String source;
    private boolean open;
    private int count = 0;

    private CompressedStreamIOIterator() {

    }

    public CompressedStreamIOIterator(File f, BaseFileAccessingObjectFactory<T> fac) throws IOException {
        factory = fac;
        setFile(f);
    }

    public CompressedStreamIOIterator(BaseFile f, BaseFileAccessingObjectFactory<T> fac) throws IOException {
        factory = fac;
        setStream(f.getCInputStream());
    }

    public CompressedStreamIOIterator(InputStream is, BaseFileAccessingObjectFactory<T> fac) throws IOException {
        factory = fac;
        setStream(is);
    }

    public String toString() {
        return Fmt.S("CSIOT :%s count:%s, open:%s", source, count, open);
    }

    private void setFile(File f) throws IOException {
        blockInputStream = new FSInputStream(f);
        source = f.getAbsolutePath();
        open = true;
        line = aux();
    }

    private void setStream(CInputStream is) throws IOException {
        //XXX This is not correct we must know where the end of stream is?
        // we start out with -1 and it will eventually get set to the correct length when we hit eof.
        blockInputStream = is;
        source = is.toString();
        open = true;
        line = aux();
    }

    private void setStream(InputStream is) throws IOException {
        //XXX This is not correct we must know where the end of stream is?
        // we start out with -1 and it will eventually get set to the correct length when we hit eof.
        blockInputStream = new InputInputStream(new DataInputStream(is), -1);
        source = is.toString();
        open = true;
        line = aux();
    }

    public void close() throws Exception {
        if (blockInputStream != null) {
            blockInputStream.close();
        }
        blockInputStream = null;
    }

    public boolean hasNext() {
        return line != null;
    }

    public T next() {
        if (line == null) {
            return null;
        }
        T l = line;
        try {
            line = aux();
            if (line == null) {
                open = false;
                try {
                    close();
                } catch (Exception e) {
                    Log.util.error("Unable to close %s %e", e, e);
                }
            }
        } catch (IOException e) {
            Log.util.error("Exception reading %s %s %e", source, e, e);
        }
        return l;
    }

    private T aux() throws IOException {
        if (open == false) {
            // ensure we dont read past end of file.
            return null;
        }
        T line = factory.getObject();
        count++;
        if (line.read(blockInputStream)) {
            return line;
        }
        return null;
    }

    public void remove() {
        // NA
    }
}
