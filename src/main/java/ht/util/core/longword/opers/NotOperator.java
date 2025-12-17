package ht.util.core.longword.opers;

/**
 * Not of an operator
 */
public class NotOperator implements LongOperator {
    private LongOperator oper;

    public NotOperator(LongOperator oper) {
        this.oper = oper;
    }

    /**
     * @param l
     * @return true if there is a test.
     */
    public boolean match(long l) {
        return !oper.match(l);
    }

    public void initForPass() {

    }
}
