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
package com.hitorro.util.basefile.tools.transactiondir;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.string.StringUtil;

/**
 *
 */
public class TransactingFileEntry implements Comparable<TransactingFileEntry> {
    private BaseFile bf;
    private String namePart;
    private long transaction;

    private TransactingFileEntry(BaseFile bf, String parts[]) {
        this.bf = bf;
        transaction = Long.parseLong(parts[1]);
        int index = StringUtil.nthIndex(bf.getName(), "-", 2);
        if (index != -1) {
            namePart = bf.getName().substring(index + 1);
        }
    }

    public static TransactingFileEntry getEntry(BaseFile bf) {
        if (bf.getName().startsWith("t-")) {
            String parts[] = StringUtil.tokenizeFromSingleChar(bf.getName(), "-");
            if (parts.length < 3) {
                // not a transaction file.
                return null;
            }
            return new TransactingFileEntry(bf, parts);
        }
        return null;
    }

    public long getTransaction() {
        return transaction;
    }

    public String getName() {
        return namePart;
    }

    public BaseFile getFile() {
        return bf;
    }

    @Override
    public int compareTo(final TransactingFileEntry transactingFileEntry) {
        return (int) (transaction - transactingFileEntry.transaction);
    }
}
