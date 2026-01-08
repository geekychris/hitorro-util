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

import java.util.Arrays;

/**
 * collector of parameters we care to extract from a url using the UrlCursor.  This should be a relatively efficient
 * linear extraction process as long as the parameters expected are finite.  Other collectors should be used if key
 * value pairs are unknown in advance.
 */
public class UrlCursorParameters {
    // sorted by hash
    private UrlCursorParameter params[];

    public UrlCursorParameters(UrlCursorParameter... params) {
        this.params = params;
        Arrays.sort(params);
    }

    /**
     * Scan the url picking out any of the
     *
     * @param umc
     * @return
     */
    public boolean collect(UrlMemoryCursor umc) {
        boolean found = false;
        for (UrlCursorParameter p : params) {
            p.reset();
        }
        if (!umc.resetToFirstArg()) {
            return false;
        }
        long hash;
        do {
            hash = umc.getKeyHash();
            for (UrlCursorParameter p : params) {
                if (hash < p.hash) {
                    break;
                }
                if (hash == p.hash) {
                    // ensure strings test (hash is not enough)
                    if (umc.isKeyPartTokenSameIgnoreCase(p.token)) {
                        // found a test
                        String val = umc.getValuePartOfArgToken();
                        p.setValue(val);
                        found = true;
                        break;
                    }
                }
            }
        }
        while (umc.nextToken());
        return found;
    }
}