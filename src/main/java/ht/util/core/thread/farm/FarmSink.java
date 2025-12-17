package ht.util.core.thread.farm;

import ht.util.core.iterator.queue.AbstractDequeue;
import ht.util.core.iterator.queue.AbstractEnqueue;
import ht.util.core.iterator.sinks.Sink;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sink for a generified threaded queue, knows how to shutdown once the queue is complete.
 *
 * @param <I>
 * @author chris
 */
public class FarmSink<I> implements Runnable {
    private long timeout = 10 * 1000;

    private ThreadGroup group;

    private AbstractEnqueue<I> queue;
    private AbstractDequeue<I> dequeue;

    private Sink<I> command;

    private String name;

    private Thread thread;

    private boolean completed = false;

    private int processedItemCount = 0;

    public FarmSink(String name, ThreadGroup group, AbstractEnqueue<I> in,
                    Sink<I> command) {
        this.command = command;
        this.group = group;
        this.name = name;
        this.queue = in;
        this.dequeue = (AbstractDequeue<I>) queue.dequeue();
    }

    public void start() {
        thread = new Thread(group, this, name);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        thread = null;
    }

    public boolean getCompleted() {
        return completed;
    }

    public int getProcessedCount() {
        return processedItemCount;
    }

    public void run() {
        while (!queue.getQueueComplete()) {
            try {
                I i = dequeue.poll(timeout, TimeUnit.MILLISECONDS);
                if (i != null) {
                    command.accept(i);
                    processedItemCount++;
                }
            } catch (InterruptedException e) {

            }
        }
        try {
            command.close();
        } catch (IOException e) {

        }
        completed = true;
    }
}
