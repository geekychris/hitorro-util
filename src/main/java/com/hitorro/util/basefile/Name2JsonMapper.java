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
package com.hitorro.util.basefile;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

public class Name2JsonMapper implements Mapper<String, JsonNode> {
    private BaseFile directory;
    private String defaultDomain;

    public Name2JsonMapper(BaseFile directory, String defaultDomain) {
        this.directory = directory;
        this.defaultDomain = defaultDomain;
    }

    @Override
    public JsonNode apply(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return null;
        }
        int index = s.indexOf("_");
        if (index == -1 && !StringUtil.nullOrEmptyString(defaultDomain)) {
            s = Fmt.S("%s_%s", defaultDomain, s);
        }
        s = s.toLowerCase();
        BaseFile file = directory.getChild("%s.json", s);
        if (file.exists()) {
            try {
                return file.getJsonNode();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}