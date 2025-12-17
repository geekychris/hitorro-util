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
package com.hitorro.util.basefile.tools;

import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JVS2JsonMapper;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.jarfile.JarItemIterator;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.JsonValueSource;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.sinks.BaseSink;
import com.hitorro.util.core.iterator.sinks.JSONSinkAsArray;
import com.hitorro.util.core.iterator.sinks.JsonSink;
import com.hitorro.util.core.iterator.sinks.MappingSink;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.io.csv.CSVIterator;
import com.hitorro.util.io.csv.CSVIterator2JSONMapper;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.xml.XE;
import com.hitorro.util.xml.XEDomMapper;

import java.io.*;
import java.util.Set;

/**
 * Set of convenience methods and mappers
 */
public class BaseFileUtil {

    /**
     * BaseFile to input stream
     */
    public static BaseMapper<BaseFile, InputStream> bf2inputstream = new BaseMapper<BaseFile, InputStream>() {
        public InputStream apply(BaseFile bf) {
            try {
                return bf.getInputStream();
            } catch (IOException e) {
                return null;
            }
        }
    };
    public static BaseMapper<BaseFile, OutputStream> bf2outputstream = new BaseMapper<BaseFile, OutputStream>() {
        public OutputStream apply(BaseFile bf) {
            OutputStream os = null;
            try {
                return bf.getDataOutputStream();
            } catch (IOException e) {
                Log.filesystem.error("Unable to create stream for basefile %s %e", e, e);
                return null;
            }
        }
    };

    public static BaseMapper<JsonNode, JsonValueSource> jsonNode2JacksonVS = new BaseMapper<JsonNode, JsonValueSource>() {
        @Override
        public JsonValueSource apply(final JsonNode e) {
            return new JsonValueSource(e);
        }
    };
    public static BaseMapper<JsonValueSource, JsonNode> jacksonVS2json = new BaseMapper<JsonValueSource, JsonNode>() {
        @Override
        public JsonNode apply(final JsonValueSource e) {
            return e.getNode();
        }
    };
    public static BaseMapper<AbstractIterator<JsonNode>, AbstractIterator<JsonValueSource>> jn2valuesource = new BaseMapper<AbstractIterator<JsonNode>, AbstractIterator<JsonValueSource>>() {
        @Override
        public AbstractIterator<JsonValueSource> apply(final AbstractIterator<JsonNode> e) {
            return e.map(jsonNode2JacksonVS);
        }
    };
    public static BaseMapper<BaseFile, Reader> bf2reader = bf2inputstream.combine(FileUtil.is2reader);
    public static BaseMapper<BaseFile, AbstractIterator<String>> bf2lineiter = bf2reader.combine(FileUtil.reader2stringiter);
    public static BaseMapper<BaseFile, AbstractIterator<JSONElement>> bf2json = bf2reader.combine(FileUtil.readerJsonIter);
    public static BaseMapper<BaseFile, AbstractIterator<JSONElement>> bf2twitter_json = bf2reader.combine(FileUtil.readerJsonIterOfTwitter);
    public static BaseMapper<BaseFile, AbstractIterator<JsonNode>> bf2jacksonjson = bf2reader.combine(FileUtil.readerJacksonJsonIter);
    public static BaseMapper<BaseFile, AbstractIterator<JsonValueSource>> bf2jacksonValueSource = bf2jacksonjson.combine(jn2valuesource);
    public static BaseMapper<BaseFile, Writer> bf2utf8Writer = bf2outputstream.combine(FileUtil.os2Utf8Writer);
    public static BaseMapper<BaseFile, PrintWriter> bf2utf8printwriter = bf2outputstream.combine(FileUtil.os2Utf8PrintWriter);
    public static BaseMapper<BaseFile, PrintStream> bf2utf8printstream = bf2outputstream.combine(FileUtil.os2Utf8PrintStream);
    /**
     * Naive construction of a csv reader.  We assume utf8 encoding for csv files and we assume the first sheet if it is
     * a xls file
     */
    public static BaseMapper<BaseFile, CSVIterator> bf2csv = new BaseFileCSV2CSVIteratorMapper(null, "UTF-8");
    public static BaseMapper<BaseFile, CSVIterator> bf2tsv = new BaseFileCSV2CSVIteratorMapper(null, "UTF-8", '\t');
    public static BaseMapper<BaseFile, CSVIterator> bf2tsvOrcsv = new BaseMapper<BaseFile, CSVIterator>() {


        @Override
        public CSVIterator apply(final BaseFile e) {
            String ext = e.getExtension();
            if (!StringUtil.nullOrEmptyString(ext)) {
                if (ext.equalsIgnoreCase("tsv")) {
                    return bf2tsv.apply(e);
                }
            }
            return bf2csv.apply(e);

        }
    };
    public static BaseMapper<BaseFile, AbstractIterator<JsonValueSource>> csv_bf2json = bf2tsvOrcsv.combine(CSVIterator2JSONMapper.me);
    public static BaseMapper<InputStream, AbstractIterator<XE>> is2wikipedia_xedom = new XEDomMapper("page");
    public static BaseMapper<BaseFile, AbstractIterator<XE>> bf2Xwikipdeai_xedom = bf2inputstream.combine(is2wikipedia_xedom);
    public static BaseMapper<InputStream, AbstractIterator<XE>> is2rss_xedom = new XEDomMapper("item");
    public static BaseMapper<BaseFile, AbstractIterator<XE>> bf2rss_xedom = bf2inputstream.combine(is2rss_xedom);
    public static BaseMapper<BaseFile, AbstractIterator<BaseFile>> bf2jar = new BaseMapper<BaseFile, AbstractIterator<BaseFile>>() {
        public AbstractIterator<BaseFile> apply(BaseFile bf) {
            return new JarItemIterator(bf);
        }
    };
    public static BaseMapper<OutputStream, BaseSink<JsonNode>> os2JArraysonSink = new BaseMapper<OutputStream, BaseSink<JsonNode>>() {
        public BaseSink<JsonNode> apply(OutputStream o) {
            return new JSONSinkAsArray(o);
        }
    };

