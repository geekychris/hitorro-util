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

/**
 * <p/>
 */
public class TLDMatcher<O extends TokenKey> extends PathComponentMatcher<O> {
    public TLDMatcher(final O unknown, final O... keysIn) {
        super(unknown, keysIn);
    }

    public boolean resetTo(UrlMemoryCursor umc) {
        umc.resetToHost();
        return umc.computeTLD();
    }

    public O match(UrlMemoryCursor umc) {
        if (resetTo(umc)) {

            long hash = umc.getValueHash();
            long t;
            for (int i = 0; i < hashes.length; i++) {
                t = hashes[i];
                if (t == hash) {
                    // hash test check for proper extension test
                    if (umc.isValuePartTokenSameIgnoreCase(keys[i].getToken())) {
                        return keys[i];
                    }
                }
                if (t > hash) {
                    // didnt find a test
                    return unknown;
                }
            }
        }

        return unknown;
    }
}

