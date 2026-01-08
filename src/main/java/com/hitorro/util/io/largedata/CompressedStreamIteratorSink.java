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
import com.hitorro.util.io.largedata.compressedstreams.FSOutputStream;
import com.hitorro.util.io.largedata.iterator.OutputIteratorSink;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p/>
 */
public class CompressedStreamIteratorSink<T extends CompressedStreamIO> implements OutputIteratorSink<T> {
    private FSOutputStream m_os;
    private Iterator<T> m_iter;
    private BaseFileAccessingObjectFactory factory;
    private BaseFile f;


    public void setFile(BaseFile file) throws IOException {
        m_os = new FSOutputStream(file);
        f = file;
    }

    public void setOutput(java.io.OutputStream os, boolean shouldCloseOnCompletion) {
        // Not implemented
        Log.util.error("setOutput is not supported in CompressedStreamIteratorSink");
    }

    public void setFactory(BaseFileAccessingObjectFactory fact) {
        factory = fact;
    }

    public void setIterator(Iterator<T> iter) {
        m_iter = iter;
    }

    public int sink() throws IOException {
        int count = 0;
        CompressedStreamIO cio = null;
        while (m_iter.hasNext()) {
            cio = m_iter.next();
            if (cio == null) {
                Log.util.error("CompressedStreamIO object null!!!");
            } else {
                cio.write(m_os);
                if (factory != null) {
                    factory.returnObject(cio);
                }
            }
            count++;
        }
        if (cio != null) {
            // if we dont have a cio we must of never processed a row anyway!
            // must do this to mark the stream with a null terminator.
            cio.close(m_os);
        }
        // this should NOT be in an else as not all CIO's will close the file!
        m_os.close();

        return count;
    }
}

