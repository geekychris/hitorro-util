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
package com.hitorro.util.urlparser;

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.string.StringUtil;


/**
 * enumeration of parts of a url. It identfies host, path part and argument parts. No Strings are newed in the normal
 * operation of the cursor (though you can ask for the token for debug purposes).
 */
public class UrlCursor {
    public Part m_type;
    protected String m_url;
    protected int urlLength;
    protected int tokenStart;
    protected int tokenLength;
    protected int m_curr;
    protected boolean isSecure;
    protected int index = 0;
    protected int keyPartLength = 0;
    int dotOccur = 0;
    private StringBuilder sb = new StringBuilder();

    public static final String getSiteFromURL(String url) {
        return getSiteFromURL(url, new UrlCursor());
    }

    public static final String getSiteFromURL(String url, UrlCursor curs) {
        curs.setUrl(url);
        curs.nextToken();
        return curs.getAllToCurrentPos();
    }


    public boolean setUrl(String url) {
        index = 0;
        m_curr = 0;
        m_url = url;
        urlLength = url.length();
        if (urlLength < 5) {
            return false;
        }
        m_type = null;
        tokenStart = 0;
        skipProtocol();
        return true;
    }

    private boolean pass() {
        while (tokenStart < urlLength) {
            char c = m_url.charAt(tokenStart);
            if (c == '/') {
                if (m_type == null) {
                    m_type = Part.Host;
                } else {
                    m_type = Part.Path;
                }
            } else if (c == '?' || c == '&') {
                m_type = Part.Argument;
            } else if (c == '#') {
                m_type = Part.Anchor;
            } else if (c == ':') {
                m_type = Part.Port;
            } else {
                return true;
            }
            tokenStart++;
        }
        return false;
    }


    public boolean nextToken() {
        keyPartLength = 0;
        boolean inValue = false;
        tokenStart = tokenStart + tokenLength;
        if (!pass()) {
            return false;
        }
        if (tokenStart >= urlLength) {
            return false;
        }
        index++;
        for (int i = tokenStart; i < urlLength; i++) {
            char c = m_url.charAt(i);
            if (inValue) {
                if (c == '&') {
                    tokenLength = i - tokenStart;
                    return true;
                }
            } else if (c == '/' || c == '?' || c == '&' || c == '#' || c == ':') {
                tokenLength = i - tokenStart;
                return true;
            }
            if (c == '=') {
                inValue = true;
                keyPartLength = i - tokenStart;
            }
        }
        tokenLength = urlLength - tokenStart;

        return true;
    }

