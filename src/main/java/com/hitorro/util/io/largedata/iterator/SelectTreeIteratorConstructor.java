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
package com.hitorro.util.io.largedata.iterator;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.LikeRowMerger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Produce an iterator that merged n channels together
 */
public abstract class SelectTreeIteratorConstructor<E> {
    protected Comparator<E> comparator;
    protected Comparator<E> m_comparator;
    protected LikeRowMerger<E> m_merger;

    public Iterator<E> getIterator(final Comparator<E> comparator,
                                   final LikeRowMerger<E> merger,
                                   final File[] fList) throws IOException {
        Iterator[] inputIteratorArray = getInputIterators(fList);
        return getIteratorAux(comparator, merger, inputIteratorArray);
    }

    public Iterator<E> getIterator(final Comparator<E> comparator,
                                   final LikeRowMerger<E> merger,
                                   final BaseFile[] fList) throws IOException {
        Iterator[] inputIteratorArray = getInputIterators(fList);
        return getIteratorAux(comparator, merger, inputIteratorArray);
    }

    public Iterator<E> getIterator(final Comparator<E> comparator,
                                   final InputStream[] istreams) throws IOException {
        Iterator[] inputIteratorArray = getInputIterators(istreams);
        return getIteratorAux(comparator, m_merger, inputIteratorArray);
    }

    private Iterator<E> getIteratorAux(final Comparator<E> comp, LikeRowMerger<E> merger, final Iterator[] inputIteratorArray) {
        if (inputIteratorArray.length == 1) {
            return inputIteratorArray[0];
        }
        SelectionTreeIterator sti = new SelectionTreeIterator(comp, inputIteratorArray);

        if (merger != null) {
            // we want to apply the output rows together.
            Iterator iter = sti.removing(comp, merger);
            return iter;
        }
        return sti;
    }

    protected Iterator[] getInputIterators(File files[]) throws IOException {
        Iterator ptsArray[] = new Iterator[files.length];
        for (int i = 0; i < files.length; i++) {
            ptsArray[i] = getIterator(files[i]);
        }
        return ptsArray;
    }

    protected Iterator[] getInputIterators(BaseFile files[]) throws IOException {
        Iterator ptsArray[] = new Iterator[files.length];
        for (int i = 0; i < files.length; i++) {
            ptsArray[i] = getIterator(files[i]);
        }
        return ptsArray;
    }

    protected Iterator[] getInputIterators(InputStream istreams[]) throws IOException {
        Iterator ptsArray[] = new Iterator[istreams.length];
        for (int i = 0; i < istreams.length; i++) {
            ptsArray[i] = getIterator(istreams[i]);
        }
        return ptsArray;
    }

    protected abstract Iterator getIterator(File fc) throws IOException;

    protected abstract Iterator getIterator(BaseFile fc) throws IOException;

    protected abstract Iterator getIterator(InputStream is) throws IOException;
}
