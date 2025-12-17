/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ht.util.servicecounters;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.ArrayUtil;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.StringProperty;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
@CommandDef(command = "counters.print",
        description = "Print system counters")
public class PrintCounters extends Command {
    @CommandArgument(required = false)
    private StringProperty PrefixKey = new StringProperty("prefix", "Prefix search based constraint of all counters", "");
    @CommandArgument(required = true)
    private BooleanProperty PriorKey = new BooleanProperty("prior", "Look to the prior counter values or current (current are a fraction of their total time)", false);
    /**
     * Dont use auto generation here due to the dynamic nature of the response shape.
     */
    private ResponseShape shape;
    private ResponseShape shapePrior;
    private int cols;

    public void setFromService(Object o) {

    }

    public void init() {
        shape = initShape(false);
        shapePrior = initShape(true);
    }

    private ResponseShape initShape(boolean prior) {
        ResponseShape s = new ResponseShape(getCommand(), "document");
        String[] headers = ArrayUtil.mergeArrays(String.class, new String[]{"CounterName", "aggregate"}, CounterContext.getContext().getCascadingSecondsNames(prior));
        s.addHeader(headers);
        s.addHeaderShortNames(headers);
        Class[] classes = ArrayUtil.mergeArrays(Class.class, new Class[]{String.class}, CounterContext.getContext().getCascadingClasses());
        cols = CounterContext.getContext().getCascadingSecondsNames(prior).length;
        s.addRowTypes(classes);
        return s;
    }

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        long ticks = CounterContext.getContext().getTicks();

        String pref = PrefixKey.apply(args);
        boolean prior = PriorKey.apply(args);
        Collection<Register> col;
        if (StringUtil.nullOrEmptyString(pref)) {
            col = CounterContext.getContext().getAll();
        } else {
            Map<String, Register> m = CounterContext.getContext().getRegisters(pref);
            col = m.values();
        }

        if (prior) {
            response.setResponseShape(shapePrior);
        } else {
            response.setResponseShape(shape);
        }
        for (Register r : col) {
            String[] row = new String[cols + 2];
            row[0] = r.getFullName();
            row[1] = r.getValue();
            if (r.isCascading()) {
                for (int i = 0; i < cols; i++) {
                    row[i + 2] = r.getAsString(prior, i);
                }
            }

            response.addRowArray(row);
        }
        response.end();
        return false;
    }
}
