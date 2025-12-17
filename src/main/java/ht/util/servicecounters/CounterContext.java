package ht.util.servicecounters;

import ht.util.core.map.MapUtil;
import ht.util.core.string.Fmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class CounterContext {
    private static CounterContext context = new CounterContext();
    private int[] cascade = new int[]{1, 5, 12, 24, 7, 31, 12};
    private int[] clock = new int[cascade.length];
    private String[] cascadeString = new String[]{"minute", "5 mins", "hour", "day", "week", "month(31)", "year"};
    private String[] priorCascadeString = new String[]{"prior minute", "prior 5 mins", "prior hour", "prior day", "prior week", "prior month(31)", "prior year"};
    private Class[] cascadeClass = new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class};
    private TreeMap<String, CounterSet> counters = new TreeMap();

    private TreeMap<String, Register> registers = new TreeMap();
    private List<Register> regs = new ArrayList();

    private long ticks = 0;

    private CounterContext() {
    }

    public static CounterContext getContext() {
        return context;
    }

    public ColumnSet getColumnSet(String args) {
        return new ColumnSet(args, this);
    }

    public long getTicks() {
        return ticks;
    }

    public List<Register> getAll() {
        return regs;
    }

    public void addRegister(CounterSet cs, Register r) {
        registers.put(Fmt.S("%s.%s", cs.getName(), r.getName()), r);
        regs.add(r);
    }

    public Register getRegister(String s) {
        return registers.get(s);
    }

    public Map<String, Register> getRegisters(String startsWith) {
        TreeMap tm = new TreeMap();
        MapUtil.extractPropertySubMap(registers, startsWith, tm);
        return tm;
    }

    public void clock() {
        ticks++;
        for (int i = 0; i < clock.length; i++) {
            clock[i]++;
            if (clock[i] > cascade[i]) {
                clock[i] = 0;
            } else {
                for (CounterSet cs : counters.values()) {
                    cs.clock(i);
                }
                return;
            }
        }

    }

    public CounterSet getSet(String key) {
        return counters.get(key);
    }

    public void addCounterSet(CounterSet set) {
        counters.put(set.getName(), set);
    }

    public int getRawRegisterCount() {
        return cascadeString.length;
    }

    public int[] getCascadingSeconds() {
        return cascade;
    }

    public String[] getCascadingSecondsNames(boolean prior) {
        if (prior) {
            return priorCascadeString;
        }
        return cascadeString;
    }


    public Class[] getCascadingClasses() {
        return cascadeClass;
    }

}
