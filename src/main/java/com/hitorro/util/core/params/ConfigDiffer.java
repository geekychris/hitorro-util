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
package com.hitorro.util.core.params;

import com.hitorro.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: chris
 */
public class ConfigDiffer {
    Iterator<String> oldIter;
    Iterator<String> newIter;
    String oldKey;
    String newKey;
    boolean of = true;
    boolean nf = true;
    List<ConfigChange> changes = new ArrayList();

    public Iterator<ConfigChange> getDiffs(HTProperties old, HTProperties newC) {
        changes.clear();
        oldIter = old.getMap().keySet().iterator();
        newIter = newC.getMap().keySet().iterator();

        advanceOld();
        advanceNew();
        while (of && nf) {
            int c = oldKey.compareTo(newKey);
            if (c == 0) {
                // equal key, ensure same values
                String oldVals = old.get(oldKey);
                String newVals = newC.get(newKey);
                if (!StringUtil.equals(oldVals, newVals, false)) {
                    changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Updated, oldKey));
                }

                advanceOld();
                advanceNew();
            } else if (c < 0) {
                changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Deleted, oldKey));
                advanceOld();
            } else {
                changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Added, newKey));
                advanceNew();
            }

        }

        while (of) {
            changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Deleted, oldKey));
            advanceOld();
        }

        while (nf) {
            changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Added, newKey));
            advanceNew();
        }

        return changes.iterator();
    }

    private void advanceOld() {
        if (of == false) {
            return;
        }
        if (oldIter.hasNext()) {
            oldKey = oldIter.next();
            of = !StringUtil.nullOrEmptyString(oldKey);
        } else {
            of = false;
        }
    }

    private void advanceNew() {
        if (nf == false) {
            return;
        }
        if (newIter.hasNext()) {
            newKey = newIter.next();
            nf = !StringUtil.nullOrEmptyString(newKey);
        } else {
            nf = false;
        }
    }


}
