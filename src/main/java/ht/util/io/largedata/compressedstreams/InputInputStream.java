package ht.util.io.largedata.compressedstreams;

import java.io.IOException;

/**
 * Lucene style Input Stream that wraps a java input stream.
 * <p/>
 * Note one should not use the seek method as seeking in an input stream doesnt make sense User: chris
 */
public class InputInputStream extends CInputStream {
    java.io.InputStream is;
    long isPosition = 0;
    boolean isClone;

    public InputInputStream(java.io.DataInputStream is, long expectedLength) throws IOException {
        this.is = is;
        length = expectedLength;
    }

    public void seek(long pos) throws IOException {
        throw new IOException("InputInputStream can not be seeked!");
    }

    /**
     * CInputStream methods
     */
    protected final void readInternal(byte[] b, int offset, int len) throws IOException {
        synchronized (is) {
            long position = getFilePointer();
            if (position != isPosition) {
                throw new IOException("position != isPosition");
            }
            int total = 0;
            do {
                int i = is.read(b, offset + total, len - total);
                if (i == -1) {
                    // now set length to the correct amount since we now know the end of the buffer
                    length = isPosition;
                }
                isPosition += i;
                total += i;
            }
            while (total < len);
        }
    }

    public final void close() throws IOException {
        if (!isClone) {
            is.close();
        }
    }

    /**
     * Random-access methods
     */
    protected final void seekInternal(long position) throws IOException {
    }

    protected final void finalize() throws IOException {
        close();            // close the file
    }

    public Object clone() {
        FSInputStream clone = (FSInputStream) super.clone();
        clone.isClone = true;
        return clone;
    }
}
