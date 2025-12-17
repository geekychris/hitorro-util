package ht.util.core.longword.opers;

/**
 * And operator
 */
public class LogicalAndOperator extends LogicalWordOperatorCollection {

    public LogicalAndOperator() {

    }

    public LogicalAndOperator(LongOperator... constraints) {
        super(constraints);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AND(");
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
            if (!constraint.match(field)) {
                return false;
            }
        }
        return true;
    }
}
