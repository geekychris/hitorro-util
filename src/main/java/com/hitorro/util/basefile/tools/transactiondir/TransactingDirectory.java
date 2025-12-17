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
import com.hitorro.util.core.string.Fmt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Directory that contains a set of files that are marked by a version number.  This allows multiple versions of a file
 * to be managed without the transient cases where a file must be removed to be replaced.  This requires a transaction
 * file to keep track of the currentTransaction transaction number (time).
 */
public class TransactingDirectory {
    private static final String TransactionFileName = "transaction.txt";
    private BaseFile transFile;
    private BaseFile dir;
    private long currentTransaction = -1;
    private long lastTransaction = -1;
    private Map<String, TransactingFile> mapOfFiles = new HashMap();

    public TransactingDirectory(BaseFile dir) throws IOException {
        this.dir = dir;
        transFile = dir.getChild(TransactionFileName);
        replenish();
    }

    public long start() throws TransactionException {
        if (currentTransaction != -1) {
            throw new TransactionException("Already in transation");
        }
        currentTransaction = System.currentTimeMillis();
        return currentTransaction;
    }

    public long commit() throws TransactionException, IOException {
        if (currentTransaction == -1) {
            throw new TransactionException("Not in transaction");
        }
        getTransactionFile(TransactionFileName).writeString(Long.toString(currentTransaction));
        TransactingFile tf = mapOfFiles.get(TransactionFileName);
        if (tf != null) {
            tf.purgeAll();
        }
        long ret = currentTransaction;
        currentTransaction = -1;
        replenish();
        return ret;
    }

    public BaseFile getCommittedFile(String name) {
        TransactingFile tf = mapOfFiles.get(name);
        if (tf == null) {
            return null;
        }
        TransactingFileEntry tfe = tf.getEntry();
        if (tfe == null) {
            return null;
        }
        return tfe.getFile();
    }

    public BaseFile getNonTransactionFile(String name) {
        return dir.getChild(name);
    }

    public BaseFile getTransactionFile(String name) throws TransactionException {
        if (currentTransaction == -1) {
            throw new TransactionException("Not in transaction");
        }
        BaseFile file = dir.getChild(Fmt.S("t-%s-%s", currentTransaction, name));

        return file;
    }

    private void replenish() throws IOException {
        mapOfFiles = new HashMap();
        BaseFile files[] = dir.listFiles();
        for (BaseFile file : files) {
            TransactingFileEntry entry = TransactingFileEntry.getEntry(file);
            if (entry != null) {
                TransactingFile tf = mapOfFiles.get(entry.getName());
                if (tf == null) {
                    tf = new TransactingFile(entry.getName());
                    mapOfFiles.put(entry.getName(), tf);
                }
                tf.add(entry);
            }
        }

        // Now we go through all what we have collected and make sure they are in order
        for (TransactingFile tf : mapOfFiles.values()) {
            tf.finishSetup();
        }

        // Find the transaction file if it exists and we want to use the latest file.
        TransactingFile tf = mapOfFiles.get(TransactionFileName);
        lastTransaction = -1;
        if (tf != null) {
            lastTransaction = tf.purgeAllButNewest();
        }
        // get rid of all files that are not cutting it.
        for (TransactingFile t : mapOfFiles.values()) {
            t.purgeAndAssign(lastTransaction);
        }
    }
}
