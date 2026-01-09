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

import com.hitorro.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ContentSniffers {
    private static ContentSniffers s_sniffers;
    private Map<String, ContentSniffer> m_sniffers = new HashMap<String, ContentSniffer>();
    private List<ContentSniffer> sniffersList = new ArrayList<ContentSniffer>();

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
        sniffersList.add(sniffer);
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
        for (ContentSniffer sn2 : sniffersList) {
            returnType = sn2.getTypeFromContent(content, mimeTypeHint);
            if (!StringUtil.nullOrEmptyOrBlankString(returnType)) {
                // we believe this one already.
                return returnType;
            }
        }
        return null;
    }
}
