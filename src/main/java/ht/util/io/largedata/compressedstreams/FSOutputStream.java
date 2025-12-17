package ht.util.io.largedata.compressedstreams;

import ht.util.basefile.fs.BaseFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Taken from Lucene and modified for a narrow compressed stream use case.
 */

public class FSOutputStream extends COutputStream {
    RandomAccessFile file = null;

    public FSOutputStream(BaseFile path) throws IOException {
        this(path.getLocalFileIfPossible());
    }

    public FSOutputStream(File path) throws IOException {
        file = new RandomAccessFile(path, "rw");
        file.setLength(0); // Truncate the file if it already exists
    }

    public FSOutputStream(File path, boolean append) throws IOException {
        file = new RandomAccessFile(path, "rw");
        if (append) {
            // seek to the end of the file (setting the fp correctly)
            this.seek(file.length());
        } else {
            file.setLength(0); // Truncate the file if it already exists
        }
    }

    /**
     * output methods:
     */
    public final void flushBuffer(byte[] b, int size) throws IOException {
        file.write(b, 0, size);
    }

    public final void close() throws IOException {
        super.close();
        file.close();
    }

    /**
     * Random-access methods
     */
    public final FSOutputStream seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
        return this;
    }

    public final long length() throws IOException {
        return file.length();
    }

    protected final void finalize() throws IOException {
        // saw this weird case when the finalizer was getting a null pointer exception here
        // I suspect that if the constructor above fails, the VM still calls the finalizer
        // on the half-created object
        // I had my debugger set to break at NPEs and it stopped the finalizer thread
        // this is probably not needed, but safe
        if (file != null) {
            file.close();          // close the file
        }
    }

}
