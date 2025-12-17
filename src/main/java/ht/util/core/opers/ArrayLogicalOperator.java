package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Given an array of some object.  see if any or all of its values test the embedded logical operator.
 */
public class ArrayLogicalOperator<F> implements HTPredicate<Object[]> {
    private HTPredicate<F> op;
    private Mode mode;

    public ArrayLogicalOperator(HTPredicate<F> op, Mode mode) {
        this.op = op;
        this.mode = mode;
    }

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final Object e[]) {
        switch (mode) {
            case Any:
                for (Object o : e) {
                    if (op.test((F) o)) {
                        return true;
                    }
                }
                return false;
            case All:
                for (Object o : e) {
                    if (!op.test((F) o)) {
                        return false;
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }

    public enum Mode {
        All, Any
    }

}
