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
package com.hitorro.util.io.largedata.compressedstreams;

/**
 * Taken from lucene and re-tooled for our hacked IO streams.
 * <p>
 * Copyright 2004 The Apache Software Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;


/**
 * Optimized implementation of a vector of bits.  This is more-or-less like java.util.BitSet, but also includes the
 * following: <ul> <li>a count() method, which efficiently computes the number of one bits;</li> <li>optimized read from
 * and write to disk;</li> <li>inlinable get() method;</li> </ul>
 *
 * @author Doug Cutting
 * @version $Id: BitVector.java,v 1.1 2005/10/13 23:50:54 ccollins Exp $
 */
public final class BitVector {

    private static final byte[] BYTE_COUNTS = {      // table of bits/byte
            0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8
    };
    private byte[] bits;
    private int size;
    private int count = -1;

    /**
     * Constructs a vector capable of holding <code>n</code> bits.
     */
    public BitVector(int n) {
        size = n;
        bits = new byte[(size >> 3) + 1];
    }

    /**
     * Constructs a bit vector from the file <code>name</code> in Directory <code>d</code>, as written by the {@link
     * #write} method.
     */
    public BitVector(CInputStream input) throws IOException {
        try {
            size = input.readInt();              // read size
            count = input.readInt();              // read count
            bits = new byte[(size >> 3) + 1];          // allocate bits
            input.readBytes(bits, 0, bits.length);      // read bits
        } finally {
            input.close();
        }
    }

    /**
     * Sets the value of <code>bit</code> to one.
     */
    public final void set(int bit) {
        bits[bit >> 3] |= 1 << (bit & 7);
        count = -1;
    }

    /**
     * Sets the value of <code>bit</code> to zero.
     */
    public final void clear(int bit) {
        bits[bit >> 3] &= ~(1 << (bit & 7));
        count = -1;
    }

    /**
     * Returns <code>true</code> if <code>bit</code> is one and <code>false</code> if it is zero.
     */
    public final boolean get(int bit) {
        return (bits[bit >> 3] & (1 << (bit & 7))) != 0;
    }

    /**
     * Returns the number of bits in this vector.  This is also one greater than the number of the largest valid bit
     * number.
     */
    public final int size() {
        return size;
    }

    /**
     * Returns the total number of one bits in this vector.  This is efficiently computed and cached, so that, if the
     * vector is not changed, no recomputation is done for repeated calls.
     */
    public final int count() {
        // if the vector has been modified
        if (count == -1) {
            int c = 0;
            int end = bits.length;
            for (int i = 0; i < end; i++) {
                c += BYTE_COUNTS[bits[i] & 0xFF];      // sum bits per byte
            }
            count = c;
        }
        return count;
    }

    /**
     */
    public final void write(COutputStream output) throws IOException {
        try {
            output.writeInt(size());              // flushToDisk size
            output.writeInt(count());              // flushToDisk count
            output.writeBytes(bits, bits.length);      // flushToDisk bits
        } finally {
            output.close();
        }
    }

}
