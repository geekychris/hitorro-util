package ht.util.servicecounters;

import ht.util.core.Env;


/**
 *
 */
public class CounterClock implements Runnable {
    private boolean running = true;
    private int secondsSleep = 60;
    private CounterContext cc;
    private Object notifier = new Object();

    private long ticks;

    public CounterClock(CounterContext cc) {
        this.cc = cc;
    }

    public Object getNotifier() {
        return notifier;
    }

    public long getTicks() {
        return ticks;
    }

    public void run() {
        while (running) {
            Env.sleepNSeconds(secondsSleep);
            if (cc != null) {
                cc.clock();
                synchronized (notifier) {
                    notifier.notifyAll();
                }
            }
        }
    }
}
