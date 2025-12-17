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
package com.hitorro.jsontypesystem.propreaders;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.params.ConfigChange;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BaseMappingProperty;

import java.util.*;

public class JVSConfigChangeRegistry {
    private static JVSConfigChangeRegistry registry = new JVSConfigChangeRegistry();
    private TreeMap<String, List<JVSConfigChangeObserver>> observers = new TreeMap();

    public static JVSConfigChangeRegistry getRegistry() {
        return registry;
    }

    public void addObserver(BaseMappingProperty pk, JVSConfigChangeObserver cco) {
        addObserver(pk.getKey(), cco);
    }

    /**
     * Number of observers that are registered.
     */
    public int getSize() {
        return observers.size();
    }

    public void addObserver(String path, JVSConfigChangeObserver cco) {
        List<JVSConfigChangeObserver> l = observers.get(path);
        if (l == null) {
            l = new ArrayList();
            observers.put(path, l);
        }
        l.add(cco);
    }

    public void removeObserver(BaseMappingProperty pk, JVSConfigChangeObserver cco) {
        removeObserver(pk.getKey(), cco);
    }

    public void removeObserver(String path, JVSConfigChangeObserver cco) {
        List<JVSConfigChangeObserver> l = observers.get(path);
        if (l == null) {
            return;
        }
        l.remove(cco);
    }

    public int runDiff(JVS old, JVS newC) {
        Map<JVSConfigChangeObserver, JVSConfigChangeObserver> hitMaps = new HashMap();
        AbstractIterator<ConfigChange> iter = new JVSConfigDiffer(old, newC).getDiffs();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        while (iter.hasNext()) {
            ConfigChange curr = iter.next();
            count += enumeratePath(sb, curr, hitMaps);


        }
        // finally notify all that participated with a note that we finished processing.
        notifyEnd(hitMaps);
        return count;
    }

    private int enumeratePath(StringBuilder sb, ConfigChange cc, Map<JVSConfigChangeObserver, JVSConfigChangeObserver> hitMaps) {
        String p[] = StringUtil.tokenizeFromSingleChar(cc.getKey(), ".", false);
        String pOut[] = new String[p.length];
        int count = notifyObserver(p[0], cc, hitMaps);
        for (int i = 1; i < p.length; i++) {
            sb.setLength(0);
            for (int j = 0; j <= i; j++) {
                if (j > 0) {
                    sb.append(".");
                }
                sb.append(p[j]);

            }
            count += notifyObserver(sb.toString(), cc, hitMaps);
        }

        return count;

    }

    private int notifyObserver(String path, ConfigChange cc, Map<JVSConfigChangeObserver, JVSConfigChangeObserver> hitMaps) {
        int count = 0;
        List<JVSConfigChangeObserver> l = observers.get(path);
        if (ListUtil.notNullAndContainsRows(l)) {
            for (JVSConfigChangeObserver o : l) {
                o.change(cc);
                hitMaps.put(o, o);
                count++;
            }
        }
        return count;
    }

    private void notifyEnd(Map<JVSConfigChangeObserver, JVSConfigChangeObserver> hitMaps) {
        for (JVSConfigChangeObserver cco : hitMaps.keySet()) {
            cco.finished();
        }
    }

}



