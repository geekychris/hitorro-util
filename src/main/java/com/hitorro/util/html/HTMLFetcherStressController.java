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
package com.hitorro.util.html;

import com.hitorro.util.core.Env;
import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class HTMLFetcherStressController {
    public static HTMLFetcherStressController s_controller = null;
    private int m_maxClients = 20;
    private long m_pauseBetweenThreadStart = 100;
    private long m_pauseBetweenFetches = 1000;
    private List<String> m_urls = new ArrayList<String>();
    private List<Thread> m_threads = new ArrayList<Thread>();
    private File m_inputFile;
    private int m_failures = 0;
    private int m_success = 0;
    private long accumSuccessTimes = 0;
    private boolean m_running = false;

    public void setInputFile(String file) throws FileNotFoundException {
        m_inputFile = new File(file);
        Iterator<String> iter = FileUtil.getLineReaderIteratorFromFile(m_inputFile);
        m_urls.clear();
        while (iter.hasNext()) {
            m_urls.add(iter.next());
        }
    }

    public int getMaxClients() {
        return m_maxClients;
    }

    public long getPauseBetweenThreadStart() {
        return m_pauseBetweenThreadStart;
    }

    public long getPauseBetweenFetchesInMS() {
        return m_pauseBetweenFetches;
    }

    public List<String> getUrlList() {
        return m_urls;
    }

    public boolean running() {
        return m_running;
    }

    public void stop() {
        m_running = false;
        m_threads.clear();
    }

    public void startClients() {
        m_running = true;
        m_threads.clear();
        for (int i = 0; i < getMaxClients(); i++) {
            HTMLFetcherStressClient client = new HTMLFetcherStressClient(this);
            Thread t = new Thread(client);
            m_threads.add(t);
            t.start();
            Env.sleepMillis(this.getPauseBetweenThreadStart());
        }
    }

    public synchronized void incrementSuccess(long timeToFetch) {
        m_success++;
        accumSuccessTimes += timeToFetch;
    }

    public synchronized long getAverageFetchTime() {
        if (m_success == 0) {
            return 0;
        }
        return accumSuccessTimes / m_success;
    }

    public int getSuccesses() {
        return m_success;
    }

    public int getFailures() {
        return m_failures;
    }

    public synchronized void incrementFailures() {
        m_failures++;
    }
}
