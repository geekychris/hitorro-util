/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

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
