package ht.util.io.largedata.compressedstreams.aggregator;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.opers.HTPredicate;
import ht.util.io.csv.CSVFormattedWriter;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;
import ht.util.io.largedata.CompressedStreamIO;

import java.io.IOException;
import java.util.Queue;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 27, 2005 Time: 4:45:46 PM
 * <p/>
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
