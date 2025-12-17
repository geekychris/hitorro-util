package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;
import ht.util.core.opers.LogicalSelector;
import ht.util.io.StoreException;

import java.io.IOException;

/**
 * Sink that has a set of sinks.  Sink to this it decides which sub put to goto. Acts as a wrapper, uses simple logical
 * constraints to decide which path.  If there are too many possible outcomes a better algorithm should probably be
 * chosen as the LogicalSelector currently just scans a listFiles of logical operators
 */
public class SplitSink<I> extends BaseSink<I> {
    protected LogicalSelector<I, Sink<I>> ts = new LogicalSelector();
    private boolean failOnMissingSink = false;

    public SplitSink(boolean failOnMissingSink) {
        this.failOnMissingSink = failOnMissingSink;
    }

    public void add(HTPredicate<I> oper, Sink<I> sink) {
        ts.addSelection(oper, sink);
    }

    @Override
    public boolean init(JsonNode node) {
        boolean success = true;
        for (Sink<I> s : ts.getTargets()) {
            if (!s.init(node)) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public boolean start() {
        ts.finishSetup();
        return true;
    }

    @Override
    public boolean add(final I o) throws IOException, StoreException {
        Sink<I> sink = ts.select(o);
        if (sink != null) {
            return sink.add(o);
        }
        return !failOnMissingSink;
    }

    @Override
    public boolean stop() throws IOException {
        boolean success = true;
        for (Sink<I> s : ts.getTargets()) {
            if (!s.stop()) {
                success = false;
            }
        }
        return success;
    }
}
