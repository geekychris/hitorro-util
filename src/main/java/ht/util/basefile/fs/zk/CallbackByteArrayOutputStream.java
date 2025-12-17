package ht.util.basefile.fs.zk;

import java.io.ByteArrayOutputStream;

public class CallbackByteArrayOutputStream extends ByteArrayOutputStream {
    private ZKFile file;

    public CallbackByteArrayOutputStream(ZKFile file) {
        this.file = file;
    }

    public void close() {

    }
}
