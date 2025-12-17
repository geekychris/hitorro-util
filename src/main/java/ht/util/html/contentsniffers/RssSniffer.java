package ht.util.html.contentsniffers;

import ht.util.html.TagFinder;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 5, 2005 Time: 3:44:18 PM
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