    public String getAllToCurrentPos() {
        try {
            int to = tokenStart + tokenLength;
            if (m_url.length() >= to) {
                return m_url.substring(0, to);
            } else {
                return Constants.EmptyString;
            }
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    public String getToken() {
        return m_url.substring(tokenStart, tokenStart + tokenLength);
    }

    public String getTokenLowerCase() {
        sb.setLength(0);
        getTokenLowerCase(sb);
        return sb.toString();
    }

    public void getTokenLowerCase(StringBuilder s) {
        for (int i = 0; i < tokenLength; i++) {
            s.append(Character.toLowerCase(m_url.charAt(tokenStart + i)));
        }
    }

    public String getTokenUpperCase() {
        sb.setLength(0);
        getTokenUpperCase(sb);
        return sb.toString();
    }

    public void getTokenUpperCase(StringBuilder s) {
        for (int i = 0; i < tokenLength; i++) {
            sb.append(Character.toUpperCase(m_url.charAt(tokenStart + i)));
        }
    }

    public boolean isTokenSameIgnoreCase(String buff, int pos, int size) {
        return StringUtil.subStringEqualsIgnoreCase(m_url, tokenStart, tokenLength, buff, pos, size);
    }

    /**
     * If this is an argument, get the key part
     *
     * @return
     */
    public String getKeyPartOfArgToken() {
        return m_url.substring(tokenStart, tokenStart + keyPartLength);
    }

    public void getKeyPartOfArgToken(StringBuilder sb, boolean lowerCase) {
        for (int i = tokenStart; i < tokenStart + keyPartLength; i++) {
            if (lowerCase) {
                sb.append(Character.toLowerCase(m_url.charAt(i)));
            } else {
                sb.append(m_url.charAt(i));
            }
        }
    }

    public String getValuePartOfArgToken() {
        int start = tokenStart + keyPartLength + 1;
        return m_url.substring(start, tokenStart + tokenLength);
    }

    public void getValuePartOfArgToken(StringBuilder sb) {
        int start = tokenStart + keyPartLength + 1;
        for (int i = start; i < tokenStart + tokenLength; i++) {
            sb.append(m_url.charAt(i));
        }
    }

    public boolean isKeyPartTokenSameIgnoreCase(String buff, int pos, int size) {
        return StringUtil.subStringEqualsIgnoreCase(m_url, tokenStart, keyPartLength, buff, pos, size);
    }

    public boolean isKeyPartTokenSameIgnoreCase(String buff) {
        return StringUtil.subStringEqualsIgnoreCase(m_url, tokenStart, keyPartLength, buff, 0, buff.length());
    }

    public boolean isValuePartTokenSameIgnoreCase(String buff) {
        return isValuePartTokenSameIgnoreCase(buff, 0, buff.length());
    }

    public boolean isValuePartTokenSameIgnoreCase(String buff, int pos, int size) {
        int start = tokenStart + keyPartLength + 1;
        return StringUtil.subStringEqualsIgnoreCase(m_url, start, tokenLength - start + tokenStart, buff, pos, size);
    }


    public boolean isTokenSameIgnoreCase(String buff) {
        return isTokenSameIgnoreCase(buff, 0, buff.length());
    }

    public boolean isTokenSameIgnoreCase(UrlCursor cur) {
        return isTokenSameIgnoreCase(cur.m_url, cur.tokenStart, cur.tokenLength);
    }


    public Part getUrlPartType() {
        return m_type;
    }

    /**
     * Parse the part to see if its of the form: xxx.yyy
     * <p/>
     * We scan backwards to get the last occurence
     *
     * @return
     */
    public boolean hasExtension() {
        if (keyPartLength == -1) {
            return false;
        }
        if (keyPartLength == 0) {
            for (int i = tokenStart + tokenLength - 1; i > tokenStart; i--) {
                if (this.m_url.charAt(i) == '.') {
                    keyPartLength = i - tokenStart;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean computeTLD() {
        if (keyPartLength == -1) {
            return false;
        }
        if (keyPartLength == 0) {
            boolean foundFirst = false;
            for (int i = tokenStart + tokenLength - 1; i > tokenStart; i--) {
                char c = m_url.charAt(i);
                if (c == '.' || c == '/') {
                    if (foundFirst == false) {
                        foundFirst = true;
                        continue;
                    }
                    keyPartLength = i - tokenStart;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Scan a url finding the end of the host part, such as:
     * <p/>
     * http://www.cnn.com https://wwww.cnn.com/ www.cnn.com:8002 www.cnn.com:8002/
     *
     * @return index of end, must be a fully qualified url (http[s]
     */
    private boolean skipProtocol() {
        m_type = Part.Host;
        tokenLength = 0;

        if (!StringUtil.startsWithIgnoreCase(m_url, "http")) {
            // could be something starting like /blablabla
            if (StringUtil.startsWithIgnoreCase(m_url, "/")) {
                tokenStart = 1;
            }
            // no http part, thats ok.
            return true;
        }
        tokenStart = 4;
        if (m_url.charAt(4) == 's') {
            isSecure = true;
            tokenStart = 5;
        } else {
            isSecure = false;
        }
        if (StringUtil.subStringEqualsIgnoreCase(m_url, tokenStart, 3, "://")) {
            tokenStart += 3;
            // we want it to still be on the / so that we can progress past it with next
            return true;
        } else {
            return false;
        }
    }

    public boolean isSecure() {
        return isSecure;
    }

    /**
     * @return
     */
    public int getTokenIndex() {
        return this.tokenStart;
    }

    public int getTokenLength() {
        return this.tokenLength;
    }

    public enum Part {
        Host, Port, Path, Argument, Anchor
    }
}
