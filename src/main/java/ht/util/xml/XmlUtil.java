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
package ht.util.xml;


public class XmlUtil {

    /*

     */
    public static final String escapeXML(String toEncode) {
        StringBuilder buff = new StringBuilder();

        return escapeXML(toEncode, buff).toString();
    }

    /**
     * Possibly not the most optimial routine but looks for existence of reserved xml characters and escapes them using
     * the predefined entity references.
     *
     * @param toEncode
     * @param buffer
     * @return buffer with escaped characters in.
     */


    public static final StringBuilder escapeXML(String toEncode, StringBuilder buffer) {
        int size = toEncode.length();
        for (int i = 0; i < size; i++) {
            char c = toEncode.charAt(i);
            switch (c) {
                case '&':
                    buffer.append("&amp;");
                    break;
                case '<':
                    buffer.append("&lt;");
                    break;
                case '>':
                    buffer.append("&gt;");
                    break;
                case '\'':
                    buffer.append("&apos;");
                    break;
                case '"':
                    buffer.append("&quote;");
                    break;
                default:
                    buffer.append(c);
            }
        }
        return buffer;
    }
}
