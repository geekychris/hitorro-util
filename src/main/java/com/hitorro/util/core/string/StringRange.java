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

import gnu.trove.map.hash.TLongObjectHashMap;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.map.MapUtil;

import java.util.List;

public class StringRange<P> {

    private String buffer;
    private String spanText = null;
    private int start;
    private int end;
    private P payload;
    private StringRange children[];
    private long fingerprint = 0;

    public StringRange(String buffer) {
        this.buffer = buffer;
        start = 0;
        end = buffer.length() - 1;
    }

    public StringRange(String buffer, int indexStart, int indexEnd) {
        this.buffer = buffer;
        this.start = indexStart;
        this.end = indexEnd;
    }

    public long getFP64() {
        if (fingerprint == 0) {
            fingerprint = FPHash64.getFP(buffer.substring(start, end));
        }
        return fingerprint;
    }

    public StringRange<P>[] getChildren() {
        return children;
    }

    public void setChildren(List<StringRange> coll) {
        children = coll.toArray(new StringRange[coll.size()]);
    }

    public void setChildren(StringRange children[]) {
        this.children = children;
    }

    public P getPayload() {
        return payload;
    }

    public void setPayload(P payload) {
        this.payload = payload;
    }

    public String toString() {
        return String.format("{%s}  s: {%d}, e: {%s}", getSpan(), start, end);
    }

    public StringRange getSpanContaining(int pos) {
        if (start <= pos && end >= pos) {
            if (children != null) {
                for (StringRange span : children) {
                    StringRange ret = span.getSpanContaining(pos);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
            return this;
        }
        return null;
    }

    public String getBuffer() {
        return buffer;
    }

    public String getSpan() {
        if (spanText == null) {
            spanText = buffer.substring(start, end);
        }
        return spanText;
    }

    /**
     * Compute a hash apply of fingerprints to list of spans with that fingerprint.
     *
     * @return
     */
    public TLongObjectHashMap<List<StringRange<P>>> getSpanHashMap() {
        TLongObjectHashMap<List<StringRange<P>>> map = new TLongObjectHashMap<List<StringRange<P>>>();
        getSpanHashMap(map);
        return map;
    }

    protected void getSpanHashMap(TLongObjectHashMap<List<StringRange<P>>> map) {
        long fp = getFP64();
        MapUtil.add(map, fp, this);
        if (children != null) {
            for (StringRange<P> span : children) {
                span.getSpanHashMap(map);
            }
        }
    }
}
