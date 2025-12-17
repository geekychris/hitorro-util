package ht.util.json.keys.propaccess;

import ht.util.core.IntegerUtil;
import ht.util.core.string.Fmt;

public class IndexedPart extends Part {
    private String val;
    private boolean isNum;
    private int index;

    public IndexedPart(final String name) {
        super(name);
    }

    public void setIndex(int index) {
        this.index = index;
        this.isNum = true;
    }

    public Part clone() {
        IndexedPart i = new IndexedPart(name);
        i.val = val;
        i.isNum = isNum;
        i.index = index;
        return i;
    }

    public void setValue(String val) {
        this.val = val;
        isNum = IntegerUtil.isNumber(val);
        if (isNum) {
            index = IntegerUtil.parseInt(val);
        }
    }

    public String getIndexAsValue() {
        return val;
    }

    public int getIndexPosition() {
        return index;
    }

    public boolean isNumeric() {
        return isNum;
    }

    public boolean isIndexed() {
        return true;
    }

    public boolean isNull() {
        if (isNum) {
            return false;
        }
        return val == null || val.length() == 0;
    }

    void append(StringBuilder builder) {
        super.append(builder);
        builder.append('[');
        if (isNum) {
            builder.append(index);
        } else {
            builder.append(val);
        }

        builder.append(']');
    }

    public String toString() {
        if (isNum) {
            return Fmt.S("%s[%s]", name, index);
        }
        return Fmt.S("%s[%s]", name, val);
    }
}
