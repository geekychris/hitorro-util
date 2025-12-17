package ht.util.io.largedata.compressedstreams;

import ht.util.basefile.fs.BaseFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * XXX TODO CJC This code is taken from lucene 1.4.3 and is temporary we should consider license usage and or writing
 * our own version of these routines.
 */

public final class FSInputStream extends CInputStream {
    Descriptor file = null;
    boolean isClone;

    public FSInputStream(File path) throws IOException {
        file = new Descriptor(path, "r");
        length = file.length();
    }

    public FSInputStream(BaseFile path) throws IOException {
        file = new Descriptor(path.getLocalFileIfPossible(), "r");
        length = file.length();
    }

    /**
     * CInputStream methods
     */
    protected final void readInternal(byte[] b, int offset, int len)
            throws IOException {
        synchronized (file) {
            long position = getFilePointer();
            if (position != file.position) {
                file.seek(position);
                file.position = position;
            }
            int total = 0;
            do {
                int i = file.read(b, offset + total, len - total);
                if (i == -1) {
                    throw new IOException("read past EOF");
                }
                file.position += i;
                total += i;
            }
            while (total < len);
        }
    }

    public final void close() throws IOException {
        if (!isClone) {
            file.close();
        }
    }

    /**
     * Random-access methods
     */
    protected final void seekInternal(long position) {
    }

    protected final void finalize() throws IOException {
        close();            // close the file
    }

    public Object clone() {
        FSInputStream clone = (FSInputStream) super.clone();
        clone.isClone = true;
        return clone;
    }

    /**
     * Method used for testing. Returns true if the underlying file descriptor is valid.
     */
    boolean isFDValid() throws IOException {
        return file.getFD().valid();
    }

    private class Descriptor extends RandomAccessFile {
        /* DEBUG */
        //private String name;
        /* DEBUG */
        public long position;

        public Descriptor(File file, String mode) throws IOException {
            super(file, mode);
            /* DEBUG */
            //name = file.toString();
            //debug_printInfo("OPEN");
            /* DEBUG */
        }

    }
}