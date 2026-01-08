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
 * <?xml version="1.0" encoding="UTF-8"?> <!-- generator="wordpress/2.0.3" --> <rss version="2.0"
 * xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:wfw="http://wellformedweb.org/CommentAPI/"
 * xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"
 * xmlns:dtvmedia="http://participatoryculture.org/RSSModules/dtv/1.0" xmlns:media="http://search.yahoo.com/mrss" >
 * <p/>
 * <channel>
 * <p/>
 * <p/>
 * tactic:
 * <p/>
 * scan for the first occurences of an <rss> and a <channel> tag. If found, there order is rss first then its an rss
 * feed. if the rss tag has a version element after it, we can extract the version.
 */
public class RssSniffer implements ContentSniffer {
    public static final String MIME = "text/rss";

    public String getMimeType() {
        return MIME;
    }

    public String getTypeFromContent(String content, String believedContentType) {
        TagFinder tf = new TagFinder();
        tf.set(content, 3000);
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("rss")) {
                if (tf.findAttribute("version")) {
                    String version = tf.getAttributeValue();
                    return MIME;
                }
            }
        }
        return null;
    }
}
