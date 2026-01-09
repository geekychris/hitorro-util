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
package com.hitorro.util.urlparser;

import junit.framework.TestCase;

/**
 */
public class UrlEditDistance extends TestCase {
    private UrlCursor curA = new UrlCursor();
    private UrlCursor curB = new UrlCursor();

    /**
     * Determine how far apart two urls are, ignoring case and not considering arguments.
     * <p/>
     * We consider two urls candidates for similarity if they come from the same host.  If they are not, then -1 is
     * returned.
     * <p/>
     * The calculation is in the form of an "edit distance".  For each change that has to be applied to get to the same
     * url.  For example:
     * <p/>
     * www.cnn.com/world/food/eat.meat.html www.cnn.com/world/food/eat.meat.html
     * <p/>
     * This has an edit distance of 0, since the urls are identical
     * <p/>
     * www.cnn.com/world/food/eat.meat.html www.cnn.com/world/food/eat.fruit.html
     * <p/>
     * This would have the edit distance of 1, since we simply have to replace the file name.
     * <p/>
     * www.cnn.com/world/food/eat.meat.html www.cnn.com/world/health/alergies/hay-fever/thebeesolution.htm
     * <p/>
     * This would have the edit distance of 6.  Since, we have to
     *
     * @param urlA
     * @param urlB
     * @return
     */
    public final int determineUrlDistance(String urlA, String urlB) {
        int distance = 0;
        curA.setUrl(urlA);
        curB.setUrl(urlB);
        if (!curA.nextToken() || !curB.nextToken()) {
            // couldnt even parse past the host.
            return -1;
        }
        if (!curA.isTokenSameIgnoreCase(curB)) {
            // hosts not same
            return -1;
        }
        boolean same = true;
        while (same) {
            boolean nA = curA.nextToken();
            boolean nB = curB.nextToken();
            if (nA && nB &&
                    curA.getUrlPartType() == UrlCursor.Part.Path &&
                    curB.getUrlPartType() == UrlCursor.Part.Path) {
                if (curA.isTokenSameIgnoreCase(curB)) {
                    // same parts
                    continue;
                } else {
                    if (nA) {
                        distance++;
                    }
                    if (nB) {
                        distance++;
                    }
                }


            }

            distance += countPathParts(curA);
            distance += countPathParts(curB);

            same = false;
        }

        return distance;
    }

    private int countPathParts(UrlCursor cur) {
        int distance = 0;
        while (cur.nextToken() && cur.getUrlPartType() == UrlCursor.Part.Path) {
            distance++;
        }
        return distance;
    }
}
