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

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.KeyValue;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.http.HTTPClient;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.urlparser.URLUtil;

import java.io.*;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;

/**
 * page increment the fetch count.  Can be used multiple times by a single thread. Fetching follows redirects.
 */
public class HTMLPageFetcher {
    public static final String DefaultAgent_String = "Mozilla/5.0 (compatible; MSIE 6.0; HiTorro; crawler_admin@hitorro.net)";
    public static final String Firefox3_5_agent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2";
    public static final int DEFAULT_MAX_REDIRECTS = 16;

    private String defaultAgent = DefaultAgent_String;
    private long maxRawPageLen = 1024 * 1024;
    private long httpTimeout = 5 * Constants.MillisInSecond;

    private HTTPClient m_httpClient = new HTTPClient();
    private long pagesFetched = 0;
    private int m_redirects;
    private long accumParseTime;
    private long accumFetchTime;

    public HTMLPageFetcher() {

    }

    public void setDefaultAgent(String agent) {
        defaultAgent = agent;
    }

    public void setHttpTimeout(int timeoutMs) {
        httpTimeout = timeoutMs;
        // re-init the manager as the timeouts have changed.
        initManager();
    }

    public int getCode() throws IOException {
        return m_httpClient.getResponseCode();
    }

    public void setMaxRawPageLength(int size) {
        maxRawPageLen = size;
    }

    private void initManager() {
        m_httpClient.setReadTimoutInMillis((int) httpTimeout);
    }

    public long getHTTPTimeout() {
        return httpTimeout;
    }

    public HTMLPage fetchPage(String url) {
        m_redirects = 0;
        accumFetchTime = 0;
        accumParseTime = 0;
        HTMLPage page = new HTMLPage();
        fetchPage(m_httpClient, url, page, true);
        pagesFetched++;
        if (!StringUtil.nullOrEmptyString(page.getSource()) && !StringUtil.nullOrEmptyString(page.getUrl())) {
            return page;
        }
        Log.httpfetcher.debug("Redirects %s accum fetch time %s, accum parseTime %s, URL: %s",
                m_redirects, accumFetchTime, accumParseTime, url);
        return null;
    }

    public HTMLPage fetchPageSource(HTTPClient client, String url, boolean truncate) {
        if ((client != null) && (!StringUtil.nullOrEmptyString(url))) {
            HTMLPage page = new HTMLPage();
            fetchPage(client, url, page, truncate);
            return page;
        } else {
            return null;
        }
    }

