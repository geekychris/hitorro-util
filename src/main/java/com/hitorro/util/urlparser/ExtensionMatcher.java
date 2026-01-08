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
 * extension of a url
 */
public class ExtensionMatcher extends TokenMatch {
    public ExtensionMatcher(String... args) {
        super(args);
    }

    public boolean match(UrlMemoryCursor umc) {
        if (umc.hasExtension()) {
            long hash = umc.getValueHash();
            long t;
            for (int i = 0; i < hashes.length; i++) {
                t = hashes[i];
                if (t == hash) {
                    // hash test check for proper extension test
                    if (umc.isValuePartTokenSameIgnoreCase(keys[i])) {
                        return true;
                    }

                }
                if (t > hash) {
                    // didnt find a test
                    return false;
                }
            }
        }
        return false;
    }
}
