package ht.util.core.iterator.queue;

import ht.util.core.thread.EnhancedThreadGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParallelDequeue<E,O, T> {

    private List<DequeueParThread<E,O,T>> threads = new ArrayList<>();
    private ParallelQueueProcessor<E,O> dqp;
    private Function<E, O> mapper;
    private ThreadGroup tg;
    private String name;
    private int threadCount;

    public ParallelDequeue (AbstractDequeue<E> dequeue, AbstractEnqueue<O> targetEnqueue,
                            Function<E, O> mapper, String name, int threadCount) {
        this.dqp = new ParallelQueueProcessor<E,O>(dequeue, targetEnqueue);
        this.mapper = mapper;
        this.name = name;
        this.threadCount = threadCount;
        tg = new EnhancedThreadGroup(name);
    }

    public void startThreads () {
        for (int i = 0; i < threadCount; i++) {
            DequeueParThread<E,O,T> dpt = new DequeueParThread<>(tg, dqp, name, i, mapper);
            threads.add(dpt);
            dpt.start();
        }
    }

}
