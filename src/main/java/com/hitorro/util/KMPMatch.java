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
package com.hitorro.util;


public class KMPMatch {
    private int[] failure = new int[20];
    private int matchPoint;

    public static void test() {
        KMPMatch kmp = new KMPMatch();

        int ind = kmp.match("ABC ABCDAB ABCDABDDABDE", "ABCDABD");
        assert ind == 11;

        ind = kmp.match("ABC ABCDAB ABCDABCDABDE", "ABCDABD");
        assert ind == -1;
    }

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    final public int match(String text, String pattern) {
        if (failure.length < text.length()) {
            failure = new int[text.length() * 2];
        }
        computeFailure(pattern);
        int j = 0;
        int tl = text.length();
        int pl = pattern.length();
        if (tl == 0) return -1;

        for (int i = 0; i < tl; i++) {
            char tc = text.charAt(i);
            while (j > 0 && pattern.charAt(j) != tc) {
                j = failure[j - 1];
            }
            if (pattern.charAt(j) == tc) {
                j++;
            }
            if (j == pl) {
                matchPoint = i - pl + 1;
                return matchPoint;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private void computeFailure(String pattern) {
        int j = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(j) != pattern.charAt(i)) {
                j = failure[j - 1];
            }
            if (pattern.charAt(j) == pattern.charAt(i)) {
                j++;
            }
            failure[i] = j;
        }
    }

    public int getMatchPoint() {
        return matchPoint;
    }
}
