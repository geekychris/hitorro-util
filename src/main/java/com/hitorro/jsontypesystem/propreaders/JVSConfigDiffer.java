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
import com.hitorro.jsontypesystem.JVSFieldDiffer;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.core.params.ConfigChange;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;

public class JVSConfigDiffer extends JVSFieldDiffer {
    private List<ConfigChange> changes = new ArrayList();

    public JVSConfigDiffer(final JVS jvs1, final JVS jvs2) {
        super(jvs1, jvs2);
    }


    @Override
    public void added(final Propaccess path) {
        changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Added, path.toString()));
    }

    @Override
    public void removed(final Propaccess path) {
        changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Deleted, path.toString()));
    }

    @Override
    public void changed(final Propaccess path) {
        changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Updated, path.toString()));
    }

    public AbstractIterator<ConfigChange> getDiffs() {
        changes.clear();
        executeDiff();
        return new CollectionIterator<ConfigChange>(changes);
    }

}
