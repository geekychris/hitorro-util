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
 * <p/>
 * Mug Shot:
 * <p/>
 * <?xml version="1.0" encoding="utf-8"?> <feed xmlns="http://www.w3.org/2005/Atom">
 * <p/>
 * <title>Example Feed</title> <link href="http://example.org/"/> <updated>2003-12-13T18:30:02Z</updated> <author>
 * <name>John Doe</name> </author> <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>
 * <p/>
 * <entry>
 */
public class AtomSniffer implements ContentSniffer {
    public static final String MIME = "text/atom";

    public String getMimeType() {
        return MIME;
    }

    public String getTypeFromContent(String content, String believedContentType) {
        TagFinder tf = new TagFinder();
        tf.set(content, 3000);
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("feed")) {
                return MIME;
            }
        }
        return null;
    }
}
