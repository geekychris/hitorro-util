package ht.util.html.contentsniffers;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 5, 2005 Time: 3:36:25 PM
 */
public interface ContentSniffer {
    String getMimeType();

    /**
     * Get the content type you believe we have.  Sniffs whatever content has been provided to determine what file type
     * it thinks it has.
     *
     * @param content
     * @param believedContentType
     * @return
     */
    String getTypeFromContent(String content, String believedContentType);
}
