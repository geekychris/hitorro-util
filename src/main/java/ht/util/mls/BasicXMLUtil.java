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
