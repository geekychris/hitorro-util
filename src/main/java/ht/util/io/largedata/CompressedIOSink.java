package ht.util.io.largedata;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.sinks.Sink;
import ht.util.io.largedata.compressedstreams.COutputStream;

import java.io.IOException;

/**
 *
 */
public class CompressedIOSink<T extends CompressedStreamIO> implements Sink<T> {

    private COutputStream m_os;
    private BaseFileAccessingObjectFactory factory;
    private int count;

    public CompressedIOSink(BaseFile file, BaseFileAccessingObjectFactory factory) throws IOException {
        m_os = file.getCOutputStream();
        this.factory = factory;
    }


    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean add(final T cio) throws IOException {
        if (cio == null) {
            Log.util.error("CompressedStreamIO object null!!!");
        } else {
            cio.write(m_os);
            if (factory != null) {
                factory.returnObject(cio);
            }
        }
        count++;
        return false;
    }

    @Override
    public boolean stop() throws IOException {
        T t = (T) factory.getObject();
        t.close(m_os);
        m_os.close();
        return true;
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}
