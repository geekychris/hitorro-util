package ht.util.core.iterator.mappers;

import ht.util.core.Constants;

/**
 * Basic object to a vector of strings mapper.  Subclass me to handle the appropriate accessor function.
 */
public abstract class Object2RowVectorMapper<E> extends BaseMapper<E, String[]> {
    protected String[] keys;
    protected String[] target;
    protected int size;

    public Object2RowVectorMapper(String[][] tuples) {
        size = tuples.length;
        keys = new String[size];
        target = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = tuples[i][0];
            target[i] = tuples[i][1];
        }
    }

    public Object2RowVectorMapper(String[] keys, String[] targets) {
        size = keys.length;
        this.keys = keys;
        this.target = targets;
    }

    public String[] getColumnNames() {
        return target;
    }

    @Override
    public String[] apply(final E e) {
        String result[] = new String[size];
        for (int i = 0; i < size; i++) {
            Object o = get(keys[i], e);
            if (o != null) {
                result[i] = o.toString();
            } else {
                result[i] = Constants.EmptyString;
            }
        }
        return result;
    }

    public abstract Object get(String field, E e);
}

