package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.GenericKeyValue;
import ht.util.io.StoreException;

import java.io.IOException;

/**
 * Special Sink that is given two inputStream to K and V data sink mappers.
 * It will then write the K and the V to that file.  The Sinks MUST
 * ensure that they are writing all their record out to the stream
 */
public class KeyValueMappingSink<K, V> extends BaseSink<GenericKeyValue<K, V>> {
    private Sink<K> kSink;
    private Sink<V> vSink;

    public KeyValueMappingSink() {

    }

    public KeyValueMappingSink(Sink<K> keySink, Sink<V> valueSink) {
        setSinks(keySink, valueSink);
    }

    public void setSinks(Sink<K> keySink, Sink<V> valueSink) {
        kSink = keySink;
        vSink = valueSink;
    }

    @Override
    public boolean init(JsonNode node) {
        return false;
    }

    @Override
    public boolean start() throws IOException {
        kSink.start();
        vSink.start();
        return true;
    }

    @Override
    public boolean add(final GenericKeyValue<K, V> o) throws IOException, StoreException {
        kSink.add(o.getKey());
        vSink.add(o.getValue());
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        kSink.stop();
        vSink.stop();
        return true;
    }
}
