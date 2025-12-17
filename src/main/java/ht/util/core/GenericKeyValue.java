package ht.util.core;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class GenericKeyValue<K, V> implements ColumnAccessor<Object> {
    private K m_k;
    private V m_v;

    public GenericKeyValue(K k, V v) {
        m_k = k;
        m_v = v;
    }

    public K getKey() {
        return m_k;
    }

    public void setKey(K k) {
        this.m_k = k;
    }

    public V getValue() {
        return m_v;
    }

    public void setValue(V v) {
        this.m_v = v;
    }

    @Override
    public Object getElement(final int i) {
        if (i == 0) {
            return m_k;
        }
        return m_v;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }
}
