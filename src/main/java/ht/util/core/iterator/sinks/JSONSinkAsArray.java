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
package ht.util.core.iterator.sinks;


import ht.util.integrationevents.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public class JSONSinkAsArray extends JsonSink {
    public JSONSinkAsArray(OutputStream os) {
        super(os);
    }

    public boolean start() {
        try {
            ow.write('[');
        } catch (IOException e) {
            Log.util.error("Unable to write out start block %s", e);
        }
        return true;
    }

    public void writeOutSeperator() throws IOException {
        ow.write(',');
    }

    public boolean stop() {
        try {
            ow.write(']');
            ow.flush();
            ow.close();
        } catch (IOException e) {
            Log.util.error("Unable to write out end block %s", e);
        }
        return true;
    }
}
