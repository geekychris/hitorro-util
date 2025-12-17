package ht.util.core.string;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 28, 2004 Time: 10:03:31 PM
 * <p/>
 * Description:
 * <p/>
 * Utility functions related to language
 */
public class LangUtil {
    /*
        Given a string like en_us, returns en
    */
    public static String getRootOfLocaleName(String lang) {
        int pos = lang.indexOf("_");
        if (pos >= 0) {
            return lang.substring(0, pos);
        }
        return null;
    }
}
