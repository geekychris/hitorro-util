package ht.util.core;

/**
 * Simple timer that onece you
 *
 * @author chris
 */
public class Timer {
    private long startJMillis;
    private long totalJMillis;
    private long deltaJMillisStart;
    private String name;
    private long startCount;

    public Timer() {
        reset();
    }

    /**
     * Create a named timer and optionally start it
     *
     * @param timerName
     */
    public Timer(String timerName) {
        reset();
        name = timerName;
    }

    /**
     * Create a named timer and optionally start it
     *
     * @param timerName
     */
    public Timer(String timerName, boolean dontStart) {
        if (!dontStart) {
            reset();
        }
        name = timerName;
    }

    public long reset() {
        startJMillis = System.currentTimeMillis();
        deltaJMillisStart = startJMillis;
        totalJMillis = 0;
        startCount = 0;
        return startJMillis;
    }

    public long getStartCount() {
        return startCount;
    }

    public long getAverage() {
        return totalJMillis / startCount;
    }


    public void start() {
        startJMillis = System.currentTimeMillis();
        startCount++;
    }

    public long stop() {
        long time = System.currentTimeMillis() - startJMillis;
        totalJMillis += time;
        return time;
    }


    /**
     * measure the time since the last delta call
     *
     * @return
     */
    public long getDelta() {
        long now = System.currentTimeMillis();
        long delta = now - deltaJMillisStart;
        deltaJMillisStart = now;
        return delta;
    }

    public long getTime() {
        return totalJMillis;
    }

    public String toString() {
        return Long.toString(totalJMillis);
    }

    public String getTimerName() {
        return name;
    }
}
