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
package com.hitorro.util.servicecounters;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ColumnSet {
    private List<Column> list = new ArrayList();
    private Column[] columns = null;
    private StringBuilder sb = new StringBuilder();

    public ColumnSet(String vals, CounterContext cc) {
        String ps[] = StringUtil.tokenizeFromSingleChar(vals, ",", true);
        for (String p : ps) {
            Register r = cc.getRegister(p);
            if (r != null) {
                list.add(new Column(p.toLowerCase(), r));
            } else {
                Map<String, Register> map = cc.getRegisters(p);
                for (String key : map.keySet()) {
                    String fullKey = Fmt.S("%s.%s", p, key);
                    r = cc.getRegister(fullKey);
                    list.add(new Column(fullKey.toLowerCase(), r));
                }
            }
        }
    }

    public void finalizeIt() {
        if (columns != null) {
            return;
        }

        columns = new Column[list.size()];
        columns = list.toArray(columns);
    }

    public void renderHeader(PrintWriter pw) {
        finalizeIt();
        pw.print("time");
        for (Column col : columns) {
            pw.print(",");
            pw.print(col.getColName());
        }
        pw.println();
        pw.flush();
    }

    public void renderRow(PrintWriter pw) {
        sb.setLength(0);
        sb.append(System.currentTimeMillis());
        for (Column col : columns) {
            sb.append(",");
            String s = col.getValue();
            sb.append(s);
        }
        pw.println(sb.toString());
    }
}

class Column {
    private String colName;
    private Register reg;

    public Column(String colName, Register reg) {
        this.colName = colName;
        this.reg = reg;
    }

    public String getColName() {
        return colName;
    }

    public String getValue() {
        return reg.getValue();
    }
}
