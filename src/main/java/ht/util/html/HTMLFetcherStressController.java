package ht.util.html;

import ht.util.core.Env;
import ht.util.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 11:32:22 AM
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
    private long m_accumSuccessTimes = 0;
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
        m_accumSuccessTimes += timeToFetch;
    }

    public synchronized long getAverageFetchTime() {
        if (m_success == 0) {
            return 0;
        }
        return m_accumSuccessTimes / m_success;
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
