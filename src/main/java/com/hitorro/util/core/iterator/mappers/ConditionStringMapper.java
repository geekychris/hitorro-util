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
package com.hitorro.util.core.iterator.mappers;


/**
 * Cleans up a string by removing non alpha and possibly numeric chars.
 */
public class ConditionStringMapper extends BaseMapper<String, String> {
    public static final ConditionStringMapper alpha = new ConditionStringMapper(true, true, ConditionStringMapper.Keep.Alpha);
    private StringBuilder sb = new StringBuilder();
    private Keep keep;
    private boolean lowerCase;
    private boolean trim;
    public ConditionStringMapper(boolean lowerCase, boolean trim, Keep keep) {
        this.keep = keep;
        this.lowerCase = lowerCase;
        this.trim = trim;
    }

    public boolean isThreadSafe() {
        return false;
    }

    public BaseMapper getCopy() {
        return new ConditionStringMapper(lowerCase, trim, keep);
    }

    @Override
    public String apply(final String e) {
        if (e == null) {
            return null;
        }
        sb.setLength(0);
        char c;
        for (int i = 0; i < e.length(); i++) {
            c = e.charAt(i);
            if (keep.keep(c)) {
                if (lowerCase) {
                    c = Character.toLowerCase(c);
                }
                sb.append(c);
            }
        }
        if (trim) {
            return sb.toString().trim();
        }
        return sb.toString();
    }

    public enum Keep {
        Alpha() {
            public boolean keep(char c) {
                return Character.isLetter(c) || Character.isSpaceChar(c);
            }

        },
        Numeric() {
            public boolean keep(char c) {
                return Character.isDigit(c) || Character.isSpaceChar(c);
            }

        },
        AlphaNumeric() {
            public boolean keep(char c) {
                return Character.isLetter(c) || Character.isDigit(c) || Character.isSpaceChar(c);
            }

        };

        public abstract boolean keep(char c);


    }
}
