package ht.util.basefile.tools.transactiondir;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.string.StringUtil;

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
