package ht.util.io.largedata;

import ht.util.io.csv.CSVFormattedWriter;
import ht.util.io.csv.UnsupportedFormatterException;
import ht.util.io.largedata.compressedstreams.CInputStream;
import ht.util.io.largedata.compressedstreams.COutputStream;

import java.io.IOException;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 31, 2005 Time: 10:16:51 AM
 */
public interface CompressedStreamIO<T> {
    void write(COutputStream os) throws IOException;

    boolean read(CInputStream is) throws IOException;

    /**
     * Something recognizable as the end of the stream.
     *
     * @param os
     * @return
     * @throws IOException
     */
    boolean close(COutputStream os) throws IOException;

    void writeCSVRow(CSVFormattedWriter formatter) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException;

    /**
     * Size in bytes
     *
     * @return
     */
    long getSize();
}

