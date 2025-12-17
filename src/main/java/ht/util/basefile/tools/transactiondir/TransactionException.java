package ht.util.basefile.tools.transactiondir;

/**
 * Created by IntelliJ IDEA. User: Chris Date: Jan 13, 2011 Time: 9:22:42 PM To change this template use File | Settings
 * | File Templates.
 */
public class TransactionException extends Exception {
    public TransactionException(String msg) {
        super(msg);
    }

    public TransactionException(Exception e) {
        super(e);
    }
}

