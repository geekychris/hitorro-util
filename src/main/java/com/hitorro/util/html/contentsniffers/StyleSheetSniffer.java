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
package com.hitorro.util.html.contentsniffers;

/**
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