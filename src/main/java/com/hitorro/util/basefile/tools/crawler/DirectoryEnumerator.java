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
package com.hitorro.util.basefile.tools.crawler;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 * Enumerator of a nested directory structure.  You provide the root of the directory and a file constraint to ensure
 * that it only looks at directories you want and files that you want.
 */
public class DirectoryEnumerator implements Runnable {
    private BaseFileCrawler crawler;
    private int pauseSeconds;
    private Object notifyMe = new Object();

    public DirectoryEnumerator(BaseFile root, Sink<BaseFile> callback, HTPredicate<BaseFile> fileConstraint, int pauseSeconds) {
        crawler = new BaseFileCrawler();
        crawler.setCallback(callback);
        crawler.setOperator(fileConstraint);
        crawler.setRoot(root);
        this.pauseSeconds = pauseSeconds;
    }

    public void run() {
        while (true) {
            try {
                enumerate();
                Env.sleepNSeconds(pauseSeconds, notifyMe);
            } catch (IOException e) {
                Log.filesystem.error("Unable to access directory for enumeration", e, e);
            }
        }
    }

    public void enumerate() throws IOException {
        crawler.execute();
    }

    public void notifyChangeToEnumerate() {
        synchronized (notifyMe) {
            notifyMe.notifyAll();
        }
    }
}