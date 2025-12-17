package ht.util.html.contentsniffers;

import ht.util.html.TagFinder;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 5, 2005 Time: 3:44:25 PM
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
