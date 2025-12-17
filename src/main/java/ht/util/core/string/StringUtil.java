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

package ht.util.core.string;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ht.util.core.ArrayUtil;
import ht.util.core.Constants;
import ht.util.html.HTMLEncoder;
import ht.util.io.FileUtil;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public final class StringUtil {
// ------------------------------ FIELDS ------------------------------

    public static final String UTF8Encoding = "UTF-8";

    public static final int BaseZeroValue = (int) '0';

// -------------------------- STATIC METHODS --------------------------


    public static String after(String s, char c) {
        if (s == null) {
            return null;
        }
        int index = s.indexOf(c);
        if (index == -1) {
            return s;
        }
        if (index + 1 == s.length()) {
            return null;
        }
        return s.substring(index + 1);
    }

    public static String collapseWhitespace(String trim) {
        if (trim.isEmpty()) {
            return trim;
        }

        StringBuilder b = new StringBuilder(trim.length());
        for (int i = 0; i < trim.length(); ++i) {
            char ch = trim.charAt(i);
            if (Character.isWhitespace(ch)) {
                b.append(' ');
                int j = i + 1;
                while (j < trim.length() && Character.isWhitespace(trim.charAt(j))) {
                    ++j;
                }
                i = j - 1;
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }


    public static int mergeHashcodes(Object... args) {
        if (args.length == 0) {
            return 0;
        }
        if (args.length == 1) {
            return args[0].hashCode();
        }
        int hash = 0;
        if (args[0] != null) {
            hash = args[0].hashCode();
        }
        for (int i = 1; i < args.length; i++) {
            if (args[i] != null) {
                hash ^= args[i].hashCode();
            }
        }
        return hash;

    }

    public static int size(String... elems) {
        int size = 0;
        for (String e : elems) {
            if (e == null) {
                continue;
            }
            size += (e.length() * 2);
        }
        return size;
    }

    public static void normalizeText(String s, StringBuilder sb) {
        int l = s.length();
        for (int i = 0; i < l; i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(Character.toLowerCase(c));
            } else if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
    }

    public static String[] increaseStringArray(String in[], int length) {
        String tmp[] = new String[length * 2];
        System.arraycopy(in, 0, tmp, 0, length);
        return tmp;
    }

    public static final ArrayNode getCallstackAsJsonArray(Throwable t) {
        final ArrayNode an = JsonNodeFactory.instance.arrayNode();
        t.printStackTrace(new PrintWriter(new Writer() {

            @Override
            public void write(char[] cbuf, int off, int len) {
                an.add(new String(cbuf, off, len));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        }));
        return an;
    }

    /**
     * @param v1
     * @param len1
     * @param v2
     * @param len2
     * @return
     */
    public static final int compareTo(char v1[], int len1, char v2[], int len2) {
        int i = 0;
        int j = 0;
        int n = len1;
        if (i == j) {
            int k = i;
            int lim = n + i;
            while (k < lim) {
                char c1 = v1[k];
                char c2 = v2[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
        } else {
            while (n-- != 0) {
                char c1 = v1[i++];
                char c2 = v2[j++];
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
        }
        return len1 - len2;
    }

    public static final int startsWith(char v1[], int len1, char v2[], int len2) {
        int i = 0;
        if (len1 <= len2) {
            int k = i;
            int lim = len1 + i;
            while (k < lim) {
                char c1 = v1[k];
                char c2 = v2[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return 0;
        }

        return len1 - len2;
    }


    /**
     * scanning from right to left return the index of the first character that is not the character provided
     *
     * @param s
     * @param c
     * @return index of the first character from right to left that is not c or -1 if never met.
     */
    public static final int lastNonChar(String s, char c) {
        for (int i = s.length() - 1; i > 0; i--) {
            char ch = s.charAt(i);
            if (ch != c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * take an array of strings and prepend the provided string.
     *
     * @param arr
     * @param prependS
     * @return
     */
    public static String[] preapendArray(String[] arr, String prependS) {
        if (arr == null) {
            return null;
        }
        String result[] = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            if (s == null) {
                s = Constants.EmptyString;
            }
            result[i] = StringUtil.strcat(prependS, s);
        }
        return result;
    }

    public static final String rightTrim(String s) {
        int index = lastNonChar(s, ' ');
        if (index == -1 || index >= s.length()) {
            return s;
        }
        return s.substring(0, index + 1);
    }

    public static final String leftTrim(String s) {
        int index = firstNonChar(s, ' ');
        if (index <= 0) {
            return s;
        }
        return s.substring(index - 1, index);
    }

    /**
     * scanning from right to left return the index of the first character that is not the character provided
     *
     * @param s
     * @param c
     * @return index of the first character from right to left that is not c or -1 if never met.
     */
    public static final int firstNonChar(String s, char c) {
        int lastIndex = s.length() - 1;
        for (int i = 0; i < lastIndex; i++) {
            char ch = s.charAt(i);
            if (ch != c) {
                return i;
            }
        }
        return -1;
    }


    public static String[] combineStringArrays(String arr[], String arr2[]) {
        int size = ArrayUtil.sizeOfArrayIgnoringNull(arr);
        size += ArrayUtil.sizeOfArrayIgnoringNull(arr2);
        if (size == 0) {
            return null;
        }
        String target[] = new String[size];
        int ind = 0;
        ind += ArrayUtil.copy(target, ind, arr);
        ind = ArrayUtil.copy(target, ind, arr2);

        return target;
    }

    public static String[] combineStringArrays(String arr[][]) {
        int size = 0;
        for (int i = 0; i < arr.length; i++) {
            size += ArrayUtil.sizeOfArrayIgnoringNull(arr[i]);
        }

        if (size == 0) {
            return null;
        }
        String target[] = new String[size];
        int ind = 0;
        for (int i = 0; i < arr.length; i++) {
            ind += ArrayUtil.copy(target, ind, arr[i]);
        }
        return target;
    }


    /**
     * Get the nth incarnation of a specific substring in the test string
     *
     * @param test
     * @param lookFor
     * @param n
     * @return
     */
    public static final int nthIndex(String test, String lookFor, int n) {
        int index = 0;
        for (int i = 0; i < n; i++) {
            if (i != 0) {
                index++;
            }
            index = test.indexOf(lookFor, index);
            if (index == -1) {
                return -1;
            }
        }
        return index;
    }

    /**
     * Add all the elements of the array to the listFiles
     *
     * @param arr
     * @param list
     */
    public static final void addAll(String arr[], List<String> list) {
        for (String s : arr) {
            list.add(s);
        }
    }

    public static final String tailFollowing(String tok, String s) {
        int ind = s.lastIndexOf(tok);
        if (ind == -1) {
            return null;
        }
        return s.substring(ind + tok.length());
    }

    /**
     * scan backwards through a string looking for the nth occurence of character c, if not found return -1
     *
     * @param s
     * @param c
     * @param nth
     * @return
     */
    public static final int getNthIndexFromEnd(String s, char c, int nth) {
        int i = s.length() - 1;
        int count = 0;
        for (; i >= 0; i--) {
            if (s.charAt(i) == c) {
                count++;
                if (count == nth) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static final String getPrePackedString(String packing, Object o) {
        String s = o.toString();
        int l = s.length();
        int pL = packing.length();
        if (l < pL) {
            return StringUtil.strcat(packing.substring(0, pL - l), s);
        }
        return s;
    }

    /**
     * strip out all non numeric parts of a string and make a number from it.
     *
     * @param text
     * @return
     */
    public static final String getTextOnly(String text) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            if (Character.isLetter(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * Get the occurences of a token in s
     *
     * @param s
     * @param t
     * @return
     */
    public static final int count(String s, String t) {
        int count = 0;
        int ind = s.indexOf(t);
        int width = s.length();
        while (ind != -1 && ind < width) {
            count++;
            ind = s.indexOf(t, ind + 1);
        }
        return count;
    }

    public static final String getNthPart(String s, String sep, int i) {
        String parts[] = StringUtil.tokenizeFromSingleChar(s, sep);
        if (i > parts.length - 1) {
            return null;
        }

        return parts[i];
    }

    public static final String toLowerCaseIfNotNull(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return null;
        }
        return s.toLowerCase();
    }

    /**
     * aquire the last part of a canonical string:
     * <p/>
     * a.b.c
     * <p/>
     * returns
     * <p/>
     * c
     *
     * @param s
     * @return
     */
    public static final String getLastCanonicalPart(String s) {
        String parts[] = StringUtil.tokenizeFromSingleChar(s, ".", true);
        if (parts == null) {
            return null;
        }
        return parts[parts.length - 1];
    }

    /**
     * look to see if any of the test strings are contained in the source string
     *
     * @param source
     * @param l
     * @param ignoreCase
     * @return
     */
    public static final int containsAny(String source, String l[], boolean ignoreCase) {
        int i = 0;
        for (String t : l) {
            if (ignoreCase) {
                t = t.toLowerCase();
                source = source.toLowerCase();
            }
            if (source.indexOf(t) != -1) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * does the string contain only alpha characters and full stops: aaa.bbb.cc
     *
     * @param s
     * @return
     */
    public static final boolean isAlphaPunkt(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                continue;
            } else if (c == '.') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * look for any indicator of null, including a string null
     *
     * @param s
     * @return
     */
    public static final boolean nullOrEmptyStringOrSaysNull(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return true;
        }
        return s.equalsIgnoreCase("null");
    }

    /**
     * reverse a canonical path:
     * <p/>
     * a.b.c becomes: c.b.a
     *
     * @param tok
     * @return
     */
    public static final String reverseCanon(String tok) {
        String parts[] = StringUtil.tokenizeFromSingleChar(tok, ".");
        StringBuilder sb = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            if (sb.length() != 0) {
                sb.append(".");
            }
            sb.append(parts[i]);
        }

        String result = sb.toString();
        return result;
    }

    /**
     * remove anything from a variable name that is bad joo joo for such things as HDFS
     */
    public static final String cleansForNonAlphaDigitCharacters(String variable) {
        StringBuilder sb = new StringBuilder();
        int size = variable.length();
        for (int i = 0; i < size; i++) {
            char c = variable.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * strip out all non numeric parts of a string and make a number from it.
     *
     * @param text
     * @return
     */
    public static final long getLongNumberFromText(String text) {
        long numb = 0;
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            if (Character.isDigit(c)) {
                int v = c - BaseZeroValue;
                numb = numb * 10 + v;
            }
        }
        return numb;
    }

    /**
     * strip out all non numeric parts of a string and make a number from it.
     *
     * @param text
     * @return
     */
    public static final int getIntegerNumberFromText(String text) {
        int numb = 0;
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            if (Character.isDigit(c)) {
                int v = c - BaseZeroValue;
                numb = numb * 10 + v;
            }
        }
        return numb;
    }

    /**
     * strip out all non numeric parts of a string and make a number from it.
     *
     * @param text
     * @return
     */
    public static final int getIntNumberFromText(String text) {
        int numb = 0;
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            if (Character.isDigit(c)) {
                int v = c - BaseZeroValue;
                numb = numb * 10 + v;
            }
        }
        return numb;
    }


    /**
     * Capitalizes the first letter of words contained in the String. If firstOnly is true, then only the first word is
     * capitalized. Words are separated by one space.
     *
     * @param words     a single word or phrase
     * @param firstOnly true if only the first word in the string is capitalized, else all words will be capitalized
     * @return string with capitalized first letters
     */
    public static final String capitalize(String words, boolean firstOnly) {
        String capitalizedWords = words;

        if (firstOnly) {
            capitalizedWords = capitalizeFirstLetter(words);
        } else {
            if (words != null) {
                StringBuffer capWords = new StringBuffer();
                String[] wordArray = words.split(Constants.SPACE);
                for (int i = 0; i < wordArray.length; i++) {
                    capWords.append(capitalizeFirstLetter(wordArray[i]));
                    if (i < wordArray.length - 1) {
                        capWords.append(Constants.SPACE);
                    }
                }
                capitalizedWords = capWords.toString();
            }
        }

        return capitalizedWords;
    }

    /**
     * Capitalizes the first letter of a String
     *
     * @param string
     * @return
     */
    public static final String capitalizeFirstLetter(String string) {
        String formattedString = string;

        if (string != null && !string.equals("")) {
            StringBuffer capString = new StringBuffer();

            char firstLetter = string.charAt(0);
            capString.append(Character.toUpperCase(firstLetter));
            if (string.length() > 1) {
                capString.append(string.substring(1));
            }
            formattedString = capString.toString();
        }

        return formattedString;
    }

    /**
     * Read a file into a String.
     *
     * @param f
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */

    public static String readFileIntoString(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        readFileIntoBuilder(sb, f);
        return sb.toString();
    }

    /**
     * Read a file into a StringBuilder.
     *
     * @param builder We will append all content to the end of this StringBuilder.
     * @param file
     * @throws java.io.FileNotFoundException
     * @throws IOException
     */
    public static void readFileIntoBuilder(StringBuilder builder, File file)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                first = false;
            } else {
                builder.append(Constants.NewLineChar);
            }
            builder.append(line);
        }
    }

    /**
     * Read an CInputStream into a StringBuilder. The input stream is assumed to contained characters.
     *
     * @param builder We will append all content to the end of this StringBuilder.
     * @param in      The input stream
     * @throws java.io.FileNotFoundException
     * @throws IOException
     */
    public static void readStreamIntoBuilder(StringBuilder builder, InputStream in)
            throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
    }

    /**
     * Crude reader that assumes that the IS is nothing other than a series of bytes. We use this for such things as
     * finding the character encoding of xml in an input stream.
     *
     * @param builder
     * @param in
     * @param charsToRead
     * @throws IOException
     */
    public static void readStreamIntoBuilder(StringBuilder builder, InputStream in, int charsToRead)
            throws IOException {

        while (charsToRead > 0) {
            int i = in.read();
            if (i == -1) {
                return;
            }
            charsToRead--;

            builder.append((char) i);
        }
    }

    /**
     * Read an CInputStream into a StringBuilder. The input stream is assumed to contain characters.
     *
     * @param in The input stream
     * @return The StringBuilder containing the contents of the stream
     */
    public static StringBuilder readStreamIntoBuilder(InputStream in)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        readStreamIntoBuilder(builder, in);
        return builder;
    }

    public static void writeStringBuilderToFile(StringBuilder sb, File f) {
        PrintWriter pw = FileUtil.getBufferedPrintWriterFromFile(f);
        pw.write(sb.toString());
        pw.flush();
        pw.close();
    }

    public static void writeStringToFile(String sb, File f) {
        PrintWriter pw = FileUtil.getBufferedPrintWriterFromFile(f);
        pw.write(sb);
        pw.flush();
        pw.close();
    }

    /**
     * Test a string for containing only the test char
     *
     * @param test string
     * @return true only contains test char.
     */
    public static final boolean containsAllSameChars(final String test,
                                                     final char testChar) {
        int testSize = test.length();
        for (int i = 0; i < testSize; i++) {
            if (test.charAt(i) != testChar) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a string of length size with the padding character.
     *
     * @param padChar
     * @param size
     * @return
     */
    public static final String createPadding(char padChar, int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(padChar);
        }
        return builder.toString();
    }

    /**
     * Limit a string to a maximum length.
     *
     * @param value  - the string to be limited
     * @param maxLen - the maximum length allowed
     * @return the string if it is null or is <= the maxlen, else the substring up to maxlen
     */
    public static final String cutToLength(String value, int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        if (maxLen <= 0) {
            return new String();
        }

        return value.substring(0, maxLen);
    }

    /**
     * Decode a string that was encoded for a url or post.
     *
     * @param ins String with html encoding (&amp; and the like)
     * @return the decoded string, or null if the input was null
     */
    public static final String decodeUrl(String ins) {
        if (ins == null) {
            return null;
        }
        try {
            return URLDecoder.decode(ins, UTF8Encoding);
        } catch (UnsupportedEncodingException uee) {
            // this shouldn't ever happen
            return ins;
        }
    }

    /**
     * Test a string does not contain the test char
     *
     * @param test string
     * @return true only contains test char.
     */
    public static final boolean doesNotContainChar(final String test,
                                                   final char testChar) {
        int testSize = test.length();
        for (int i = 0; i < testSize; i++) {
            if (test.charAt(i) == testChar) {
                return false;
            }
        }
        return true;
    }

    /**
     * Encode a string for html content.
     *
     * @param ins String to be encoded
     * @return the encoded String or null if the input was null.
     */
    public static final String encodeForXML(String ins) {
        if (ins == null) {
            return null;
        }
        StringBuilder work = new StringBuilder(ins);
        int len = work.length();
        for (int ii = 0; ii < len; ii++) {
            char cc = work.charAt(ii);
            String replace = null;
            int rlen = 0;
            if (cc == '&') {
                replace = "&amp;";
                rlen = 5;
            } else if (cc == '<') {
                replace = "&lt;";
                rlen = 4;
            } else if (cc == '>') {
                replace = "&gt;";
                rlen = 4;
            } else if (cc == '"') {
                replace = "&quot;";
                rlen = 6;
            } else if (cc > 255) {
                replace = StringUtil.strcat("&#", Integer.toString(cc), ";");
                rlen = replace.length();
            }
            if (replace != null) {
                work.replace(ii, ii + 1, replace);
                // adjust our indices
                len += (rlen - 1);
                ii += (rlen - 1);
            }
        }

        return work.toString();
    }

    /**
     * Encode a string for a URL or post.
     *
     * @param ins String to be encoded
     * @return the encoded String or null if the input was null.
     */
    public static final String encodeUrl(String ins) {
        if (ins == null) {
            return null;
        }
        try {
            return URLEncoder.encode(ins, UTF8Encoding);
        } catch (UnsupportedEncodingException uee) {
            // this shouldn't ever happen
            return ins;
        }
    }

    public static final boolean endsWithIgnoringCase(String string, String test) {
        return endsWithIgnoringCase(string, test, true);
    }

    /**
     * Determine if a string ends with another string ignoring case.
     *
     * @param string
     * @param test
     * @return
     */
    public static final boolean endsWithIgnoringCase(String string, String test, boolean ignoreCase) {
        return endsWithIgnoringCase(string, test, ignoreCase, string.length());
    }

    public static final boolean endsWithIgnoringCase(String string, String test, boolean ignoreCase, int lengthS) {
        if (!ignoreCase) {
            return string.endsWith(test);
        }

        int lengthTest = test.length();
        if (lengthS < lengthTest) {
            return false;
        }
        int startS = lengthS - lengthTest;
        for (int i = 0; i < lengthTest; i++) {
            char sC = string.charAt(startS + i);
            char tC = test.charAt(i);
            if (Character.toLowerCase(sC) != Character.toLowerCase(tC)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lots of examples of where logic will either consider or ignore case of a string compare. Simplifies the logic on
     * typical callers.
     *
     * @param a
     * @param b
     * @param ignoreCase
     * @return true if the same
     */
    public static final boolean equals(String a, String b, boolean ignoreCase) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null && b != null) {
            return false;
        }
        if (a != null && b == null) {
            return false;
        }

        if (ignoreCase) {
            return a.equalsIgnoreCase(b);
        }
        return a.equals(b);
    }

    /**
     * Return the first part of a string containing a seperator token.
     *
     * @return null if no token found, else the first part of the line is returned
     */
    public static final String firstPart(final String line,
                                         final char token) {
        if (line == null) {
            return null;
        }
        int index = line.indexOf(token);
        if (index == -1) {
            return null;
        }
        String result[] = new String[1];
        return line.substring(0, index);
    }

    /**
     * Get a string padding of n length
     *
     * @param length
     * @return
     */

    public static final String getNpadding(final int length, final char c) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buff.append(c);
        }

        return buff.toString();
    }

    /**
     * Get the default value if the provided value is null or empty.
     *
     * @param val
     * @param defaultVal
     * @return
     */
    public static final String getValueDefault(String val, String defaultVal) {
        if (StringUtil.nullOrEmptyString(val)) {
            return defaultVal;
        }
        return val;
    }

    public static final long hashStringCaseFree(String buffer) {
        return hashStringCaseFree(buffer, 0, buffer.length());
    }

    /**
     * Takes normal hash of string, but ignores case. Returns a hash code for this string. The hash code for a
     * <code>String</code> object is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using <code>int</code> arithmetic, where <code>s[i]</code> is the <i>i</i>th character of the string,
     * <code>n</code> is the length of the string, and <code>^</code> indicates exponentiation. (The hash value of the
     * empty string is zero.)
     *
     * @return a hash code value for this object.
     */

    public static final long hashStringCaseFree(String buffer, int start, int length) {
        int h = 0;

        int len = start + length;
        char c;
        for (int i = start; i < len; i++) {
            c = Character.toLowerCase(buffer.charAt(i));

            h = 31 * h + c;
        }
        return h;
    }

    /**
     * Assign a default value if the value provided is null
     *
     * @param val
     * @param defaultVal
     * @return
     */
    public static final String ifNotNullDefault(String val, String defaultVal) {
        if (StringUtil.nullOrEmptyOrBlankString(val)) {
            return defaultVal;
        }
        return val;
    }

    /**
     * Test to see if the provided test string is null or empty if so then return a default value, else return the test
     * value.
     * <p/>
     * Usefull way to ensure that a value is always provided to a function or variable.
     *
     * @param test
     * @param defaultValue
     * @return test if not null or defaultValue if test null or empty.
     */
    public static final String ifNullOrEmptyReplace(final String test,
                                                    final String defaultValue) {
        if (nullOrEmptyString(test)) {
            return defaultValue;
        }
        return test;
    }

    /**
     * Scan a 1d array for matching string.
     *
     * @param s
     * @param list
     * @param ignoreCase
     * @return true if found.
     */
    public static final boolean inList(String s, String[] list,
                                       boolean ignoreCase) {
        for (String test : list) {
            if (equals(test, s, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scan a 1d array for matching string.
     *
     * @param s
     * @param list
     * @param ignoreCase
     * @return true if found.
     */
    public static final boolean inList(String s, String[][] list,
                                       int columnNo, boolean ignoreCase) {
        for (int i = 0; i < list.length; i++) {
            if (equals(list[i][columnNo], s, ignoreCase)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Simple non efficient routine for converting a listFiles of objects to an array of Strings
     *
     * @param listOfStrings
     * @return
     */
    public static final String[] listToArray(final List listOfStrings) {
        String[] result = new String[listOfStrings.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = listOfStrings.get(i).toString();
        }
        return result;
    }

    /**
     * Lower case a string if its not null
     *
     * @param s
     * @return
     */
    public static final String lowerCaseIfNotNull(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return s;
        }
        return s.toLowerCase();
    }

    /**
     * Append the key value pairs of a apply into a buffer, using provided seperator tokens
     *
     * @param map
     * @param keyValSep
     * @param lineSeperator
     * @return
     */
    public static final StringBuilder mapToBuffer(final Map map,
                                                  String keyValSep, final String lineSeperator) {
        StringBuilder buffer = new StringBuilder();
        Set s = map.keySet();
        Iterator keys = s.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = (String) map.get(key);
            buffer.append(key);
            buffer.append(keyValSep);
            buffer.append(value);
            buffer.append(lineSeperator);
        }
        return buffer;
    }

    public static final String[] mergeStringArrays(String[] array1,
                                                   String[] array2) {
        String[] array = new String[array1.length + array2.length];
        int idx = 0;
        for (String str : array1) {
            array[idx++] = str;
        }
        for (String str : array2) {
            array[idx++] = str;
        }
        return array;
    }

    /**
     * Merge the elements of a listFiles into one string.
     *
     * @param list
     * @param joinToken
     * @return merged string.
     */
    public static final String mergeWithJoinToken(final List list,
                                                  final String joinToken) {
        StringBuilder buff = new StringBuilder();
        StringBuilderUtil.mergeWithJoinToken(buff, list.toArray(), joinToken);
        return buff.toString();
    }

    public static final String mergeWithJoinToken(final Collection coll,
                                                  final String joinToken) {
        StringBuilder sb = new StringBuilder();
        boolean needJoinToken = false;
        for (Object item : coll) {
            if (needJoinToken) {
                sb.append(joinToken);
            }
            needJoinToken = true;
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * Merge using a join token all of the elements of the provided array.
     *
     * @param array
     * @param joinToken
     * @return merged string
     */
    public static final String mergeWithJoinToken(final Object array[],
                                                  final String joinToken) {
        StringBuilder buff = new StringBuilder();
        StringBuilderUtil.mergeWithJoinToken(buff, array, joinToken);
        return buff.toString();
    }

    /**
     * Given an array of objects, apply them into a stingle string using a join token. Do this only for the array
     * elements withing the range provided.
     *
     * @param array
     * @param joinToken token to use to insert between each
     * @param start     position to include in apply.
     * @param length    how many positions to include in apply.
     * @return String of merged tokens.
     */
    public static final String mergeWithJoinToken(final Object array[],
                                                  final String joinToken, int start, int length) {
        StringBuilder buff = new StringBuilder();
        StringBuilderUtil.mergeWithJoinToken(buff, array, joinToken, start, length);
        return buff.toString();
    }

    /**
     * Add the prefix to each element of the listFiles and return the merged listFiles
     *
     * @param list
     * @param prefix
     * @param joinToken
     * @return merged string.
     */
    public static final String mergeWithPrefixAndJoinToken(
            final List<String> list, final String prefix, final String joinToken) {
        StringBuilder buff = new StringBuilder();
        ArrayList<String> newlist = new ArrayList<String>();
        for (String str : list) {
            newlist.add(prefix + str);
        }
        StringBuilderUtil.mergeWithJoinToken(buff, newlist.toArray(), joinToken);
        return buff.toString();
    }

    /**
     * Add the prefix to each element of the listFiles and return the merged listFiles
     *
     * @param list
     * @param prefix
     * @param joinToken
     * @return merged string.
     */
    public static final String mergeWithPrefixAndJoinToken(
            final String list[], final String prefix, final String joinToken) {
        StringBuilder buff = new StringBuilder();
        ArrayList<String> newlist = new ArrayList<String>();
        for (String str : list) {
            newlist.add(prefix + str);
        }
        StringBuilderUtil.mergeWithJoinToken(buff, newlist.toArray(), joinToken);
        return buff.toString();
    }

    /**
     * perform an equality check if both strings are not null. If both are null, this function still returns false (not
     * equal).
     *
     * @param a
     * @param b
     * @param ignoreCase
     * @return
     */
    public static final boolean notNullEquals(String a, String b, boolean ignoreCase) {
        if (!StringUtil.nullOrEmptyString(a) && !StringUtil.nullOrEmptyString(b)) {
            if (ignoreCase) {
                return a.equalsIgnoreCase(b);
            }
            return a.equals(b);
        }
        return false;
    }

    /**
     * Test that the references string is not null and not empty (that is 0 chars in length) or contains only
     * whiteSpace
     *
     * @param test string
     * @return true if null or empty string
     */
    public static final boolean nullOrEmptyOrBlankString(final String test) {
        return test == null || test.length() == 0
                || containsAllSameChars(test, ' ');
    }

    /**
     * Test that the references string is not null and not empty (that is 0 chars in length)
     *
     * @param test string
     * @return true if null or empty string
     */
    public static final boolean nullOrEmptyString(final String test) {
        return test == null || test.length() == 0;
    }

    public static final boolean nullOrEmptyString(final String... tests) {
        for (String t : tests) {
            if (nullOrEmptyString(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test that the references string is not null and not empty (that is 0 chars in length)
     *
     * @param test string
     * @return true if null or empty string
     */
    public static final boolean nullOrEmptyStringBuilder(
            final StringBuilder test) {
        return test == null || test.length() == 0;
    }

    /**
     * Convert an array of objects into an array of strings using their toString method. If the object is null then use
     * the default value provided.
     *
     * @param vals
     * @param defaultValue
     * @return
     */
    public static String[] objectArrayToString(Object vals[],
                                               String defaultValue) {
        String[] result = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            Object o = vals[i];
            if (o == null) {
                result[i] = defaultValue;
            } else {
                result[i] = o.toString();
            }
        }
        return result;
    }

    /**
     * Simple non efficient routine for converting a listFiles of objects to an array of Strings
     *
     * @return String array representation of the array of objects.
     */
    public static final String[] objectArrayToStringArray(Object listOfObjects[]) {
        String[] result = new String[listOfObjects.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = listOfObjects[i].toString();
        }
        return result;
    }

    /**
     * return an array of strings that are packed to the length specified.
     *
     * @param vals
     * @param length
     * @return
     */
    public static String[] pack(Object vals[], int length[], char padding) {
        StringBuilder builder = new StringBuilder();
        String[] result = new String[vals.length];
        int min = Math.min(vals.length, length.length);
        for (int i = 0; i < min; i++) {
            builder.setLength(0);
            padToLength(vals[i], padding, length[i], builder);
            result[i] = builder.toString();
        }
        return result;
    }

    /**
     * append to the buffer the padMe string of size size.
     *
     * @param padMe
     * @param padding
     * @param size
     * @param builder
     */
    public static final void pad(Object padMe, char padding, int size,
                                 StringBuilder builder) {
        builder.append(padMe);
        for (int i = 0; i < size; i++) {
            builder.append(padding);
        }
    }

    public static String padToLength(char padding, int size) {
        if (size <= 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        padToLength("", padding, size, b);
        return b.toString();
    }

    /**
     * Pad a string with the padding char to the desired size.
     *
     * @param padMe
     * @param padding
     * @param size
     * @return
     */
    public static final String padToLength(Object padMe, char padding, int size) {
        StringBuilder builder = new StringBuilder();
        padToLength(padMe, padding, size, builder);
        return builder.toString();
    }

    /**
     * append to the buffer the padMe string and if space (based upon size) we fill to size using the padding
     * character.
     *
     * @param padMe
     * @param padding
     * @param size
     * @param builder
     */
    public static final void padToLength(Object padMe, char padding, int size,
                                         StringBuilder builder) {
        int sizeIn = builder.length();
        builder.append(padMe);
        int pad = size - (builder.length() - sizeIn);
        // if (pad > 0)
        {
            for (int i = 0; i < pad; i++) {
                builder.append(padding);
            }
        }
    }

    /**
     * append to the buffer the padMe string and if space (based upon size) we fill to size using the padding
     * character.
     *
     * @param padMe
     * @param padding
     * @param size
     * @param builder
     */
    public static final String gePadding(Object padMe, char padding, int size,
                                         StringBuilder builder) {
        builder.setLength(0);
        int sizeIn = builder.length();
        builder.append(padMe);
        int pad = size - (builder.length() - sizeIn);
        // if (pad > 0)
        {
            for (int i = 0; i < pad; i++) {
                builder.append(padding);
            }
        }
        return builder.toString();
    }

    /**
     * prepend to the buffer to size the padding character, then append to the buffer the padMe string.
     *
     * @param padMe
     * @param padding
     * @param size
     * @param builder
     */
    public static final void prependPadToLength(Object padMe, char padding, int size,
                                                StringBuilder builder) {
        String padMeStr = padMe.toString();

        int pad = size - (builder.length() + padMeStr.length());
        {
            for (int i = 0; i < pad; i++) {
                builder.append(padding);
            }
        }

        builder.append(padMe);
    }

    public static final String removeSpecialTags(String buf, String startTag, String endTag, List<String> extractedTags) {
        int stSize = startTag.length();
        int etSize = endTag.length();
        StringBuilder sb = new StringBuilder();
        int prev = 0;
        int index = buf.indexOf(startTag);
        while (index != -1) {
            sb.append(buf, prev, index);
            prev = index + stSize;
            index = buf.indexOf(endTag, prev);
            if (index != -1) {
                String tok = buf.substring(prev, index);
                extractedTags.add(tok);
                sb.append(tok);
                prev = index + etSize;
                index = buf.indexOf(startTag, prev);
            }
        }
        sb.append(buf, prev, buf.length());
        return sb.toString();
    }

    public static String replace(String a, String b, String c) {
        StringBuilder builder = new StringBuilder();
        int index = a.indexOf(b);
        int prev = 0;
        while (index != -1) {
            builder.append(a, prev, index);
            builder.append(c);
            prev = index + b.length();
            index = a.indexOf(b, prev);
        }
        if (a.length() - prev > 0) {
            builder.append(a, prev, a.length());
        }

        return builder.toString();
    }

    /**
     * Look for a character within a slop distance within the data string starting at a
     *
     * @param data         to scan
     * @param startIndex   starting position in data to scan.
     * @param slopDistance is how many characters forward to test
     * @param c            character to test for.
     * @return
     */
    public static int sloppyIndexOf(final String data, final int startIndex,
                                    final int slopDistance, final char c) {
        int dataLength = data.length();
        if (dataLength < startIndex) {
            // our starting index position is greater than the size of the
            // array.
            return -1;
        }

        int end = Math.min(dataLength, startIndex + slopDistance);
        for (int i = startIndex; i < end; i++) {
            if (data.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Split a string in two by provided token
     *
     * @return null if no token found, else 2 element string array divided by the token
     */
    public static final String[] splitByToken(final String line,
                                              final char token) {
        int index = line.indexOf(token);
        if (index == -1) {
            String p[] = new String[1];
            p[0] = line;
            return p;
        }
        String result[] = new String[2];
        result[0] = line.substring(0, index);
        if (index - 1 >= line.length()) {
            result[1] = Constants.EmptyString;
        } else {
            result[1] = line.substring(index + 1);
        }
        return result;
    }


    public static final String[] splitByFirstToken(final String line,
                                                   final String token) {
        if (StringUtil.nullOrEmptyOrBlankString(line)) {
            return null;
        }
        int index = line.indexOf(token);
        if (index == -1) {
            return new String[]{line};
        }
        String result[] = new String[2];
        result[0] = line.substring(0, index);
        if (index - 1 >= line.length()) {
            result[1] = Constants.EmptyString;
        } else {
            result[1] = line.substring(index + token.length());
        }
        return result;
    }


    /**
     * Build a single string representation of a stack of objects in string form
     *
     * @param stack
     * @param seperator
     * @return string form of a stack of objects.
     */
    public static final String stackToString(Stack stack, String seperator) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < stack.size(); i++) {
            builder.append(stack.get(i).toString());
            builder.append(seperator);
        }
        return builder.toString();
    }

    /**
     * Case independent starts with test that does not create any string objects.
     *
     * @param s
     * @param index
     * @param test
     * @return true if same
     */
    public static final boolean startsWithIgnoreCase(String s, int index, String test) {
        if (s.length() - index < test.length()) {
            return false;
        }

        int size = test.length();
        for (int i = 0; i < size; i++) {
            if (Character.toLowerCase(s.charAt(index + i)) != Character.toLowerCase(test.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Perform a case insensative starts with.  This generates an intermediate string object!!
     *
     * @param s
     * @param test
     * @return
     */
    public static final boolean startsWithIgnoreCase(String s, String test) {
        return startsWithIgnoreCase(s, test, true);
    }

    /**
     * Perform a case insensative starts with.  This generates an intermediate string object!!
     *
     * @param s
     * @param test
     * @return
     */
    public static final boolean startsWithIgnoreCase(String s, String test, boolean ignore) {
        if (s.length() < test.length()) {
            return false;
        }
        String start = s.substring(0, test.length());
        if (ignore) {
            return start.equalsIgnoreCase(test);
        }
        return start.equals(test);
    }

    public static final int startsWithIndexIgnoreCase(String s, String test[]) {
        for (int i = 0; i < test.length; i++) {
            if (startsWithIgnoreCase(s, test[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Concatinate a series of objects together using their toString() form. This uses a non synchronized string buffer
     * to performn the contination.
     *
     * @param args
     * @return
     */
    public static final String strcat(Object... args) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            buff.append(args[i]);
        }

        return buff.toString();
    }

    /**
     * Simple wrapper to determine if a string exists in another string.
     *
     * @param test         string to scan for the searchString
     * @param searchString
     * @return
     */
    public static final boolean stringContains(final String test,
                                               final String searchString) {
        return test.indexOf(searchString) >= 0;
    }

    /**
     * Expensive (introduces new strings)
     *
     * @param test
     * @param searchString
     * @return
     */
    public static final boolean stringContainsIgnoreCase(final String test,
                                                         final String searchString) {
        return stringContains(test.toLowerCase(), searchString.toLowerCase());
    }

    /**
     * Returns a new string that is a substring of this string. The substring begins at the end of 'prefix' string
     * starting with t the specified index and extends to beginning of the 'suffix' string.
     * <p/>
     * Examples: StringUtil.substring("the slow purple fox", "the ", "fox", 0) returns "slow purple"
     * StringUtil.substring("the slow purple fox", "the slow", "", 0) returns " purple fox"
     *
     * @param buffer     the string to search against.
     * @param prefix     the substring immediately prior to the substring of interest.
     * @param suffix     the substring immediately after the substring of interest.
     * @param beginIndex the beginning index, inclusive.
     * @return the specified substring.
     */
    public static final String substring(String buffer, String prefix, String suffix, int beginIndex) {
        String substring = "";
        int endIndex = -1;


        if (StringUtil.nullOrEmptyString(prefix)) {
            prefix = "";
            beginIndex = 0;
        } else {
            beginIndex = buffer.indexOf(prefix) + prefix.length();
        }


        if (beginIndex >= 0) {
            if (StringUtil.nullOrEmptyString(suffix)) {
                endIndex = buffer.length();
            } else {
                endIndex = buffer.indexOf(suffix, beginIndex);
            }
        }


        if (endIndex >= beginIndex) {
            substring = buffer.substring(beginIndex, endIndex);
        }

        return substring;
    }

    /**
     * Perform a case insensative compare of a substring.  This does not generate any intermediate objects other than
     * char's and ints on the stack. For better performance, if the user of this method knows the most common case, they
     * should use that case in the tag
     *
     * @param buffer
     * @param start
     * @param entityNameLength
     * @param tag
     * @return
     */
    public static final boolean subStringEqualsIgnoreCase(String buffer, int start, int entityNameLength, String tag) {
        if (tag.length() != entityNameLength) {
            return false;
        }
        char c;
        char ct;
        if (buffer.length() < start + entityNameLength) {
            // no point going further, the tag would roll off the end of the buffer
            return false;
        }
        for (int i = 0; i < entityNameLength; i++) {
            c = buffer.charAt(i + start);
            ct = tag.charAt(i);
            if (c == ct) {
                // simple case perhaps people should code their lookups assuming the most common cases.
                continue;
            }
            if (Character.isLetter(c)) {
                if (Character.toLowerCase(c) == Character.toLowerCase(ct)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Perform a case insensitive compare of a substring.  This does not generate any intermediate objects other than
     * char's and ints on the stack. For better performance, if the user of this method knows the most common case, they
     * should use that case in the tag
     *
     * @param buffer
     * @param start
     * @param entityNameLength
     * @param tag
     * @param tagPos
     * @param tagSize
     * @return
     */
    public static final boolean subStringEqualsIgnoreCase(String buffer, int start, int entityNameLength, String tag, int tagPos, int tagSize) {
        if (tagSize != entityNameLength) {
            return false;
        }
        char c;
        char ct;

        for (int i = 0; i < entityNameLength; i++) {
            c = buffer.charAt(i + start);
            ct = tag.charAt(i + tagPos);
            if (c == ct) {
                // simple case perhaps people should code their lookups assuming the most common cases.
                continue;
            }
            if (Character.isLetter(c)) {
                if (Character.toLowerCase(c) == Character.toLowerCase(ct)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * remove all non-alphanumeric characters from a string
     *
     * @param string
     */
    public static final String toAlphaNumericOnly(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Convert a List of strings into an array of strings.
     *
     * @param list
     * @return
     */
    public static final String[] toArray(List<String> list) {
        int size = list.size();
        String array[] = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Need to handle:
     * <p>
     * "this is @@ a test@@case"
     * "@@this is @@ a test @@case@@"
     *
     * @param theString
     * @param seperator
     * @return
     */
    public static String[] tokenizeFromMultiChar(String theString, String seperator, boolean trim) {
        int count = 1;
        int size = seperator.length();
        int length = theString.length() - size;
        boolean startsAt0 = theString.startsWith(seperator);
        boolean endsWith = theString.endsWith(seperator);
        // skip over 0th position
        int index = theString.indexOf(seperator, 1);
        while (index != -1) {
            if (endsWith && index == length) {
                break;
            }
            count++;
            index = theString.indexOf(seperator, index + 1);
        }
        String parts[] = new String[count];
        int arrInd = 0;
        index = theString.indexOf(seperator, 1);
        int priorInd = 0;
        if (startsAt0) {
            priorInd = size;
        }
        while (index != -1) {
            if (trim) {
                parts[arrInd++] = theString.substring(priorInd, index).trim();
            } else {
                parts[arrInd++] = theString.substring(priorInd, index);
            }
            if (endsWith && index == length) {
                break;
            }
            priorInd = index + size;
            index = theString.indexOf(seperator, index + 1);
        }
        if (!endsWith) {
            if (trim) {
                parts[arrInd++] = theString.substring(priorInd, theString.length()).trim();
            } else {
                parts[arrInd++] = theString.substring(priorInd, theString.length());
            }
        }
        return parts;
    }

    /**
     * Given a string containing some kind of "seperator", seperate the string into tokens based upon that seperator.
     * For example:
     * <p/>
     * tokenizeFromSingleChar ("the, quick, brown, fox", ",") would apply an array:
     * <p/>
     * the quick brown fox
     *
     * @param theString
     * @param separator
     * @return
     */
    public static String[] tokenizeFromSingleChar(String theString, String separator) {
        return tokenizeFromSingleChar(theString, separator, false);
    }

    public static List<String> tokensFromSingleCharToList(String theString, String separator) {
        String classPath = System.getProperty("java.class.path");
        String toks[] = StringUtil.tokenizeFromSingleChar(classPath, File.pathSeparator);
        List<String> queue = new ArrayList(toks.length);
        for (String tok : toks) {
            queue.add(tok);
        }
        return queue;
    }

    // --------------------------------------------------------------------------

    /**
     * Take a string and break it up into separate tokens.
     * <p/>
     * For example:
     * <p/>
     * tokenizeFromSingleChar ("the, quick, brown, fox", ",") would apply an array:
     * <p/>
     * the quick brown fox
     *
     * @param theString String we are breaking up
     * @param separator The token separator
     * @param trimIt    , run trim on the strings to remove whitespace
     * @return an array of resulting tokens. Won't ever be null but may have zero length
     */
    public static String[] tokenizeFromSingleChar(final String theString,
                                                  final String separator, final boolean trimIt) {
        if (theString == null) {
            return new String[0];
        }

        StringTokenizer tz = new StringTokenizer(theString, separator);
        int ntok = tz.countTokens();
        String[] result = new String[ntok];
        for (int ii = 0; ii < ntok; ii++) {
            if (trimIt) {
                result[ii] = tz.nextToken().trim();
            } else {
                result[ii] = tz.nextToken();
            }
        }
        return result;
    }

    /**
     * Take a string and break it up into separate tokens. Takes into account quotes to not tokenizeFromSingleChar data within
     * quotes.
     * <p/>
     * Note that this current implementation uses a stringbuilder and could be more efficient by tracking the index
     * positions directly without copying to a buffer.
     * <p/>
     * For example:
     * <p/>
     * tokenizeFromSingleChar ("the, quick, brown, fox", ",") would apply an array:
     * <p/>
     * the quick brown fox
     *
     * @param theString String we are breaking up
     * @param separator The token separator
     * @param trimIt    , run trim on the strings to remove whitespace
     * @return an array of resulting tokens. Won't ever be null but may have zero length
     */
    public static final String[] tokenizeEscapedString(final String theString,
                                                       final char separator, final boolean trimIt) {
        List<String> parts = new ArrayList<String>();
        boolean inQuotes = false;
        int size = theString.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            char currentChar = theString.charAt(i);

            switch (currentChar) {
                case '\'':
                case '\"':
                    inQuotes = !inQuotes;
                    break;
                default:
                    if (currentChar == separator) {
                        if (inQuotes) {
                            builder.append(currentChar);
                        } else if (builder.length() > 0) {
                            parts.add(builder.toString());
                            builder.setLength(0);
                        }
                    } else {
                        builder.append(currentChar);
                    }
            }
        }
        if (builder.length() > 0) {
            parts.add(builder.toString());
        }
        return listToArray(parts);
    }

    /**
     * Tokenize a string, removing any null or empty tokens.
     *
     * @param theString
     * @param separator
     * @param trimIt
     * @return
     */
    public static String[] tokenizeRemovingNullOrEmptyStrings(final String theString,
                                                              final String separator, final boolean trimIt) {
        if (theString == null) {
            return new String[0];
        }

        StringTokenizer tz = new StringTokenizer(theString, separator);
        int ntok = tz.countTokens();
        List<String> resultList = new ArrayList<String>();

        for (int ii = 0; ii < ntok; ii++) {
            String token = tz.nextToken();
            if (!StringUtil.nullOrEmptyString(token)) {
                if (trimIt) {
                    token.trim();
                }
                resultList.add(token);
            }
        }
        int size = resultList.size();
        String result[] = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = resultList.get(i);
        }
        return result;
    }

    /**
     * Convert an array of Strings to a listFiles of Strings
     *
     * @param ar
     * @return list
     */
    public static final List<String> toList(String... ar) {
        ArrayList<String> list = new ArrayList<String>();
        for (String o : ar) {
            list.add(o);
        }
        return list;
    }

    public static String trimString(String str, int length) {
        return truncateToLength(str, length);
    }

    /**
     * Truncate text that needs to be decoded to a max length with rollout character (such as ...). If the text is null
     * then use a default value instead.  The length of the text is max - length of the rollout text.
     *
     * @param text
     * @param max
     * @param post
     * @param defaultValue
     * @return
     */

    public static final String truncateDecodedTextToLengthWithPostText(String text, int max,
                                                                       String post, String defaultValue) {
        if (!StringUtil.nullOrEmptyString(text)) {
            text = HTMLEncoder.decodeHtml(text);
            if (text.length() > max) {
                text = StringUtil.strcat(text.substring(0, max - post.length()), post);
            }
        } else {
            text = defaultValue;
        }
        return text;
    }

    /**
     * Truncate a token to length if it is not null and is greater than the token length limit.
     *
     * @param token
     * @param length
     * @return
     */
    public static final String truncateToLength(String token, int length) {
        if (!StringUtil.nullOrEmptyString(token) && token.length() > length) {
            token = token.substring(0, length);
        }
        return token;
    }

    /**
     * Pretty print the number of bytes in the form of a bytes as B,K,M,G
     *
     * @param bytes
     * @return string of the form 45MB
     */
    public static final String getBytesNeatForm(long bytes) {
        if (bytes < Constants.KBytes) {
            return Fmt.S("%sB", bytes);
        }
        if (bytes < Constants.MBytes) {
            return Fmt.S("%sKB", bytes / Constants.KBytes);
        }
        if (bytes < Constants.GBytes) {
            return Fmt.S("%sMB", bytes / Constants.MBytes);
        }
        return Fmt.S("%sGB", bytes / Constants.GBytes);
    }


    /**
     * Count the number of occurences of a character in a string.
     *
     * @param s
     * @param c
     * @return
     */
    public static final int countInstances(String s, char c) {
        int count = 0;
        int l = s.length();
        for (int i = 0; i < l; i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }

        return count;
    }

    /**
     * Ensure that the l and r characters are evenly balanced assuming a non
     * recursive representation:
     * <p>
     * x.y[2].z[2]  = true
     * x.c[2[2] = false
     *
     * @param s
     * @param l
     * @param r
     * @return
     */
    public static final boolean ensureBalance(String s, char l, char r) {
        int count = 0;
        int length = s.length();

        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == l) {
                count++;

            } else if (c == r) {
                count--;
            }
            if (count > 1 || count < 0) {
                return false;
            }
        }

        return true;
    }
}
