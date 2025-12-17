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
package com.hitorro.util.json.mapper;

import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONNumber;
import com.hitorro.util.json.JSONType;

/**
 * Keep a min max for two jason fields defined as the min and max fields.  It assumes they are number fields. You can
 * reset the min / max after a run.  Useful for such things as tracking the largest and smalled dates seen in integer /
 * long form.
 */
public class LongMinMaxJSONMapper extends BaseMapper<JSONElement, JSONElement> {
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private String minPath[] = null;
    private String maxPath[] = null;

    public LongMinMaxJSONMapper(String minPath, String maxPath) {
        if (!StringUtil.nullOrEmptyString(minPath)) {
            this.minPath = StringUtil.tokenizeFromSingleChar(minPath, ".");
        }
        if (!StringUtil.nullOrEmptyString(maxPath)) {
            this.maxPath = StringUtil.tokenizeFromSingleChar(maxPath, ".");
        }
    }

    public void reset() {
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    @Override
    public JSONElement apply(final JSONElement e) {
        if (minPath != null) {
            JSONElement minE = e.getFromPath(minPath, 0, minPath.length);
            if (minE != null && minE.getJSONType() == JSONType.Number) {
                long l = ((JSONNumber) minE).get().longValue();
                if (l < min) {
                    min = l;
                }
            }
        }
        if (maxPath != null) {
            JSONElement maxE =
                    e.getFromPath(maxPath, 0, maxPath.length);
            if (maxE != null && maxE.getJSONType() == JSONType.Number) {
                long l = ((JSONNumber) maxE).get().longValue();
                if (l > max) {
                    max = l;
                }
            }
        }
        return e;
    }
}
