package ht.util.core.longword.opers;

/**
 * Or collection
 */

public class LogicalOrOperator extends LogicalWordOperatorCollection {

    public LogicalOrOperator() {

    }

    public LogicalOrOperator(LongOperator... constraints) {
        super(constraints);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OR(");
        boolean comma = false;
        for (LongOperator constraint : m_constraints) {
            if (comma == true) {
                builder.append(",");
            } else {
                comma = true;
            }
            builder.append(constraint.toString());

        }
        builder.append(")");
        return builder.toString();
    }

    public boolean match(long field) {
        for (LongOperator constraint : m_constraints) {
            if (constraint.match(field)) {
                return true;
            }
        }
        return false;
    }
}