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
package com.hitorro.util.core.map;


import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.CommandArgs;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.longword.NamedBitsOfLong;
import com.hitorro.util.core.longword.WordBits;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.csv.CSVReader;
import com.hitorro.util.io.csv.ColumnTableMeta;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

/**
 * Wrapper around a TLongLongHashMap that provides a way to assume that the key is hash and the long value is a series
 * of bits that can be used to express such things as parts of speech flags.  Assumes a potential source of the data are
 * csv and named value files.  When called for the first time against a directory, it will not find a serialized
 * hashtable and therefor load the csv and nv files into a hash, followed by promptly saving the serialized hashtable
 * out.  Subsequent calls use the serialized hash, unless it has been removed.  Changing a csv or nv file currently does
 * not cause a recompute.
 *
 * @auther: chris
 */
public class FPHashLongMap extends FPHashBaseMap implements CSVConsumer {

    public static final String KeyName = "key";

    public static final String MapFileName = "tlonglong.apply";
    public static final String HashileName = "hash";
    public static final String WordbitsHashfileName = "wordbitshash.hsh";


    // used in the loading part.
    private JVS csvMap = new JVS();
    private ColumnTableMeta meta;

    protected FPHashLongMap(WordBits bits, BaseMapper<String, Long> keyMapping, boolean lowerCase) {
        init(0, bits, defaultInitialSize, keyMapping, lowerCase);
    }

    protected FPHashLongMap(WordBits bits, int initialSize, BaseMapper<String, Long> keyMapping, boolean lowerCase) {
        init(0, bits, initialSize, keyMapping, lowerCase);
    }

    /**
     * @param bits
     * @param dir
     * @param expectedSize
     * @param lowerCase
     * @param tokenizeKey
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static FPHashLongMap loadFromDirectory(WordBits bits, BaseFile dir, int expectedSize, boolean lowerCase, boolean tokenizeKey)
            throws IOException, ParseException {
        return loadFromDirectory(bits, dir, expectedSize, lowerCase, getTokenizer(tokenizeKey));
    }

    /**
     * @param bits
     * @param dir
     * @param expectedSize
     * @param lowerCase
     * @param keyMapper
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static FPHashLongMap loadFromDirectory(WordBits bits, BaseFile dir, int expectedSize, boolean lowerCase, BaseMapper<String, Long> keyMapper)
            throws IOException, ParseException {
        BaseFile[] files = getFiles(dir);

        long nbolHash = 0;
        for (NamedBitsOfLong nbol : bits) {
            nbolHash = FPHash64.combineFingerPrints(nbol.getLongHash(), nbolHash);
        }

        BaseFile mapFile = dir.getChild(MapFileName);
        BaseFile hashFile = dir.getChild(HashileName);

        BaseFile wordBitsHashFile = dir.getChild(WordbitsHashfileName);
        long existingWordBitsHash = 0;
        if (wordBitsHashFile.exists()) {

            existingWordBitsHash = wordBitsHashFile.readLong();
        }
        if (existingWordBitsHash != nbolHash) {
            Log.util.info("WordBits for fphashlongmap: %s changed from %s to %s", dir, existingWordBitsHash, nbolHash);
            wordBitsHashFile.delete();
            wordBitsHashFile.writeLong(nbolHash);
            // nuke our files
            mapFile.delete();
            hashFile.delete();
        }

        long filesHash = hashFiles(files);

        long existingHash = 0;
        if (hashFile.exists()) {
            existingHash = hashFile.readLong();
        }

        FPHashLongMap m;
        if (filesHash == existingHash) {
            if (mapFile.exists()) {
                m = new FPHashLongMap(bits, keyMapper, lowerCase);
                m.load(mapFile);
                return m;
            }
        } else {
            // not same write out new hash. Remove hash file
            mapFile.delete();
            hashFile.writeLong(filesHash);
        }


        // new up with the appropriate expected size since we didnt have it on disk to get exact size
        m = new FPHashLongMap(bits, expectedSize, keyMapper, lowerCase);
        // dont have the cached file,

        for (BaseFile file : files) {
            String ext = file.getExtension();
            if (!StringUtil.nullOrEmptyString(ext)) {
                ext = ext.toLowerCase();
                if ("csv".equals(ext)) {
                    m.loadFromCSV(file, false);
                } else if ("tsv".equals(ext)) {
                    m.loadFromCSV(file, true);
                } else {
                    m.loadFromNameValuePairs(file);
                }
            }
        }
        m.write(mapFile);
        return m;
    }

    private static BaseFile[] getFiles(final BaseFile dir) throws IOException {
        FileEndsWith csvFilter = new FileEndsWith("csv", true);
        FileEndsWith nvFilter = new FileEndsWith("nv", true);
        FileEndsWith tsvFilter = new FileEndsWith("tsv", true);
        LogicalOrOperator or = new LogicalOrOperator(csvFilter, nvFilter, tsvFilter);
        BaseFile files[] = dir.listFiles(or);
        return files;
    }

    /**
     * Lame function that generates some kinda hash from the last modified times and file names.
     *
     * @param files
     * @return some funky hash code
     */
    private static long hashFiles(BaseFile files[]) {
        long fp = 0;
        for (BaseFile f : files) {
            String s = StringUtil.strcat(f.getName(), Long.toString(f.getModifiedTime()));
            fp = FPHash64.combineFingerPrints(FPHash64.getFP(s), fp);
        }
        return fp;
    }

    private static BaseMapper<String, Long> getTokenizer(boolean tokenizeKey) {
        if (tokenizeKey) {
            return String2HashFunctions.stringtokens2hash;
        } else {
            return String2HashFunctions.string2hash;
        }
    }

    public void load(BaseFile f) throws IOException {
        map = f.readTLongLong();
    }

    /**
     * Write
     *
     * @param f
     * @throws IOException
     */
    public void write(BaseFile f) throws IOException {
        f.writeMap(map);
    }

    private void loadFromCSV(BaseFile file, boolean tab) throws IOException {
        CSVReader reader = null;
        if (tab) {
            reader = new CSVReader(file, "utf-8", '\t');
        } else {
            reader = new CSVReader(file, "utf-8");
        }
        reader.readLines(this);
    }

    private void loadFromNameValuePairs(BaseFile f) throws ParseException {
        Iterator<String> iter = BaseFileUtil.bf2lineiter.apply(f);
        String s;
        while (iter.hasNext()) {
            try {
                s = iter.next();
                csvMap.clear();
                CommandArgs.getParameters(s, false, true, csvMap);
                this.add(csvMap);
            } catch (PropaccessError propaccessError) {
                return;
            }
        }
    }

    public void line(int rowCount, String[] line) {
        try {
            if (rowCount == 0) {
                meta = ColumnTableMeta.init(line);
            } else {
                csvMap.clear();
                String names[] = meta.getColumnNames();
                for (int i = 0; i < names.length; i++) {
                    csvMap.set(names[i], line[i]);
                }
                add(csvMap);
            }
        } catch (PropaccessError propaccessError) {
            propaccessError.printStackTrace();
        }
    }

    protected void add(JVS m) throws PropaccessError {
        String v = csvMap.getString(KeyName);
        if (StringUtil.nullOrEmptyString(v)) {
            // problem, expected a key!!!!
        } else {
            updateRecord(m, v, 0);
        }
    }
}
