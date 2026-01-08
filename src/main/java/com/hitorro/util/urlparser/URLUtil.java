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

import com.hitorro.util.core.string.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 */
public class URLUtil {

    public static final String getSiteFromURL(String url) {
        UrlCursor curs = new UrlCursor();
        return getSiteFromURL(url, curs);
    }

    public static final String getSiteFromURL(String url, UrlCursor curs) {
        curs.setUrl(url);
        curs.nextToken();
        return curs.getAllToCurrentPos();
    }

    public static final String cleanupUrl(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
            return new URL(url).toString();
        } catch (MalformedURLException mue) {
            return url;
        } catch (UnsupportedEncodingException e) {
            return url;
        } catch (java.lang.IllegalArgumentException e) {
            return "";
        }
    }

    public static final String cleanupUrlAndCutToLength(String url, int length) {
        return StringUtil.truncateToLength(cleanupUrl(url), length);
    }
}
