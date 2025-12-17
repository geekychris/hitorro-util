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
package com.hitorro.util.basefile.fs.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.BooleanProperty;

import java.io.IOException;

/**
 * Receiver of a single string that we put into a hashtable where we ultimate write out the string along with its
 * frequency.
 */
public class StringSetBaseFileStateSink extends HashCountingBaseFileStateSink<String> {
    public static final BasefileProperty BaseFileKey = new BasefileProperty("outfile", "outputfile in the format of a basefile");
    public static BooleanProperty WriteCount = new BooleanProperty("writecount", "write the frequency count", false);
    private boolean writeCounts = false;

    public StringSetBaseFileStateSink(BaseFile outputFile, boolean writeCounts) {
        super(outputFile);
        this.writeCounts = writeCounts;
    }

    public StringSetBaseFileStateSink() {
        // called from configs
        super(null);
    }

    public boolean init(final JsonNode map) {
        writeCounts = WriteCount.apply(map);
        this.setBaseFile(BaseFileKey.apply(map));
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        if (writeCounts) {
            return BaseFileUtil.writeStringStringCountFromTObjectString(outputFile, set);
        } else {
            return BaseFileUtil.writeStringFromTObjectString(outputFile, set);
        }
    }
}
