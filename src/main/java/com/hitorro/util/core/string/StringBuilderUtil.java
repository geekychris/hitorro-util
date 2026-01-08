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
package com.hitorro.util.core.string;

import java.io.*;

/**
 */
public class StringBuilderUtil {
    /**
     * Append contents of string to a string builder if it is not null or empty
     *
     * @param sb
     * @param s
     */
    public static void appendIfNotNull(StringBuilder sb, String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return;
        }
        sb.append(s);
    }

    public static final void appendIfNotNull(StringBuilder builder, String description, String value, String seperator) {
        if (!StringUtil.nullOrEmptyOrBlankString(value)) {
            if (builder.length() > 0 && !StringUtil.nullOrEmptyString(seperator)) {
                builder.append(seperator);
            }
            builder.append(description);
            builder.append(value);
        }
    }

    /**
     * Merge into a StringBuilder the elements of the array seperated by the join token.
     *
     * @param buff
     * @param array
     * @param startPos
     * @param joinToken
     */
    public static final void mergeWithJoinToken(final StringBuilder buff,
                                                final Object array[], final int startPos, final String joinToken) {
        for (int i = startPos; i < array.length; i++) {
            if (i != 0) {
                buff.append(joinToken);
            }
            buff.append(array[i]);
        }
    }

    /**
     * Merge into a StringBuilder the elements of the array seperated by the join token.
     *
     * @param buff
     * @param array
     * @param startPos
     * @param joinToken
     */
    public static final void mergeWithJoinToken(final StringBuilder buff,
                                                final Object array[],
                                                final int startPos,
                                                final int endPos,
                                                final String joinToken) {
        for (int i = startPos; i < endPos; i++) {
            if (i != 0) {
                buff.append(joinToken);
            }
            buff.append(array[i]);
        }
    }

    /**
     * Merge into a StringBuilder the elements of the array seperated by the join token.
     *
     * @param buff
     * @param array
     * @param joinToken
     */
    public static final void mergeWithJoinToken(final StringBuilder buff,
                                                final Object array[], final String joinToken) {
        mergeWithJoinToken(buff, array, 0, joinToken);
    }

    /**
     * Given an array of objects, apply them into a stingle string using a join token. Do this only for the array
     * elements withing the range provided.
     *
     * @param buff      to receive the concatinate content in.
     * @param array
     * @param joinToken token to use to insert between each
     * @param start     position to include in apply.
     * @param length    how many positions to include in apply.
     * @return String of merged tokens.
     */
    public static final void mergeWithJoinToken(final StringBuilder buff,
                                                final Object array[], final String joinToken, final int start,
                                                final int length) {

        for (int i = start; i < start + length; i++) {
            if (i != start) {
                buff.append(joinToken);
            }
            buff.append(array[i]);
        }
    }

    /**
     * Test that the references string is not null and not empty (that is 0 chars in length)
     *
     * @param test string
     * @return true if null or empty string
     */
    public static final boolean nullOrEmptyString(final StringBuilder test) {
        return test == null || test.length() == 0;
    }

    /**
     * Read a file into a StringBuilder.
     *
     * @param builder We will append all content to the end of this StringBuilder.
     * @param file
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void readFileIntoBuilder(StringBuilder builder, File file)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
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

    /**
     * Read an CInputStream into a StringBuilder. The input stream is assumed to contained characters.
     *
     * @param builder We will append all content to the end of this StringBuilder.
     * @param in      The input stream
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
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
     * @throws java.io.IOException
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
     * Concatinate a series of objects together using their toString() form. This uses a non synchronized string buffer
     * to performn the contination.
     *
     * @param args
     * @return StringBuilder with concatinated content
     */
    public static final StringBuilder strcat(StringBuilder buff, Object... args) {
        for (int i = 0; i < args.length; i++) {
            buff.append(args[i]);
        }

        return buff;
    }
}