    /**
     * Fetch the content of the page. follow re-direct page. Note that the case of the tag cannot be guaranteed.
     * <p/>
     * <META HTTP-EQUIV="Refresh" CONTENT="5; URL=html-redirect.html">
     */
    private void fetchPage(HTTPClient client, String url, HTMLPage page,
                           boolean truncate) {
        try {
            if (StringUtil.nullOrEmptyString(url)) {
                page.setDestinationUrl(null);
                page.setSource(null);
            } else {
                fetchRaw(client, url, page, truncate);

                // following redirects
                for (int attempt = 0; attempt < DEFAULT_MAX_REDIRECTS; ++attempt) {
                    if (page.getSource() != null) {
                        // this could be optimized if we didnt want to parse the dom....due to case sensitivity issues
                        // I chose to forfeit performance for accuracy.
                        if (followRedirect(url, page, client, truncate)) {
                            break;
                        }
                    } else {
                        // cannot continue without page content
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        } finally {
            // recover original URL (after redirection)
            page.setUrl(url);
        }

        return;
    }

    private boolean followRedirect(String url,
                                   HTMLPage page,
                                   HTTPClient client,
                                   boolean truncate) throws IOException {
        HTMLParser parser = page.getParser();
        accumParseTime += page.getParseTime();
        String redirectUrl = parser.getRedirectURL(url);
        if (!StringUtil.nullOrEmptyOrBlankString(redirectUrl)) {
            fetchRaw(client, redirectUrl, page, truncate);
            page.incrementRefresh();
            return false;
        }
        return true;
    }

    /**
     * Fetch the content of the page. Followin re-direct.
     */
    private void fetchRaw(HTTPClient client, String url, HTMLPage page,
                          boolean truncate) {
        m_redirects++;
        Date lastMod = null;
        Date expires = null;

        BufferedReader bufReader = null;
        String agentId = defaultAgent;

        StringBuilder buf = new StringBuilder();
        try {
            // clean up the URL  before fetching (e.g., put in the missing protocol)
            url = URLUtil.cleanupUrl(url);


            client.resetKeyValueParameters();

            client.setHost(url);
            client.addRequestParameter("User-Agent", agentId);


            Timer t = new Timer();
            InputStream is = client.executeGet();
            if (is != null) {
                List<KeyValue> header = client.getResponseHeader();
                //Header[] header = get.getResponseHeaders();
                page.setHeader(header);

                BufferedInputStream bis = new BufferedInputStream(is);
                String encoding = HTMLUtil.getCharEncodingFromBufferedInputStream(bis, 500);

                bufReader = new BufferedReader(new InputStreamReader(bis, encoding));

                // XXX we may need to deal with redirects that are other than soft redirects.
                page.setDestinationUrl(m_httpClient.getUrl().toString());

                String str;
                long srcLen = 0;
                str = bufReader.readLine();
                while (str != null && srcLen < maxRawPageLen) {
                    long lineLen = str.length();

                    if (!truncate || (lineLen + srcLen <= maxRawPageLen)) {
                        buf.append(str);
                    } else {
                        buf.append(str, 0, (int) (maxRawPageLen - srcLen - 1));
                        Log.httpfetcher.warn("Truncated the page at %str KB, URL: %s", maxRawPageLen / 1024, url);
                        page.setTruncated();
                    }
                    buf.append("\n");
                    srcLen += lineLen + 1;
                    str = bufReader.readLine();
                }


                page.setLinkTime(t.getTime());
                page.setSource(buf.toString());

                setLastModified(client, lastMod, page);
                setExpires(client, expires, page);
                page.setExceptionReason(HTMLPage.Exception.None);
            } else {
                page.setExceptionReason(HTMLPage.Exception.Timeout);
                page.setTruncated();
            }
            t.stop();
            accumFetchTime += t.getTime();
        } catch (SocketTimeoutException ste) {
            Log.util.debug("HtmlPageFetcher: Couldn't fetch the page at %s, %s ", ste, url);
            page.setExceptionReason(HTMLPage.Exception.Timeout);
            page.setTruncated();
            page.setSource(buf.toString());
        } catch (IOException ioe) {
            Log.util.debug("HtmlPageFetcher: Couldn't fetch the page at %s, %s ", ioe, url);
            page.setExceptionReason(HTMLPage.Exception.IOException);
            page.setSource(null);
        } finally {
            if (bufReader != null) {

                try {
                    bufReader.close();
                } catch (IOException e) {
                    Log.util.error("Error reading buffer %s %e", e, e);
                }

            }
        }
    }

    private void setExpires(HTTPClient client, Date expires, HTMLPage page) {
        String eTime = client.getResponseHeader("Expires");

        if (eTime != null && eTime.length() > 20) {
            DateFormat dateFmt = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            expires = dateFmt.parse(eTime, new ParsePosition(14));
        }


        page.setExpirationDate(expires);
    }

    private void setLastModified(HTTPClient client, Date lastMod, HTMLPage page) {

        String modTime = client.getResponseHeader("Last-Modified");

        if (modTime != null && modTime.length() > 20) {
            DateFormat dateFmt = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            lastMod = dateFmt.parse(modTime, new ParsePosition(14));
        }


        if (lastMod == null) {
            // didnt find a last modified, lets pretend its 45 days
            lastMod = new Date(System.currentTimeMillis() - 45 * 24 * 60 * 60 * 1000l);
        }

        page.setLastModified(lastMod);
    }
}






