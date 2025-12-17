package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterContext;
import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 *
 */
public class DoubleRegister extends Register {
    private volatile double[] registers;
    private volatile double[] registersPrior;
    private volatile double register;

    public DoubleRegister(CounterSet cs, final String name, String description) {
        super(cs, name, description);
        registers = new double[CounterContext.getContext().getRawRegisterCount()];
        registersPrior = new double[CounterContext.getContext().getRawRegisterCount()];
    }

    public void clock(int depth) {
        for (int i = 0; i <= depth; i++) {
            registers[i + 1] += registers[i];
            registersPrior[i] = registers[i];
            registers[i] = 0;
        }
    }

    public void incrementBy(double v) {
        registers[0] += v;
        register += v;
    }

    public void decrementBy(double v) {
        registers[0] -= v;
        register -= v;
    }

    public void setTo(double v) {
        registers[0] = v;
        register = v;
    }

    public double getAsDouble(boolean prior, int i) {
        if (prior) {
            return registersPrior[i];
        }
        return registers[i];
    }

    public long getAsLong(boolean prior, int i) {
        if (prior) {
            return (long) registersPrior[i];
        }
        return (long) registers[i];
    }

    public String getAsString(boolean prior, int i) {
        if (prior) {
            return Double.toString(registersPrior[i]);
        }
        return Double.toString(registers[i]);
    }

    public double getAsDouble() {
        return register;
    }

    public long getAsLong() {
        return (long) register;
    }

    public String getValue() {
        return Double.toString(register);
    }
}
