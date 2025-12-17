package ht.util.html.contentsniffers;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 5, 2005 Time: 3:44:01 PM
 * <p/>
 * Mug shot:
 * <p/>
 * .nohover a:hover { color: #c50; background: none; }
 * <p/>
 * h1, h2, h3, h4 { letter-spacing: -1px; font-family: trebuchet ms, sans-serif; } h1 { font-size: 2.4em; } h3 {
 * font-size: 1.6em; }
 * <p/>
 * div.alert { padding:10px; padding-bottom:30px; margin:10px; border:1px solid #ccc; background:#eee; }
 * <p/>
 * Tactic, look for margin:, border:, color:, if we find two out of 4 thats sufficient.
 */
public class StyleSheetSniffer implements ContentSniffer {
    public static final String MIME = "text/css";

    public String getMimeType() {
        return MIME;
    }

    private boolean has(String content, String key) {
        return content.indexOf(key) != -1;
    }

    public String getTypeFromContent(String content, String believedContentType) {
        int count = 0;
        if (has(content, "margin")) {
            count++;
        }
        if (has(content, "border")) {
            count++;
        }
        if (count > 1) {
            return MIME;
        }
        if (has(content, "color")) {
            count++;
        }
        if (count > 1) {
            return MIME;
        }
        if (has(content, "background")) {
            count++;
        }
        if (count > 1) {
            return MIME;
        }
        return null;
    }
}