package ht.util.html.contentsniffers;

import ht.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 5, 2005 Time: 3:51:08 PM
 */
public class ContentSniffers {
    private static ContentSniffers s_sniffers;
    private Map<String, ContentSniffer> m_sniffers = new HashMap<String, ContentSniffer>();
    private List<ContentSniffer> m_sniffersList = new ArrayList<ContentSniffer>();

    public static ContentSniffers getSniffers() {
        if (s_sniffers == null) {
            ContentSniffers t = new ContentSniffers();
            t.addSniffer(new HtmlSniffer());
            t.addSniffer(new StyleSheetSniffer());
            t.addSniffer(new RssSniffer());
            t.addSniffer(new AtomSniffer());
            s_sniffers = t;
        }
        return s_sniffers;
    }

    public static void setSniffers(ContentSniffers sn) {
        s_sniffers = sn;
    }

    /**
     * Add a sniffer to the listFiles of sniffers.  Execution order is in the order they are added to this listFiles, this is so
     * sniffers can depend on what previous sniffers fail on (for instance, the css sniffer knows it cant be html as the
     * html sniffer has already been run).
     *
     * @param sniffer
     */
    public void addSniffer(ContentSniffer sniffer) {
        m_sniffers.put(sniffer.getMimeType(), sniffer);
        m_sniffersList.add(sniffer);
    }

    /**
     * Attempt to figure out the type of a piece of content...the content does not have to be complete
     *
     * @param content
     * @param mimeTypeHint
     * @return
     */
    public String getMimeType(String content, String mimeTypeHint) {

        ContentSniffer sn = null;
        String returnType = null;
        if (!StringUtil.nullOrEmptyString(mimeTypeHint)) {
            sn = m_sniffers.get(mimeTypeHint.toLowerCase());
            if (sn != null) {
                returnType = sn.getTypeFromContent(content, mimeTypeHint);
                if (!StringUtil.nullOrEmptyOrBlankString(returnType)) {
                    // we believe this one already.
                    return returnType;
                }
            }
        }
        for (ContentSniffer sn2 : m_sniffersList) {
            returnType = sn2.getTypeFromContent(content, mimeTypeHint);
            if (!StringUtil.nullOrEmptyOrBlankString(returnType)) {
                // we believe this one already.
                return returnType;
            }
        }
        return null;
    }
}
