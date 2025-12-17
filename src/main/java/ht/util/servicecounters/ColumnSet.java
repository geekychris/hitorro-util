package ht.util.servicecounters;

import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
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
