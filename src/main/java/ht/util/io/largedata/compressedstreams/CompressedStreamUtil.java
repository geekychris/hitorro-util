package ht.util.io.largedata.compressedstreams;

import ht.util.io.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Utility functions for accessing compressed streams of data
 *
 * @author chris
 */
public class CompressedStreamUtil {

    /**
     * Create an compressed input stream either disk or ram based.
     *
     * @param file
     * @param ramBased
     * @return input stream that is ram based (require no further io) or a disk based input stream that uses a 1k
     * buffer.
     * @throws IOException
     */
    public static final CInputStream getInputStream(File file, boolean ramBased)
            throws IOException {
        if (ramBased) {
            return getInputStreamRAM(file);
        } else {
            return getInputStreamDisk(file);
        }
    }

    public static final CInputStream getInputStreamFromByteArray(byte buff[]) {
        RAMInputStream is = new RAMInputStream(null);
        is.setBuffer(buff);
        return is;
    }

    /**
     * Ram based input stream.  reads the whole file into memory and wraps it in a ram input stream.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static final CInputStream getInputStreamRAM(File file)
            throws IOException {
        byte buffer[] = FileUtil.getFileAsByteArray(file);
        RAMInputStream is = new RAMInputStream(null);
        is.setBuffer(buffer);
        return is;
    }

    /**
     * Disk based input stream.  All reads go via a 1k buffer but ultimately require disk io's...though the os may be
     * good to you :-}
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static final CInputStream getInputStreamDisk(File file)
            throws IOException {
        FSInputStream is = new FSInputStream(file);
        return is;
    }

}
