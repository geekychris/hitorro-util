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

import com.hitorro.util.core.string.StringUtil;

/**
 * against.
 */
public class PathMatch {
    protected long[] hashes;
    protected String[] keys;

    public PathMatch(String... keysIn) {
        int size = keysIn.length;
        hashes = new long[size];
        keys = new String[size];
        for (int i = 0; i < size; i++) {
            hashes[i] = StringUtil.hashStringCaseFree(keysIn[i]);
            keys[i] = keysIn[i];
        }
        // dont sort its just a seperated path
    }

    /**
     * Does not assume its at the start of the url.  Continues from current position
     *
     * @param umc
     * @return
     */
    public boolean match(UrlMemoryCursor umc) {
        long hash;
        for (int i = 0; i < hashes.length; i++) {
            if (i != 0) {
                if (!umc.nextToken()) {
                    return false;
                }
            }
            hash = umc.getHash();
            if (hashes[i] != hash) {
                return false;
            }
            if (!umc.isTokenSameIgnoreCase(keys[i])) {
                return false;
            }
        }
        // all parts are same
        return true;
    }

}
