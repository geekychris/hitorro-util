package ht.util.urlparser;

import ht.util.core.string.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 2, 2005 Time: 3:30:21 PM
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
