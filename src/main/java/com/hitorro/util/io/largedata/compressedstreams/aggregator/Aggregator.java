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
package com.hitorro.util.io.largedata.compressedstreams.aggregator;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.io.csv.CSVFormattedWriter;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.CompressedStreamIO;

import java.io.IOException;
import java.util.Queue;

/**
 * Aggregator has the responsibility of
 */
public interface Aggregator<T extends CompressedStreamIO, O extends CompressedStreamIO> {
    HTPredicate<T> getDefaultOperator();

    /**
     * Setup the output file to be used for the aggregate output.
     *
     * @param targetFile
     * @return
     */
    boolean setOutput(BaseFile targetFile);

    /**
     * Regular aggregation of some kind of input object type.
     * <p/>
     * Implementation is specific, some may write to the targetfile as they process the input, some may output at the
     * end, some may use temporary files.
     * <p/>
     * Once close() is called, we know we must write out the final targetFile
     *
     * @param nll
     * @return
     */
    boolean aggregate(T nll);


    boolean close() throws IOException;


    /**
     * aggregate files MUST be mergable.  The target apply file is again defined with setOutput().
     *
     * @param sourceFiles
     * @return
     */
    boolean merge(Queue<BaseFile> sourceFiles) throws Exception;

    /**
     * Indication we want to recycle this object for use in another computation.  Re-init any data structures for
     * re-use.
     */
    void reset();

    String getFileExtension();

    String getDescription();

    /**
     * What aggregated reports are we dependent on.  If we are dependent on something, those dependencies must be
     * resolved first before we can create our report.
     *
     * @return
     */
    Aggregator[] getDependcies();

    /**
     * Define what is used as input.
     *
     * @return
     */
    Aggregator getInputFileType();

    BaseFileAccessingObjectFactory<O> getTargetObjectFactory();

    /**
     * The formatted writer knows how to apply the tuple to the appropriate field text.
     *
     * @return
     */
    CSVFormattedWriter getCSVFormattedWriter();
}
