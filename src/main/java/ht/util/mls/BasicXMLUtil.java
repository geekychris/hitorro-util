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
package ht.util.mls;

import ht.util.core.string.StringUtil;
import ht.util.xml.SimpleDOMNode;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 29, 2004 Time: 7:44:08 AM
 * <p/>
 * Description:
 */
public class BasicXMLUtil {
    public static final String DescriptionKey = "Description";
    public static final String LanguageKey = "language";

    public static final MultiLingualString getMLSFromDom(SimpleDOMNode node) {
        SimpleDOMNode nodes[] = node.getChildren(DescriptionKey);

        int count = nodes.length;
        if (count == 0) {
            return null;
        }

        MultiLingualString mls = new MultiLingualString();
        for (int i = 0; i < count; i++) {
            String text = nodes[i].getText();
            String language = nodes[i].getAttributeString(LanguageKey);
            if (!StringUtil.nullOrEmptyString(text) &&
                    !StringUtil.nullOrEmptyString(language)) {
                mls.addLanguage(language, text);
            } else {
                // XXX should log that you tried to load a language with a null
                // text or language
            }
        }
        return mls;
    }
}
