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
package com.hitorro.util.html;

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;

/**
 * template use File | Settings | File Templates.
 */
public class HTMLEncoder {
    // HTML entity translations
    // please keep them in order (java String comparison order) so a binary search will work properly
    private static EntityTranslation[] s_entities = {
            new EntityTranslation("AElig", 198),
            new EntityTranslation("Aacute", 193),
            new EntityTranslation("Acirc", 194),
            new EntityTranslation("Agrave", 192),
            new EntityTranslation("Aring", 197),
            new EntityTranslation("Atilde", 195),
            new EntityTranslation("Auml", 196),
            new EntityTranslation("Ccedil", 199),
            new EntityTranslation("ETH", 208),
            new EntityTranslation("Eacute", 201),
            new EntityTranslation("Ecirc", 202),
            new EntityTranslation("Egrave", 200),
            new EntityTranslation("Euml", 203),
            new EntityTranslation("Iacute", 205),
            new EntityTranslation("Icirc", 206),
            new EntityTranslation("Igrave", 204),
            new EntityTranslation("Iuml", 207),
            new EntityTranslation("Ntilde", 209),
            new EntityTranslation("Oacute", 211),
            new EntityTranslation("Ocirc", 212),
            new EntityTranslation("Ograve", 210),
            new EntityTranslation("Oslash", 216),
            new EntityTranslation("Otilde", 213),
            new EntityTranslation("Ouml", 214),
            new EntityTranslation("THORN", 222),
            new EntityTranslation("Uacute", 218),
            new EntityTranslation("Ucirc", 219),
            new EntityTranslation("Ugrave", 217),
            new EntityTranslation("Uuml", 220),
            new EntityTranslation("aacute", 225),
            new EntityTranslation("acirc", 226),
            new EntityTranslation("acute", 180),
            new EntityTranslation("aelig", 230),
            new EntityTranslation("agrave", 224),
            new EntityTranslation("amp", '&'),
            new EntityTranslation("aring", 229),
            new EntityTranslation("atilde", 227),
            new EntityTranslation("auml", 228),
            new EntityTranslation("brkbar", 166),
            new EntityTranslation("brvbar", 166),
            new EntityTranslation("ccedil", 231),
            new EntityTranslation("cedil", 184),
            new EntityTranslation("copy", 169),
            new EntityTranslation("curren", 164),
            new EntityTranslation("deg", 176),
            new EntityTranslation("die", 168),
            new EntityTranslation("divide", 247),
            new EntityTranslation("eacute", 233),
            new EntityTranslation("ecirc", 234),
            new EntityTranslation("egrave", 232),
            new EntityTranslation("ent", 162),
            new EntityTranslation("eth", 240),
            new EntityTranslation("euml", 235),
            new EntityTranslation("frac12", 189),
            new EntityTranslation("frac14", 188),
            new EntityTranslation("frac34", 190),
            new EntityTranslation("gt", '>'),
            new EntityTranslation("hibar", 175),
            new EntityTranslation("iacute", 237),
            new EntityTranslation("icirc", 238),
            new EntityTranslation("iexcl", 161),
            new EntityTranslation("igrave", 236),
            new EntityTranslation("iquest", 191),
            new EntityTranslation("iuml", 239),
            new EntityTranslation("laquo", 171),
            new EntityTranslation("lt", '<'),
            new EntityTranslation("macr", 175),
            new EntityTranslation("mdash", 8212),
            new EntityTranslation("micro", 181),
            new EntityTranslation("middot", 183),
            new EntityTranslation("nbsp", 160),
            new EntityTranslation("not", 172),
            new EntityTranslation("ntilde", 241),
            new EntityTranslation("oacute", 243),
            new EntityTranslation("ocirc", 244),
            new EntityTranslation("ograve", 242),
            new EntityTranslation("ordf", 170),
            new EntityTranslation("ordm", 186),
            new EntityTranslation("oslash", 248),
            new EntityTranslation("otilde", 245),
            new EntityTranslation("ouml", 246),
            new EntityTranslation("para", 182),
            new EntityTranslation("plusmn", 177),
            new EntityTranslation("pound", 163),
            new EntityTranslation("quot", '"'),
            new EntityTranslation("raquo", 187),
            new EntityTranslation("reg", 174),
            new EntityTranslation("rsquo", '\''), // microsoft addition
            new EntityTranslation("sect", 167),
            new EntityTranslation("shy", 173),
            new EntityTranslation("sup1", 185),
            new EntityTranslation("sup2", 178),
            new EntityTranslation("sup3", 179),
            new EntityTranslation("szlig", 223),
            new EntityTranslation("thorn", 254),
            new EntityTranslation("times", 215),
            new EntityTranslation("uacute", 250),
            new EntityTranslation("ucirc", 251),
            new EntityTranslation("ugrave", 249),
            new EntityTranslation("uml", 168),
            new EntityTranslation("uuml", 252),
            new EntityTranslation("yacute", 253),
            new EntityTranslation("yen", 165),
            new EntityTranslation("yuml", 255),
    };

