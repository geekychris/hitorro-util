package ht.util.core.http;


import ht.util.core.Console;
import ht.util.core.*;
import ht.util.core.string.Base64;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Mar 1, 2005 Time: 7:46:03 AM
 */


public class HTTPClient {
    protected URL m_url;
    private InputStream in = null;
    private String m_auth = null;
    private URLConnection m_connection = null;
    private String m_contentType = "text/html";
    private List<KeyValue> m_kv = new ArrayList<KeyValue>();
    private boolean m_open = false;
    private String m_protocol = "http";
    // read timeout in ms.
    private int m_readTimeout = 4000;
    private OutputStream out = null;

    private static void dumpRaw(InputStream is) throws IOException {


        Console.println("=========================================");
        if (is != null) {

            int c = is.read();
            while (c != -1) {
                Console.print("%s", (char) c);
                c = is.read();
            }
            is.close();
        }
    }

    private static void dumpResponse(HTTPClient client, InputStream is) throws IOException {
        if (client != null) {
            List<KeyValue> l = client.getResponseHeader();
            for (KeyValue kv : l) {
                Console.println("%s = %s", kv.getKey(), kv.getValue());
            }
            Console.println("Response Code: %s", client.getResponseCode());
        }

        Console.println("=========================================");
        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = null;

            str = br.readLine();
            while (str != null) {
                Console.println(str);
                str = br.readLine();
            }
            is.close();
        }
    }

    public void addRequestParameter(String key, String value) {
        m_kv.add(new KeyValue(key, value));
    }

    /**
     * Finished processing http connection.
     */
    public void close() {
        if (!m_open) {
            return;
        }
        m_open = false;
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ioe) {

        }
    }

    public InputStream executeGet() {
        return executeReturningStream(null, null);
    }

    public InputStream executePut(byte[] outputBuffer, String contentFormat) {
        return executeReturningStream(outputBuffer, contentFormat);
    }

    private InputStream executeReturningStream(byte[] outputBuffer, String contentType) {
        try {
            boolean doOutput = false;

            m_connection = m_url.openConnection();

            m_connection.setDoInput(true);
            if (m_connection instanceof HttpURLConnection) {
                HttpURLConnection c = (HttpURLConnection) m_connection;
                if (outputBuffer != null) {
                    ((HttpURLConnection) m_connection).setRequestMethod("POST");
                    doOutput = true;
                } else {
                    ((HttpURLConnection) m_connection).setRequestMethod("GET");
                }
            }
            m_connection.setDoOutput(doOutput);
            m_connection.setUseCaches(false);
            m_connection.setAllowUserInteraction(false);
            m_connection.setReadTimeout(m_readTimeout);
            for (KeyValue kv : m_kv) {
                m_connection.setRequestProperty(kv.getKey(), kv.getValue());
            }

            if (!StringUtil.nullOrEmptyString(contentType)) {
                m_connection.setRequestProperty("Content-Length",
                        Integer.toString(outputBuffer.length));
                m_connection.setRequestProperty("Content-Type", contentType);
            }
            if (m_auth != null) {
                m_connection.setRequestProperty("Authorization", StringUtil.strcat("Basic ", m_auth));
            }
            m_open = true;

            if (outputBuffer != null) {
                out = m_connection.getOutputStream();
                out.write(outputBuffer);
                out.flush();
            }

            in = m_connection.getInputStream();
            return in;
        } catch (IOException x) {
            Log.httpfetcher.debug("Exception fetching url %s %e", x, x);
        } catch (StringIndexOutOfBoundsException ex) {
            Log.httpfetcher.debug("Exception fetching url %s %e", this.m_url, ex, ex);
        }
        return null;
    }


    public Map<String, List<String>> getHeader() {
        return m_connection.getHeaderFields();
    }

    public int getResponseCode() throws IOException {
        if (m_connection != null) {
            if (m_connection instanceof HttpURLConnection) {
                return ((HttpURLConnection) m_connection).getResponseCode();
            } else {
                // assume its ftp or something
                return 0;
            }
        }
        return -1;
    }

    public List<KeyValue> getResponseHeader() {
        List<KeyValue> l = new ArrayList<KeyValue>();
        Map<String, List<String>> header = getHeader();
        if (header == null) {
            return null;
        }
        Set<Map.Entry<String, List<String>>> entrySet = header.entrySet();
        Iterator<Map.Entry<String, List<String>>> iter = entrySet.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<String>> entry = iter.next();
            String key = entry.getKey();
            List<String> vals = entry.getValue();
            for (String val : vals) {
                l.add(new KeyValue(key, val));
            }
        }
        return l;
    }

    public String getResponseHeader(String name) {
        List<String> l = getHeader().get(name);
        if (ListUtil.nullOrEmpty(l)) {
            return null;
        }
        return l.get(0);
    }

    public URL getUrl() {
        if (m_connection == null) {
            return null;
        }
        return m_connection.getURL();
    }

    public String getUrl(String host, int port, String path) {
        return Fmt.S("%s://%s:%s/%s", m_protocol, host, Constants.getInteger(port), path);
    }

    public void resetKeyValueParameters() {
        m_kv.clear();
    }

    /**
     * Sets Authentication for this client. This will be sent as Basic Authentication header to the server as described
     * in <a href="http://www.ietf.org/rfc/rfc2617.txt"> http://www.ietf.org/rfc/rfc2617.txt</a>.
     */
    public void setBasicAuthentication(String user, String password) {
        if (user == null || password == null) {
            m_auth = null;
        } else {
            m_auth = Base64.encode(StringUtil.strcat(user, ":", password));
        }
    }

    /**
     * If the server provides the following in the response header: Accept-Ranges: bytes
     * <p/>
     * Its an indication that it should allow us to get subsets of the file back.
     * <p/>
     * After requesting a range, the response code should be a 206 Partial Content if its a 200 then the server ignored
     * the offset info.
     *
     * @param startOffset
     * @param stopOffset
     */
    public void setByteOffsetByRange(long startOffset, long stopOffset) {
        addRequestParameter("Range", Fmt.S("bytes=%s-%s", startOffset, stopOffset));
    }

    public void setContentType(String contentType) {
        m_contentType = contentType;
    }


    public boolean setHost(String url)
            throws IOException {
        m_url = new URL(url);

        return true;
    }

    /**
     * Connect to host using host name.
     *
     * @param host
     * @param port
     * @param path
     * @return
     * @throws UnknownHostException
     * @throws IOException
     */
    public boolean setHost(String host, int port, String path)
            throws IOException {
        m_url = new URL(getUrl(host, port, path));
        return true;
    }

    public void setReadTimoutInMillis(int millis) {
        m_readTimeout = millis;
    }
}














