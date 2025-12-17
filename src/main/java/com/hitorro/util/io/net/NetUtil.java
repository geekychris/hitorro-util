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
package com.hitorro.util.io.net;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility functions for network work (http, etc.)
 *
 * @author chris
 */
public class NetUtil {
    public static final String HOST_HASH_NAME = "host";
    public static final String HOST_HASH_ADDRESS = "address";

    public static final void foo() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface ni = e.nextElement();
            Console.println("DisplayName:%s mtu:%s, name:%s loopback:%s up:%s virtual:%s, hwaddress:%s, p2p:%s, multicastsupport:%s",
                    ni.getDisplayName(), ni.getMTU(), ni.getName(), ni.isLoopback(), ni.isUp(),
                    ni.isVirtual(), ni.getHardwareAddress(), ni.isPointToPoint(), ni.supportsMulticast());
        }
    }

    /**
     * Post data to an URL and return the first line of the response text.
     *
     * @param urlString
     * @param postData
     * @return the entire response text, or "Transmission error: 'error'" on error
     */
    public static String post(String urlString, Map postData) {
        try {
            // open up the output connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());

            Iterator itr = postData.keySet().iterator();
            boolean first = true;
            while (itr.hasNext()) {
                // to print ampersand between each item
                if (!first) {
                    out.print('&');
                } else {
                    first = false;
                }
                String key = (String) itr.next();
                String value = (String) postData.get(key);
                out.print(key);
                out.print('=');
                if (StringUtil.nullOrEmptyString(value)) {
                    out.print("");
                } else {
                    out.print(URLEncoder.encode(value, "UTF-8"));
                }
            }

            out.close();

            // read the first line of the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String result = "";
            while (true) {
                String resultLine = in.readLine();
                if (resultLine == null) {
                    break;
                }
                result += resultLine;
            }
            in.close();

            // we aren't going to use this connection again soon
            connection.disconnect();

            return result;
        } catch (IOException exc) {
            return "Transmission error: " + exc;
        }
    }

    /**
     * Read the content of a url into a string.
     *
     * @param sourceUrl the url that we should read
     * @param fullPage  true if we want to read the whole page.  If false we only read the header.
     * @return the content of the url.  Will be an empty (not null) object if we couldn't read the url.
     */
    public static StringBuilder readUrl(String sourceUrl, boolean fullPage) {
        // try to read the contents of the source
        StringBuilder input = new StringBuilder();
        try {
            // open up the output connection
            URL url = new URL(sourceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);

            // read through the </head>
            int blen = 4096;
            char[] buff = new char[blen];
            int tryCount = 0;
            while (tryCount < 3) {
                int read = reader.read(buff, 0, blen);
                if (read > 0) {
                    input.append(buff, 0, read);
                    if (!fullPage && input.indexOf("</head") > 0) {
                        break;
                    }
                } else {
                    tryCount++;
                }
                // pause a bit to let the input build up
                Env.sleepMillis(100);
            }
            reader.close();
        } catch (IOException exc) {
            Log.util.debug("IOerror in readUrl: %s", exc);
            return new StringBuilder();
        }

        return input;
    }


    public static String findRssUrlFromUrl(String sourceUrl) {
        // try to read the contents of the source
        StringBuilder input = readUrl(sourceUrl, false);

        // see if we can find the rss-alternate feed mention in the returned HTML.  Two examples:
        // <link rel="alternate" type="application/rss+xml" title="HiTorro.net: Technology" href="http://www.HiTorro.net/home/feed/" />
        // <link rel="alternate" type="application/rss+xml" title="Gadgetell RSS Feed" href="http://www.gadgetell.com/feed/" />
        // <link rel="alternate" type="application/rss+xml" title="RSS 2.0" href="/rss.xml" />

        // we don't parse the html.  Look for the <link and the closing />.  Then find target stuff in it.
        // we'll look over several links in case there are more than one
        int indx = 0;
        while (true) {
            indx = input.indexOf("<link", indx);
            if (indx < 0) {
                break;
            }
            int endx = input.indexOf("/>", indx);
            if (endx < 0) {
                break;
            }

            // look for rel
            int testx = input.indexOf("rel=\"alternate", indx);
            if (testx < 0) {
                break;
            }
            if (testx > endx) {
                indx = endx;
                continue;
            }

            // look for type
            testx = input.indexOf("application/rss+xml", indx);
            if (testx < 0) {
                break;
            }
            if (testx > endx) {
                indx = endx;
                continue;
            }

            // look for href and enclosing quotes
            testx = input.indexOf("href", indx);
            if (testx < 0) {
                break;
            }
            int hrefstart = input.indexOf("\"", testx);
            if (hrefstart < 0 || hrefstart >= input.length()) {
                break;
            }
            int hrefend = input.indexOf("\"", hrefstart + 1);
            if (hrefend < 0) {
                break;
            }
            if (hrefend > endx) {
                // found all the elements, but it ends outside the link element, keep looking
                continue;
            }

            // we've got everything
            String href = input.substring(hrefstart + 1, hrefend);

            if (href.startsWith("/")) {
                // relative url
                int slen = sourceUrl.length();
                if (sourceUrl.charAt(slen - 1) == '/') {
                    // we don't want the trailing slash
                    sourceUrl = sourceUrl.substring(0, slen - 1);
                }
                return StringUtil.strcat(sourceUrl, href);
            } else {
                // absolute url
                return href;
            }
        }

        // couldn't find it
        return null;
    }

    /**
     * @return HashMap mapping "name" => <host name>, and "address" => <IP address>
     */
    public static HashMap<String, String> getHostNameAndAddress() {
        String ignoreLocalhost = "127.0.0.1";
        String hostIPAddress = "";
        String hostName = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en != null) {
                while (en.hasMoreElements()) {
                    NetworkInterface ni = en.nextElement();
                    Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    if (addrs != null) {
                        while (addrs.hasMoreElements()) {
                            InetAddress addr = addrs.nextElement();
                            if (addr.getHostAddress() != null &&
                                    addr.getHostAddress().indexOf(ignoreLocalhost) == -1 &&
                                    addr.getHostAddress().indexOf(":") == -1) {
                                hostIPAddress = addr.getHostAddress();
                                hostName = addr.getHostName();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.util.error("Error: %s %e", e, e);
        }

        if (hostName.equals("") && hostIPAddress.equals("")) {
            try {
                InetAddress thisIp = InetAddress.getLocalHost();
                hostName = thisIp.getHostName();
                hostIPAddress = thisIp.getHostAddress();
            } catch (UnknownHostException e) {
                Log.util.error("Error: %s %e", e, e);
            }
        }

        HashMap<String, String> result = new HashMap<String, String>();
        result.put(HOST_HASH_NAME, hostName);
        result.put(HOST_HASH_ADDRESS, hostIPAddress);

        return result;
    }
}
