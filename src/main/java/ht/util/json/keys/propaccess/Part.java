package ht.util.json.keys.propaccess;


public class Part {
    protected String name;

    public Part(String name) {
        this.name = name;
    }

    public static Part get(String name, boolean isIndexed) {
        if (isIndexed) {
            return new IndexedPart(name);
        }
        return new Part(name);
    }

    public int compare(Part p) {
        if (isIndexed() != p.isIndexed()) {
            return -1;
        }
        int v = name.compareTo(p.name);
        if (v != 0) {
            return v;
        }
        if (isIndexed()) {
            return getIndexPosition() - p.getIndexPosition();
        }
        return 0;
    }

    public boolean isNull() {
        return false;
    }

    public void setIndex(int index) {

    }

    public String getIndexAsValue() {
        return null;
    }

    public String name() {
        return name;
    }

    public Part clone() {
        Part p = get(name, isIndexed());
        if (isIndexed()) {

            p.setValue(getIndexAsValue());
        }
        return p;
    }

    public String toString() {
        return name;
    }

    public boolean hasName() {
        return name.length() > 0;
    }

    public void setValue(String val) {
    }


    public int getIndexPosition() {
        return -1;
    }

    public boolean isNumeric() {
        return false;
    }

    public boolean isIndexed() {
        return false;
    }

    void append(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(".");
        }
        builder.append(name);
    }
}
