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
package com.hitorro.util.core.map;

import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.string.StringUtil;

/**
 *
 */
public class String2HashFunctions {
    public static BaseMapper<String, Long> string2hash = new BaseMapper<String, Long>() {
        @Override
        public Long apply(final String string) {
            return FPHash64.getFP(string);
        }
    };

    /**
     * take a string such as "the quick brown fox" and tokenizeFromSingleChar hashing each token seperately and combining at the end.
     * This is the same approach used in the phrase emitter of an analyzer.  This is still a naive implementation
     * because if the analyzer does any type of normalization it will not test!
     * <p/>
     * XXX Note that here we use the getFPViaChars method that does not utf-8 encode, but instead uses a char array.
     * This does not produce the same result, but is a much faster method especially in the tokenization chain
     */
    public static BaseMapper<String, Long> stringtokens2hash = new BaseMapper<String, Long>() {

        @Override
        public Long apply(final String string) {
            String[] parts = StringUtil.tokenizeFromSingleChar(string, " ");
            if (parts.length == 1) {
                return FPHash64.getFPViaChars(string);
            }
            long last = FPHash64.getFPViaChars(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                last = FPHash64.combineFingerPrints(last, FPHash64.getFPViaChars(parts[i]));
            }
            return last;
        }
    };
}
