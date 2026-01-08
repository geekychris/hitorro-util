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

import com.hitorro.util.core.Log;

import java.io.IOException;
import java.util.List;

/**
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
