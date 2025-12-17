package ht.util.core.longword.opers;

/**
 * Compare a long word against a constraint
 */
public interface LongOperator {
    /**
     * @param l
     * @return true if there is a test.
     */
    public boolean match(long l);

    public void initForPass();
}