    /**
     * Decode a string that was html-encoded.
     *
     * @param ins String with html encoding (&amp; and the like)
     * @return the decoded string, or null if the input was null
     */
    public static final String decodeHtml(String ins) {
        if (ins == null) {
            return null;
        }
        // check if there is any need to decode
        int curIndex = ins.indexOf('&');
        if (curIndex < 0) {
            // no work needed
            return ins;
        }

        // we'll have to rebuild the string
        StringBuilder work = new StringBuilder(ins);
        curIndex = 0;
        while (curIndex >= 0) {
            curIndex = work.indexOf("&", curIndex);
            if (curIndex >= 0) {
                int semiIndex = work.indexOf(";", curIndex);
                // it is possible that the input text is actually improperly coded
                // so if the &; are too close, or too far apart, or unrecognized, let the semicolon through and proceed
                if (semiIndex > curIndex + 2 && semiIndex < curIndex + 8) {
                    String token = work.substring(curIndex + 1, semiIndex);
                    int replace = 0;
                    if (token.charAt(0) == '#') {
                        if (token.charAt(1) == 'x') {
                            // hex encoding
                            replace = Integer.parseInt(token.substring(2), 16);
                        } else {
                            // decimal encoding
                            String s = token.substring(1);
                            if (Constants.isNumber(s)) {
                                replace = Integer.parseInt(s);
                            } else {
                                replace = 0;
                            }
                        }
                    } else {
                        int lower = -1;
                        int upper = s_entities.length;
                        int testIndex = (lower + upper) / 2;
                        while (testIndex > lower && testIndex < upper) {
                            int test = s_entities[testIndex].htmlCode.compareTo(token);
                            if (test == 0) {
                                // found the test
                                replace = s_entities[testIndex].utf8;
                                break;
                            } else if (test < 0) {
                                // s_entities[testIndex] is smaller than token
                                lower = testIndex;
                            } else {
                                // s_entities[testIndex] is greater than token
                                upper = testIndex;
                            }
                            testIndex = (lower + upper) / 2;
                        }

                        /*
                        // linear scan through translation table for now
                        // todo chris - make this a binary search & support more entities
                        int len = s_entities.length;
                        for (int ii = 0; ii < len; ii++) {
                            if (s_entities[ii].htmlCode.equals(token)) {
                                replace = s_entities[ii].utf8;
                                break;
                            }
                        }
                        */

                        if (replace == 0) {
                            Log.util.error("Unrecognized html decode token %s", token);
                            // by leaving replace at 0 we won't do a replacement and just move past the &
                        }
                    }

                    if (replace != 0) {
                        work.delete(curIndex, semiIndex + 1);
                        work.insert(curIndex, (char) replace);
                    }
                }
                // advance current index past the character we just put in
                curIndex++;
            }
        }

        return work.toString();
    }

    private static class EntityTranslation {
        public String htmlCode;
        public int utf8;

        public EntityTranslation(String hc, int u8) {
            htmlCode = hc;
            utf8 = u8;
        }
    }
}
