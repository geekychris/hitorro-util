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
package com.hitorro.util.basefile.fs.jarfile;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 */
public class JarItemIterator extends AbstractIterator<BaseFile> {
    private ZipInputStream zis;
    private ZipEntry ze;
    private boolean nextCalled = false;

    public JarItemIterator(BaseFile jar) {
        try {
            zis = new ZipInputStream(jar.getCompressionType().getInputCompressed(jar.getDataInputStream()));
            nextAux();
        } catch (IOException e) {
            Log.filesystem.error("Unable to initialize JarItemIterator for file %s %e %s", e, e, jar);
        }
    }

    private void nextAux() throws IOException {
        ze = zis.getNextEntry();
        nextCalled = false;
    }

    @Override
    public void close() throws IOException {
        zis.close();
    }

    @Override
    public boolean hasNext() {
        if (nextCalled) {
            try {
                nextAux();
            } catch (IOException e) {
                Log.filesystem.error("Unable to perform next on jar file %s %e", e, e);
                return false;
            }
        }
        return ze != null;
    }

    @Override
    public BaseFile next() {
        nextCalled = true;
        JarFileFile jff = new JarFileFile(zis, ze);
        ze = null;
        return jff;
    }

    @Override
    public void remove() {
    }
}
