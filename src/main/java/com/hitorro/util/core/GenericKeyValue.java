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
package com.hitorro.util.core;


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
