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

import com.hitorro.util.html.TagFinder;

/**
 * <p/>
 * Mug Shot:
 * <p/>
 * <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> <!--
 * Application: null --> <!-- Page: ces/CESFrontPage --> <!-- Generated: Fri Jan 05 12:25:09 PST 2007 --> <html> <head>
 * <meta name="generator" content="Tapestry Application Framework, version 4.0"/> <meta http-equiv="Content-Type"
 * content="text/html;charset=UTF-8"/> <title>Welcome to HiTorro</title> </head> <head> <meta http-equiv="Content-Type"
 * content="text/html; charset=utf-8"/>
 * <p/>
 * <title>ces website</title> <link rel="stylesheet" href="../reset-fonts-grids-min.css" type="text/css" media="screen">
 * <style type="text/css" media="screen">
 * <p/>
 * Tactic:
 * <p/>
 * look for first occurence of <!DOCTYPE> <html>  tag, if found we have html content
 */
public class HtmlSniffer implements ContentSniffer {
    public static final String MIME = "text/html";

    public String getMimeType() {
        return MIME;
    }

    public String getTypeFromContent(String content, String believedContentType) {
        TagFinder tf = new TagFinder();
        tf.set(content, 3000);
        boolean foundDT = false;
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("!DOCTYPE")) {
                foundDT = true;
                break;
            }
        }
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("html")) {
                return MIME;
            }
        }
        return null;
    }
}
