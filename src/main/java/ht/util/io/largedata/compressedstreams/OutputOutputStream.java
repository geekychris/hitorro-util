package ht.util.io.largedata.compressedstreams;

import java.io.IOException;

public class OutputOutputStream extends COutputStream {
    java.io.DataOutputStream os;

    public OutputOutputStream(java.io.DataOutputStream os) throws IOException {
        this.os = os;
    }

    /**
     * output methods:
     */
    public final void flushBuffer(byte[] b, int size) throws IOException {
        os.write(b, 0, size);
    }

    public final void close() throws IOException {
        super.close();
        os.close();
    }

    /**
     * Random-access methods
     */
    public final OutputOutputStream seek(long pos) throws IOException {
        throw new IOException("Cannot seek with OutputOutputStream");
    }

    public final long length() throws IOException {
        // undefined
        return -1;
    }

    protected final void finalize() throws IOException {
        // saw this weird case when the finalizer was getting a null pointer exception here
        // I suspect that if the constructor above fails, the VM still calls the finalizer
        // on the half-created object
        // I had my debugger set to break at NPEs and it stopped the finalizer thread
        // this is probably not needed, but safe
        if (os != null) {
            os.close();          // close the file
        }
    }

}

