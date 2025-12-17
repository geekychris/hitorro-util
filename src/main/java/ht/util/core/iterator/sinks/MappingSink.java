package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.function.Function;

/**
 * Yet another place to shuv a mapper (in the put??? what???). Well if you couple sinks together using such things as
 * SplitSink you may want to seperate your iterator of objects up before you convert to some other thing OR you may
 * simply want to use a apply as something that has a side such as count things.
 */
public class MappingSink<I, O> extends BaseSink<I> {
    private Sink<O> sink;

    private Function<I, O> mapper;

    public MappingSink(Sink<O> sink, Function<I, O> mapper) {
        this.sink = sink;
        this.mapper = mapper;
    }

    @Override
    public boolean init(JsonNode node) {
        return sink.init(node);
    }

    @Override
    public boolean start() throws IOException {
        return sink.start();
    }

    @Override
    public boolean add(final I o) throws IOException, StoreException {
        return sink.add(mapper.apply(o));
    }

    @Override
    public boolean stop() throws IOException {
        return sink.stop();
    }
}
