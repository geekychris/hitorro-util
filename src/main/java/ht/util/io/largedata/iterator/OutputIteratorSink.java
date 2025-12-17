package ht.util.io.largedata.iterator;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.sinks.IteratorSink;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 5:20:04 PM
 * <p/>
 * Given an iterator, put its data to some kind of output (for example HTSerializable to a file).
 */
public interface OutputIteratorSink<T> extends IteratorSink<T> {
    /**
     * If you wish to put to a file then provide a file name here...it will automatically close the file stream on
     * completion.
     *
     * @param file
     * @throws FileNotFoundException
     */
    void setFile(BaseFile file) throws IOException;

    /**
     * Set the output stream to write the content to.
     *
     * @param os
     * @param shouldCloseOnCompletion
     */
    void setOutput(OutputStream os, boolean shouldCloseOnCompletion);
}
