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
package com.hitorro.util.core.events.cache;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class PoolContainer<K, V extends PooledObjectIntf> {
    public static final BooleanProperty TrackWhereFrom = new BooleanProperty("debug.trackpool", "", true);

    protected K key;
    private int maxElements;
    private int elems = 0;
    private Object notifier = new Object();
    private Stack<V> stack = new Stack();
    private boolean trackAllocation = false;
    private long timeWhenTaken = 0;
    private Map<V, Stats<V>> inUseSet = new HashMap();

    private PooledObjectCache<K, V> poc;
    private int generation;


    public PoolContainer(K key, int maxElements, PooledObjectCache<K, V> poc, int generation) {
        this.key = key;
        this.maxElements = maxElements;
        this.poc = poc;
        this.generation = generation;
        this.trackAllocation = TrackWhereFrom.apply();
    }

    public V get() {
        synchronized (notifier) {
            try {
                while (stack.isEmpty()) {
                    if (maxElements > elems) {
                        // didnt hit max elements, expand
                        V ret = getNewPoolElement();
                        if (ret == null) {
                            return null;
                        }
                        ret.setGenerationId(generation);
                        elems++;

                        if (trackAllocation) {
                            inUseSet.put(ret, new Stats<V>(ret));
                        }
                        ret.setPoolContainer(this);
                        return ret;
                    }
                    notifier.wait();
                }
                V ret = stack.pop();
                inUseSet.put(ret, new Stats<V>(ret));
                ret.setPoolContainer(this);
                return ret;
            } catch (InterruptedException e) {
            }

        }
        return null;
    }

    public ArrayNode getDebugInfo() {
        ArrayNode an = JsonNodeFactory.instance.arrayNode();
        for (Stats<V> v : inUseSet.values()) {
            long t = v.getTimeTaken();
            ObjectNode on = JsonNodeFactory.instance.objectNode();
            on.put("timeTaken", t);
            on.set("stack", StringUtil.getCallstackAsJsonArray(v.t));
            on.put("key", v.key.toString());
            on.put("threadName", v.threadName);
            an.add(on);
        }
        return an;
    }

    public long takenForNMillis() {
        if (timeWhenTaken == 0) {
            return -1;
        }
        return System.currentTimeMillis() - timeWhenTaken;
    }

    protected V getNewPoolElement() {
        return poc.getValue(key);
    }

    /**
     * Handle returning of an element to the free pool.
     *
     * @param v
     */
    public void returnIt(V v) {
        if (v == null) {
            return;
        }
        synchronized (notifier) {
            if (v.getGenerationId() != generation) {
                // see if we can return a previously pooled object initialized pre-generation change
                if (!v.reInit(key)) {
                    returnItCleanup(v);
                    return;
                }
            }
            returnItCleanup(v);
        }
    }

    private void returnItCleanup(V v) {
        timeWhenTaken = 0;
        v.passivate();
        inUseSet.remove(v);
        stack.add(v);
        notifier.notifyAll();
    }

}

class Stats<V extends PooledObjectIntf> {

    public V key;
    public Throwable t;
    public long whenTaken;
    public String threadName;

    public Stats(V key) {
        setKey(key);
        threadName = Thread.currentThread().getName();
    }

    public boolean equals(Object o) {
        return key.equals(o);
    }

    public int hashCode() {
        return key.hashCode();
    }

    public long getTimeTaken() {
        return System.currentTimeMillis() - whenTaken;
    }

    public void setKey(V key) {
        this.key = key;
        whenTaken = System.currentTimeMillis();
        t = new Throwable();
    }

    public String toString() {
        return Fmt.S("%s %s %e", key, getTimeTaken(), t);
    }
}