    public static BaseMapper<OutputStream, BaseSink<JsonNode>> os2JJsonSink = new BaseMapper<OutputStream, BaseSink<JsonNode>>() {
        public BaseSink<JsonNode> apply(OutputStream o) {
            return new JsonSink(o);
        }
    };

    public static BaseMapper<OutputStream, BaseSink<JVS>> os2JVSSink = new BaseMapper<OutputStream, BaseSink<JVS>>() {
        public BaseSink<JVS> apply(OutputStream o) {
            return new MappingSink(new JsonSink(o), JVS2JsonMapper.me);
        }
    };


    public static BaseMapper<BaseFile, BaseSink<JsonNode>> bf2ArrayJsonSink = bf2outputstream.combine(os2JArraysonSink);

    /**
     * VERY simple binary differ that will compare two files and determine if they are exactly the same or not.
     *
     * @param a
     * @param b
     * @return 0 if same, 1 if file a does not exist 2 if file b does not exist 3 if file sizes different, 4 if byte
     * level difference
     * @throws IOException
     */
    public static final int binaryFileDiff(BaseFile a, BaseFile b) throws IOException {
        InputStream isA;
        InputStream isB;
        try {
            isA = a.getDataInputStream();
        } catch (FileNotFoundException fnfe) {
            return 1;
        }
        try {
            isB = b.getDataInputStream();
        } catch (FileNotFoundException fnfe) {
            return 2;
        }
        if (a.length() != b.length()) {
            return 3;
        }
        if (IOUtil.getStreamsIdentical(isA, isB)) {
            return 0;
        }
        // bytes are different
        return 4;
    }

    /**
     * write out a set of strings
     *
     * @param file
     * @param map
     * @return
     * @throws IOException
     */
    public static final boolean writeStringFromTObjectString(BaseFile file, TObjectIntHashMap<String> map) throws IOException {
        Writer writer = BaseFileUtil.bf2utf8Writer.apply(file);
        for (TObjectIntIterator<String> it = map.iterator(); it.hasNext(); ) {
            it.advance();
            writer.write(it.key());
            writer.write(Constants.NewLineString);
        }
        return true;
    }

    public static final boolean writeStringStringCountFromTObjectString(BaseFile file, TObjectIntHashMap<String> map) throws IOException {
        Writer writer = BaseFileUtil.bf2utf8Writer.apply(file);
        for (TObjectIntIterator<String> it = map.iterator(); it.hasNext(); ) {
            it.advance();
            writer.write(it.key());
            writer.write(",");
            writer.write(Integer.toString(it.value()));
            writer.write(Constants.NewLineString);
        }
        return true;
    }

    public static final boolean writeStringFromSet(BaseFile file, Set<String> set) throws IOException {
        Writer writer = BaseFileUtil.bf2utf8Writer.apply(file);
        for (String s : set) {
            writer.write(s);
            writer.write(Constants.NewLineString);
        }
        return true;
    }

    public static final boolean writeTLongStringHashMap(BaseFile file, TLongObjectHashMap<String> map) throws IOException {
        OutputStream os = BaseFileUtil.bf2outputstream.apply(file);
        DataOutputStream dos = new DataOutputStream(os);
        for (TLongObjectIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeLong(it.key());
            dos.writeUTF(it.value().toString());
        }
        dos.flush();
        dos.close();
        return true;
    }


}

