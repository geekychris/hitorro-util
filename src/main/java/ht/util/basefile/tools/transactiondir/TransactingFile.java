package ht.util.basefile.tools.transactiondir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TransactingFile {
    private List<TransactingFileEntry> entries = new ArrayList();
    private TransactingFileEntry entry;
    private String name;

    public TransactingFile(String name) {
        this.name = name;
    }

    /**
     * Once we have purged all others we are left with the entry
     *
     * @return
     */
    public TransactingFileEntry getEntry() {
        return entry;
    }

    public void add(TransactingFileEntry entry) {
        entries.add(entry);
    }

    public String getName() {
        return name;
    }

    public void finishSetup() {
        Collections.sort(entries);
    }

    public void purgeAll() throws IOException {
        for (TransactingFileEntry e : entries) {
            e.getFile().delete();
        }
        entries.clear();
    }

    /**
     * Used to pull out the latest when this is a transaction file set
     *
     * @return
     * @throws IOException
     */
    public long purgeAllButNewest() throws IOException {
        if (entries.size() > 0) {
            entry = entries.remove(entries.size() - 1);
        } else {
            // nada, we dont have a transaction
            return -1;
        }
        purgeAll();
        return entry.getTransaction();
    }

    /**
     * find the latest entry by closest to the transaction number but no bigger and then get rid of all the others.
     *
     * @param transactionNumber
     * @throws IOException
     */
    public void purgeAndAssign(long transactionNumber) throws IOException {
        int index = getIndex(transactionNumber);
        if (index != -1) {
            entry = entries.remove(index);
        }
        purgeAll();
    }

    private int getIndex(long transactionNumber) {
        for (int i = 0; i < entries.size(); i++) {
            TransactingFileEntry entry = entries.get(i);
            if (entry.getTransaction() > transactionNumber) {
                return i - 1;
            }
        }
        return entries.size() - 1;
    }
}
