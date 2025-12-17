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

import com.hitorro.util.core.Log;

/**
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
 * A memory-resident {@link CInputStream} implementation.
 *
 * @version $Id: RAMInputStream.java,v 1.4 2006/02/10 04:35:36 ccollins Exp $
 */

public class RAMInputStream extends CInputStream implements Cloneable {
    private RAMFile file;
    private int pointer = 0;

    public RAMInputStream(RAMFile f) {
        if (f != null) {
            file = f;
            length = file.length;
        }

    }

    public Object clone() {
        if (readOnlyBuffer == true) {
            RAMInputStream returnMe = new RAMInputStream(null);
            returnMe.setBuffer(this.buffer);
            return returnMe;
        } else {
            return super.clone();
        }
    }

    public void readInternal(byte[] dest, int destOffset, int len) {
        int remainder = len;
        int start = pointer;
        while (remainder != 0) {
            int bufferNumber = start / bufferSize;
            int bufferOffset = start % bufferSize;
            int bytesInBuffer = bufferSize - bufferOffset;
            int bytesToCopy = bytesInBuffer >= remainder ? remainder : bytesInBuffer;
            if (file == null) {
                Log.util.error("File is null in RAMInputStream.readInternal seeking byte %s", start);
            }
            byte[] buffer = file.buffers.get(bufferNumber);
            System.arraycopy(buffer, bufferOffset, dest, destOffset, bytesToCopy);
            destOffset += bytesToCopy;
            start += bytesToCopy;
            remainder -= bytesToCopy;
        }
        pointer += len;
    }

    public void close() {
    }

    public void seekInternal(long pos) {
        pointer = (int) pos;
    }
}
