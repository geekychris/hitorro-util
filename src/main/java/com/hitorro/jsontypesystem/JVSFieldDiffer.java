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
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.diff.GenericDiffer;
import com.hitorro.util.core.diff.GenericDifferCallback;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessComp;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.json.keys.propaccess.PropaccessIterator;

public abstract class JVSFieldDiffer implements GenericDifferCallback<Propaccess> {
    private JVS jvs1, jvs2;

    public JVSFieldDiffer(BaseFile f1, BaseFile f2) throws Exception {
        jvs1 = JVS.read(f1);
        jvs2 = JVS.read(f2);
    }

    public JVSFieldDiffer(JVS jvs1, JVS jvs2) {
        this.jvs1 = jvs1;
        this.jvs2 = jvs2;
    }

    public void executeDiff() {
        GenericDiffer differ = new GenericDiffer();
        PropaccessIterator iter1 = jvs1.getPropertyIter();
        PropaccessIterator iter2 = jvs2.getPropertyIter();
        differ.diff(iter1, iter2, PropaccessComp.comp, this);
    }

    @Override
    public void call(final Propaccess a, final Propaccess b, final GenericDiffer.Mode mode) {
        if (mode == GenericDiffer.Mode.Modify) {
            try {
                JsonNode n1 = jvs1.get(a);
                JsonNode n2 = jvs2.get(b);
                if (!n1.equals(n2)) {
                    changed(a);
                }
            } catch (PropaccessError propaccessError) {
                //
            }


        } else if (mode == GenericDiffer.Mode.Add) {
            added(b);
        } else {
            removed(a);
        }
    }

    public abstract void added(Propaccess path);

    public abstract void removed(Propaccess path);

    public abstract void changed(Propaccess path);
}
