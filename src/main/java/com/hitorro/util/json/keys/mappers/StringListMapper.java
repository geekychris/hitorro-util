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
package com.hitorro.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.StringUtil;

import java.util.List;

public class StringListMapper implements Mapper<JsonNode, List<String>> {
    private String seperator;

    public StringListMapper(String seperator) {
        this.seperator = seperator;
    }

    public List<String> apply(JsonNode jsonNodes) {
        String vals = jsonNodes.textValue();
        if (vals != null) {
            String res[] = StringUtil.tokenizeFromSingleChar(vals, this.seperator, true);
            if (res != null) {
                return StringUtil.toList(res);
            }
        }
        return null;
    }
}
