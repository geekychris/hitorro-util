package ht.util.core.longword.opers;


/**
 *
 */

public abstract class LogicalWordOperatorCollection implements LongOperator {
    protected LongOperator m_constraints[];

    public LogicalWordOperatorCollection(LongOperator... constraints) {
        m_constraints = constraints;
    }


    public void initForPass() {
        for (LongOperator oper : m_constraints) {
            oper.initForPass();
        }
    }

    public abstract boolean match(long field);
}