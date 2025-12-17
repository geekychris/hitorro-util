package ht.util.basefile.tools.queue.writer.serializer;

import ht.util.basefile.fs.BaseFile;
import ht.util.typesystem.HTObjectOutputStream;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writable for simple byte array buffers.  We dont care what is in them.  The caller assumes responsibility for them
 * being self contained and not dependent on some kind of bookends within the file format.
 */
public class SimpleByteBufferWriterWritable implements WriterWritableInterface<ByteArrayWrapper> {
    protected String extension = "ser";
    DataOutputStream dos;
    OutputStream oos;

    public SimpleByteBufferWriterWritable(String extension) {
        this.extension = extension;
    }

    public SimpleByteBufferWriterWritable() {

    }

    public void applyCloseToFile(BaseFile f) throws IOException {
        OutputStream os = new BufferedOutputStream(f.getDataOutputStreamAppend());
        os.write(HTObjectOutputStream.EndOfStream);
        os.flush();
        os.close();
    }

    public boolean open(BaseFile f) throws IOException {
        oos = new BufferedOutputStream(f.getDataOutputStream());
        dos = new DataOutputStream(oos);
        return true;
    }

    public boolean close() throws IOException {
        if (dos != null) {
            dos.flush();
            oos.close();
            dos = null;
            oos = null;
        }
        return true;
    }

    public boolean write(ByteArrayWrapper wrapper) throws IOException {
        dos.write(wrapper.buff, 0, wrapper.size);
        dos.flush();
        return true;
    }

    public int getBytes(ByteArrayWrapper wrapper) {
        return wrapper.size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String ext) {
        extension = ext;
    }
}
