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
package com.hitorro.util.basefile.fs;

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.BZip2Codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public enum CompressionType {
    gz("gz") {
        @Override
        public InputStream getInputCompressed(final InputStream is) throws IOException {
            return new GZIPInputStream(is);
        }

        @Override
        public OutputStream getOutputStreamCompressed(final OutputStream os) throws IOException {
            return new GZIPOutputStream(os);
        }
    },
    bz2("bz2") {
        @Override
        public InputStream getInputCompressed(final InputStream is) throws IOException {

            return bz2codec.createInputStream(is);
        }

        @Override
        public OutputStream getOutputStreamCompressed(final OutputStream os) throws IOException {
            return bz2codec.createOutputStream(os);
        }
    },
    none("null") {
        @Override
        public InputStream getInputCompressed(final InputStream is) {
            return is;
        }

        @Override
        public OutputStream getOutputStreamCompressed(final OutputStream os) {
            return os;
        }
    };

    static BZip2Codec bz2codec = getbz2Codec();
    private static HashMap<String, CompressionType> s_byShortName;
    private String extension;
    CompressionType(String extension) {
        this.extension = extension;
        setMapEntry(this);
    }

    private static BZip2Codec getbz2Codec() {
        BZip2Codec codec = new BZip2Codec();
        codec.setConf(new Configuration());
        return codec;
    }

    public static CompressionType getFilterByFileName(String name) {
        return getFilterByName(FileUtil.getFileExtension(name));
    }

    public static CompressionType getFilterByName(String name) {
        if (StringUtil.nullOrEmptyString(name)) {
            return none;
        }
        CompressionType ct = s_byShortName.get(name.toLowerCase());
        if (ct == null) {
            return none;
        }
        return ct;
    }

    public static int size() {
        return s_byShortName.size();
    }

    private static void setMapEntry(CompressionType filter) {
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, CompressionType>();
        }
        s_byShortName.put(filter.getExtension(), filter);
    }

    public String getExtension() {
        return extension;
    }

    public abstract InputStream getInputCompressed(InputStream is) throws IOException;

    public abstract OutputStream getOutputStreamCompressed(OutputStream os) throws IOException;

    public String getNameSansCompression(String name) {
        if (this == CompressionType.none) {
            return name;
        }
        return name.substring(0, name.length() - extension.length() - 1);
    }
}

