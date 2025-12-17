package ht.util.io.largedata.compressedstreams;

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

import java.util.ArrayList;

public class RAMFile {
    ArrayList<byte[]> buffers = new ArrayList<byte[]>();
    long length;
    int bufferSize = COutputStream.BUFFER_SIZE;
    long lastModified = System.currentTimeMillis();

    /**
     * Copy the ramfile into one single byte array.  One can provide a byte array to use, if it isnt big enough then a
     * new array will be provided.
     *
     * @param buffer
     *
     * @return byte array containing the ramfile
     */
    public byte[] fillSingleBuffer(byte[] buffer) {
        if (buffer == null || buffer.length < length) {
            // not big enough, grow
            buffer = new byte[(int) length];
        }
        int destPos = 0;
        for (byte[] b : buffers) {
            int currBuffLength = Math.min(bufferSize, (int) length - destPos);
            System.arraycopy(b, 0, buffer, destPos, currBuffLength);
            destPos += bufferSize;
        }
        return buffer;
    }

    public void reset() {
        length = 0;
        buffers.clear();
    }
}

