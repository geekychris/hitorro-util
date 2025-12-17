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
package com.hitorro.util.urlparser;

import gnu.trove.map.hash.TLongObjectHashMap;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.BooleanProperty;

/**
 *
 */
public class TLDDictionary {
    public static final BasefileProperty TLDFileKey = new BasefileProperty("tlddictionary.filename", "filename of tld dictionary", "file://${ht_data}/cctld/cctld.txt");
    public static final BooleanProperty LogMissing = new BooleanProperty("tlddictionary.logmissing", "", false);
    public boolean logMissing = LogMissing.apply();
    private TLongObjectHashMap<TLDEntry> tldMap = new TLongObjectHashMap<TLDEntry>();

    public int load() {
        return load(TLDFileKey.apply());
    }

    public int load(BaseFile bf) {
        int count = 0;

        AbstractIterator<String> iter = BaseFileUtil.bf2lineiter.apply(bf);
        while (iter.hasNext()) {
            String name = iter.next();
            String parts[] = StringUtil.tokenizeFromSingleChar(name, ".");
            long hash = FPHash64.hash(parts, parts.length);
            tldMap.put(hash, new TLDEntry(name, parts.length));
            count++;
        }
        return count;
    }

    public TLDEntry getMaxPath(String parts[]) {
        int length = parts.length;
        for (int i = length; i >= 1; i--) {
            long hash = FPHash64.hash(parts, i);
            TLDEntry e = this.tldMap.get(hash);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    /**
     * Get a domain name.  For example if we found for the url:  uk.ac.staffs.me we would of found a maximum tld of:
     * uk.ac  From that we would produce a domain name of: uk.ac.staffs
     * <p/>
     * If the name has the same depth as the parts then we just return a string of the tld
     *
     * @param parts
     * @param buff
     * @param entry
     * @return
     */
    public String getNameFromResult(String parts[], StringBuilder buff, TLDEntry entry) {
        int depth = 0;
        if (entry == null) {
            // default as we dont have the TLD
            if (logMissing) {
                Log.tld.info("Do not know top level domain for %s", StringUtil.mergeWithJoinToken(parts, "."));
            }
            depth = Math.min(parts.length, 2);
        } else {
            if (parts.length <= entry.getSize()) {
                // we are at the same depth, cant put anything.
                return entry.getName();
            }
            depth = entry.getSize() + 1;
        }

        buff.setLength(0);
        for (int i = 0; i < depth; i++) {
            if (i > 0) {
                buff.append(".");
            }
            buff.append(parts[i]);
        }
        return buff.toString();
    }

}

class TLDEntry {
    private String name;
    private int size;

    TLDEntry(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
