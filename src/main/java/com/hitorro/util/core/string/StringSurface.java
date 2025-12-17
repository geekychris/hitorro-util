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

enum State {
    Upper('A', 1), Lower('a', 3), Number('d', 3), Dash('-', 1), Unknown('U', 1);

    private char c;
    private int freq;

    State(char c, int freq) {
        this.c = c;
        this.freq = freq;

    }

    public int getFreq() {
        return freq;
    }

    public char getRepresentation() {
        return c;
    }

}

/**
 * Take a string and express it in terms of a surface syntax:
 * <p>
 * Christopher             ==> Aaaa
 * THISissaaa-1232aa-Taaa  ==> Aaaa-dddaa-Aaaa
 * %^^###AAAaaaaabbbb1111  ==> UAaaaddd
 * <p>
 * This is great for describing the general shape of string for feature generation
 */
public class StringSurface {
    private StringBuilder sb = new StringBuilder();

    public String compute(String s) {
        sb.setLength(0);

        State state = getState(s.charAt(0));
        int count = state.getFreq();
        sb.append(state.getRepresentation());
        int length = s.length();
        for (int i = 1; i < length; i++) {
            State newState = getState(s.charAt(i));
            if (newState == state) {
                count--;
                if (count > 0) {
                    sb.append(state.getRepresentation());
                }
            } else {
                state = newState;
                sb.append(state.getRepresentation());
                count = state.getFreq();
            }
        }
        return sb.toString();
    }

    private State getState(char c) {
        if (Character.isLetter(c)) {
            if (Character.isUpperCase(c)) {
                return State.Upper;
            }
            return State.Lower;
        }
        if (Character.isDigit(c)) {
            return State.Number;
        }
        if (c == '-') {
            return State.Dash;
        }
        return State.Unknown;
    }
}


