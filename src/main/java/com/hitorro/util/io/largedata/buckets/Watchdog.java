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
package com.hitorro.util.io.largedata.buckets;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.thread.TimerCallback;

import java.io.IOException;

/**
 *
 */
public class Watchdog implements TimerCallback {
    private MaturingBucketWriter mbw;

    public Watchdog(MaturingBucketWriter p) {
        mbw = p;
    }

    /*
     * callback from the timer. If implementor returns true and the callback
     * thread is in loop mode, the timer will wait again and re-invoke.
     */
    public boolean callback() {
        try {
            mbw.flushBucketIfMature();
        } catch (IOException e) {
            Log.util.error("Unable to flush bucket %s %e", e, e);
        }
        return true;
    }
}