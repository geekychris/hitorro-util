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

import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Normalize url's following the 12 cardinal rules set down here:
 * <p/>
 * http://en.wikipedia.org/wiki/Url_normalization
 * <p/>
 * Unlike the rules set down there, here we we lowercase escape sequences not upper case them as it reduces the amount
 * of checks that need to be performed
 * <p/>
 */
public class UrlNormalizer {
    private static final int Unknown = 0;
    private static final int Index = 1;
    private static final int DotDot = 2;
    private static final int SingleDot = 3;
    private static final String WWW = "www.";

    private String host;
    private String port;
    private StringBuilder sb = new StringBuilder();
    private List<String> pathParts = new ArrayList();
    private List<String> args = new ArrayList();
    private UrlMemoryCursor cur = new UrlMemoryCursor();

    private boolean removeWWW = false;

    private boolean includeHttp;
    private boolean flipHost;

    private boolean includeAnchor = false;
    private String anchor;

    private StringBuilder buff = new StringBuilder();

    private TLDDictionary tldDictionary = TLDDictionarySingleton.me.get();
    private String dom;

    private PathComponentMatcher DefaultMatcher = new PathComponentMatcher(new TokenKey("<<Unknown>>", Unknown),
            new TokenKey("default.asp", Index),
            new TokenKey("Index.html", Index),
            new TokenKey("Index.htm", Index),
            new TokenKey("..", DotDot),
            new TokenKey(".", SingleDot));

    /**
     * @param flipHost    if true, takes a host name like foo.bla.com and reverses to com.bla.foo
     * @param includeHttp if true, the resultant url is of the form http://foo.bla.com if falst its foo.bla.com
     * @param removeWWW   - if true www.foo.bla.com becomes foo.bla.com
     */
    public UrlNormalizer(boolean flipHost, boolean includeHttp, boolean removeWWW) {
        this.flipHost = flipHost;
        this.includeHttp = includeHttp;
        this.removeWWW = removeWWW;
    }

    public void setIncludeAnchor(boolean flag) {
        includeAnchor = flag;
    }

    public long getTLDDomainHash() {
        if (dom == null) {
            dom = getTLDDomain();
        }
        if (StringUtil.nullOrEmptyString(dom)) {
            return 0;
        }
        return FPHash64.getFP(dom);
    }

    public String getTLDDomain() {
        if (dom != null) {
            return dom;
        }
        String parts[] = StringUtil.tokenizeFromSingleChar(host, ".");
        TLDEntry entry = tldDictionary.getMaxPath(parts);
        dom = tldDictionary.getNameFromResult(parts, buff, entry);
        return dom;
    }

    public String normalize(String input) {
        if (StringUtil.nullOrEmptyString(input)) {
            return null;
        }
        if (input.startsWith("#")) {
            return null;
        }
        dom = null;
        sb.setLength(0);
        pathParts.clear();
        args.clear();
        cur.scan(input);
        cur.resetToHost();
        sb.append("http://");
        port = null;
        anchor = null;

        do {
            switch (cur.getUrlPartType()) {
                case Host:
                    // just the host name
                    if (removeWWW && cur.startsWith(WWW)) {
                        host = cur.getTokenLowerCase().substring(WWW.length());
                    } else {
                        host = cur.getTokenLowerCase();
                    }
                    if (flipHost) {
                        String[] parts = StringUtil.tokenizeFromSingleChar(host, ".");
                        if (parts.length > 1) {
                            sb.setLength(0);
                            for (int i = parts.length - 1; i >= 0; i--) {
                                sb.append(parts[i]);
                                if (i > 0) {
                                    sb.append(".");
                                }
                            }
                            host = sb.toString();
                        }
                    }

                    break;
                case Port:
                    if (!cur.isTokenSameIgnoreCase("80")) {
                        port = cur.getToken();
                    }
                    break;
                case Anchor:
                    anchor = cur.getTokenLowerCase();
                    break;
                case Path:
                    TokenKey key = DefaultMatcher.match(cur);
                    switch (key.getId()) {
                        case Unknown:
                            // XXX probably need to deal with escape sequences
                            if (!cur.startsWith("#")) {
                                pathParts.add(cur.getTokenLowerCase());
                            }
                            break;
                        case Index:
                            // special case, assumes that we hit a default / index end of path file name
                            // in which case we terminate the path with a /
                            if (pathParts.size() > 0) {
                                pathParts.add("/");
                            }
                            break;
                        case SingleDot:
                            // dont do anything with single dots, or index path parts
                            break;
                        case DotDot:
                            int i = pathParts.size();
                            if (i > 0) {
                                // remove because its a /../
                                pathParts.remove(i - 1);
                                break;
                            }
                    }
                    break;
                case Argument:
                    sb.setLength(0);
                    String a = cur.getKeyPartOfArgToken();
                    cur.getKeyPartOfArgToken(sb, true);
                    sb.append("=");
                    cur.getValuePartOfArgToken(sb);
                    args.add(sb.toString());
                    break;
            }
        }
        while (cur.nextToken());

        Collections.sort(args);
        sb.setLength(0);
        if (includeHttp) {
            sb.append("http://");
        }
        sb.append(host);
        if (port != null) {
            sb.append(':');
            sb.append(port);
        }
        sb.append('/');
        boolean firstPart = true;
        for (String part : pathParts) {
            if (firstPart) {
                firstPart = false;
            } else {
                if (!part.equals("/")) {
                    sb.append('/');
                }
            }
            sb.append(part);
        }
        if (args.size() > 0) {
            boolean first = true;
            for (String arg : args) {
                if (first) {
                    first = false;
                    sb.append('?');
                } else {
                    sb.append('&');
                }
                sb.append(arg);
            }
        }

        if (includeAnchor) {
            if (!StringUtil.nullOrEmptyOrBlankString(anchor)) {
                sb.append('#');
                sb.append(anchor);
            }
        }

        return sb.toString();
    }


}
