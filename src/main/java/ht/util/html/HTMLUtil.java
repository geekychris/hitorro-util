package ht.util.html;

import ht.jsontypesystem.JVS;
import ht.util.core.CommandArgs;
import ht.util.core.string.StringBuilderUtil;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.propaccess.PropaccessError;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 15, 2006 Time: 7:12:49 PM
 */
public class HTMLUtil {
    /**
     * StripBeforeAndAfterHTML removes any text before <html> and after </html>. Note that there is often text before
     * <html>, such as <br> <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"><br>
     * <p/>
     * This function is public so it can be unit tested. :(
     */
    public static String getHTMLStripped(String htmlString) {
        int numChars = 1000;
        htmlString = getStrpAfterHTML(htmlString, numChars);
        htmlString = getStripAfterHTML(htmlString, numChars);
        return htmlString;
    }

    /**
     * Strip text before <html> in the page string
     *
     * @param htmlString String containing all HTML page including tags.
     * @param numChars   Don't search more than numChars when looking for <html>
     * @return String containing the new html
     */
    public static final String getStrpAfterHTML(String htmlString, int numChars) {
        if (htmlString != null) {
            String startTag = "<html";
            if (htmlString.length() > startTag.length()) {
                if (!htmlString.substring(0, startTag.length()).toLowerCase()
                        .equals(startTag)) {
                    int numCharsToSearch = Math.min(htmlString.length(),
                            numChars);
                    String firstNChars = htmlString.substring(0,
                            numCharsToSearch).toLowerCase();
                    int indexOfHtmlTag = firstNChars.indexOf(startTag);
                    if (indexOfHtmlTag != -1) {
                        htmlString = htmlString.substring(indexOfHtmlTag);
                    }
                }
            }
        }
        return htmlString;
    }

    /**
     * Strip any characters after the last "</html>"
     * <p/>
     * Would it be faster to reverse the string, then strip everything before ">lmth/<", then reverse it again?
     *
     * @param htmlString String containing all HTML page including tags.
     * @param numChars   Don't search more than numChars when looking for </html>
     * @return String containing the new html
     */
    public static final String getStripAfterHTML(String htmlString, int numChars) {
        if (htmlString != null) {
            String endTag = "</html>";
            if (htmlString.length() > endTag.length()) {
                if (!htmlString.substring(
                                htmlString.length() - endTag.length() - 1)
                        .toLowerCase().equals(endTag)) {
                    int searchStartPos = Math.max(0, htmlString.length()
                            - numChars);
                    String lastNChars = htmlString.substring(searchStartPos)
                            .toLowerCase();
                    int indexOfHtmlTag = lastNChars.lastIndexOf(endTag);
                    if (indexOfHtmlTag != -1) {
                        htmlString = htmlString.substring(0, searchStartPos
                                + indexOfHtmlTag + endTag.length());
                    }
                }
            }
        }
        return htmlString;
    }

    /**
     * Given a BIS that we believe represents an HTML document, read the first n characters, try to find the character
     * encoding
     *
     * @param bis       Input stream to read from (must be buffered for reset reasons).
     * @param readLimit how many bytes to sniff
     * @return
     * @throws java.io.IOException
     */
    public static final String getCharEncodingFromBufferedInputStream(BufferedInputStream bis, int readLimit)
            throws IOException {
        bis.mark(readLimit);
        StringBuilder builder = new StringBuilder();
        StringBuilderUtil.readStreamIntoBuilder(builder, bis, readLimit);
        bis.reset();
        String encoding = getEncodingFromXMLSnippit(builder, readLimit);
        return encoding;
    }


    /**
     * Given a buffer of characters that look like the start of an xml document, scan it for the character encoding.
     * <p/>
     * <meta HTTP-EQUIV="Content-Type" CONTENT="text/html;CHARSET=iso-8859-1"> <meta http-equiv="Content-Type"
     * content="text/html; charset=euc-kr">
     *
     * @param b
     * @param maxLength
     * @return encoding or UTF-8 if not found
     */
    public static final String getEncodingFromXMLSnippit(StringBuilder b, int maxLength) {
        TagFinder tf = new TagFinder();
        tf.set(b.toString(), maxLength);
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("meta")) {
                if (tf.findAttribute("http-equiv")) {
                    String ct = tf.getAttributeValue();
                    if (!StringUtil.nullOrEmptyString(ct)) {
                        if (ct.equalsIgnoreCase("Content-Type")) {
                            if (tf.findAttribute("content")) {
                                String ctVal = tf.getAttributeValue();
                                JVS map = new JVS();
                                try {
                                    CommandArgs.parseArgs(ctVal, true, true, map);
                                    String r = map.getString("charset");
                                    if (!StringUtil.nullOrEmptyString(r)) {
                                        return r;
                                    }
                                } catch (ParseException e) {

                                } catch (PropaccessError propaccessError) {

                                }
                                // processing should happen here.

                            }
                        }
                    }
                }
                break;
            }
        }
        return "UTF-8";
    }


}
