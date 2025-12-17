package ht.util.html;

import ht.util.core.Log;

import java.io.IOException;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 11:27:40 AM
 * <p/>
 * Thread of execution that rotates over a listFiles of URL's.  These urls are then fetched and the success / failures are
 * recorded
 */
public class HTMLFetcherStressClient implements Runnable {
    private HTMLFetcherStressController m_controller;
    private HTMLPageFetcher m_fetcher = new HTMLPageFetcher();

    public HTMLFetcherStressClient(HTMLFetcherStressController controller) {
        m_controller = controller;
    }

    public void run() {
        while (true) {
            for (String url : m_controller.getUrlList()) {
                if (!m_controller.running()) {
                    return;
                }
                try {
                    HTMLPage page = m_fetcher.fetchPage(url);
                    if (page == null || page.getSource() == null) {
                        // failure
                        m_controller.incrementFailures();
                        Log.util.info("Failed to fetch url %s", url);
                    } else {
                        long time = page.getLinkTime();
                        m_controller.incrementSuccess(time);
                        Log.util.info("Success fetching url %s, took %sms", url, time);
                        try {
                            List<Link> links = page.getLinks();
                        } catch (IOException e) {
                            Log.util.error("%s %e", e, e);
                        }
                    }
                } catch (Exception e) {
                    // just keep going no matter what!
                }
            }
        }
    }
}
