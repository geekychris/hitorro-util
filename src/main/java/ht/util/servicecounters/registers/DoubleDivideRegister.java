package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 *
 */
public class DoubleDivideRegister extends Register {
    Register dividend;
    Register divisor;

    public DoubleDivideRegister(CounterSet cs, String name, String description, Register dividend, Register divisor) {
        super(cs, name, description);
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public void clock(int depth) {
        // do nothing
    }

    public double getAsDouble(boolean prior, int i) {
        return dividend.getAsDouble(prior, i) / divisor.getAsDouble(prior, i);
    }

    public long getAsLong(boolean prior, int i) {
        return (long) getAsDouble(prior, i);
    }

    public String getAsString(boolean prior, int i) {
        return Double.toString(getAsDouble(prior, i));
    }

    public double getAsDouble() {
        return dividend.getAsDouble() / divisor.getAsDouble();
    }

    public long getAsLong() {
        return (long) (dividend.getAsDouble() / divisor.getAsDouble());
    }

    public String getValue() {
        return Double.toString(dividend.getAsDouble() / divisor.getAsDouble());
    }
}
