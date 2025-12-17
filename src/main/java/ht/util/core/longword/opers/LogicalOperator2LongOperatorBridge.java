package ht.util.core.longword.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;

/**
 * Base class for interfacing LongOperator's with standard HTPredicate's
 * <p>
 * Implement a subclass that provides an implementation of  #getLong
 **/
public abstract class LogicalOperator2LongOperatorBridge<T> implements HTPredicate<T> {
    private LongOperator longOper;

    public LogicalOperator2LongOperatorBridge(LongOperator longOper) {
        this.longOper = longOper;
    }

    /**
     * Implement this method to provide the long value to compute the Long operator test against
     *
     * @param t
     * @return
     */
    public abstract long getLong(T t);

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final T t) {
        return longOper.match(getLong(t));
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }

}

