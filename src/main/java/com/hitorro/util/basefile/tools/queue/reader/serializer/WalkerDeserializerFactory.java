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
package com.hitorro.util.basefile.tools.queue.reader.serializer;

import java.util.HashMap;
import java.util.Map;


public class WalkerDeserializerFactory {
    private static WalkerDeserializerFactory me = new WalkerDeserializerFactory();
    private Map<String, WalkerDeserializer> map = new HashMap();

    private WalkerDeserializer defaultSerializer;

    public WalkerDeserializerFactory() {
        defaultSerializer = null;
    }

    public static WalkerDeserializerFactory getInstance() {
        return me;
    }

    public void add(WalkerDeserializer deserializer, String extension, boolean defaultVal) {
        map.put(extension, deserializer);
        if (defaultVal == true) {
            defaultSerializer = deserializer;
        }
    }

    public WalkerDeserializer getDeserializer(String extension) {
        WalkerDeserializer ser = map.get(extension);
        if (ser != null) {
            return ser;
        }
        return defaultSerializer;
    }
}
