package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.Log;
import ht.util.core.queue.ThreadedQueue;
import ht.util.core.queue.ThreadedQueueCanceledException;
import ht.util.core.queue.ThreadedQueueTimeoutException;
import ht.util.io.StoreException;

import java.io.IOException;

/**
 * Encapsulates a queue so that sinking can be asynchronous
 */
public class ThreadedQueueSink<E> extends BaseSink<E> {
    private ThreadedQueue<E> queue;
    private boolean completeQueueOnStop;
    private SinkPeerInterface peer;

    public ThreadedQueueSink(ThreadedQueue<E> queue, boolean completeQueueOnStop) {
        this.queue = queue;
        this.completeQueueOnStop = completeQueueOnStop;
    }

    public boolean peerCompleted() {
        if (peer != null) {
            return peer.completed();
        }
        return false;
    }

    /**
     * Possible the other end of this put is the SinkFromThreadedQueue, we need to keep track of that guy in the case it
     * was constructed via
     *
     * @param peer
     */
    void setPeer(SinkPeerInterface peer) {
        this.peer = peer;
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
    public boolean add(final E o) throws IOException, StoreException {
        try {
            queue.put(o);
        } catch (ThreadedQueueCanceledException e) {
            Log.queue.error("Unable to put to put %s %e", e, e);
            return false;
        } catch (ThreadedQueueTimeoutException e) {
            Log.queue.error("Unable to put to put %s %e", e, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        if (completeQueueOnStop) {
            queue.setQueueComplete();
        }
        return true;
    }
}
