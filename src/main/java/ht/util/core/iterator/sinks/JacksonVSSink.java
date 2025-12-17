package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.JsonValueSource;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public class JacksonVSSink extends BaseSink<JsonValueSource> {
    private JsonSink sink;

    public JacksonVSSink(BaseFile bf) throws IOException {
        sink = new JsonSink(bf);
    }

    public JacksonVSSink(OutputStream os) {
        sink = new JsonSink(os);
    }

    @Override
    public boolean init(JsonNode node) {
        return sink.init(node);
    }

    @Override
    public boolean start() {
        return sink.start();
    }

    public boolean add(JsonValueSource vs) {
        return sink.add(vs.getNode());
    }

    @Override
    public boolean stop() throws IOException {
        return sink.stop();
    }
}
