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
package com.hitorro.util.basefile.filters;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.core.opers.LogicalOrOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to construct an AND or OR file filter set.
 * <p>
 * Simply construct and then chain calls together
 */
public class FileFilterCollection {
    private List<HTPredicate<BaseFile>> col = new ArrayList();

    public HTPredicate<BaseFile> and() {
        if (col.size() == 0) {
            return null;
        }
        if (col.size() == 1) {
            return col.get(0);
        }
        LogicalAndOperator lao = new LogicalAndOperator();
        lao.add(col);
        return lao;
    }

    public HTPredicate<BaseFile> or() {
        if (col.size() == 0) {
            return null;
        }
        if (col.size() == 1) {
            return col.get(0);
        }
        LogicalOrOperator lao = new LogicalOrOperator();
        lao.add(col);
        return lao;
    }

    public FileFilterCollection isDir() {
        col.add(IsDir.isDir);
        return this;
    }

    public FileFilterCollection notDir() {
        col.add(IsDir.notDir);
        return this;
    }

    public FileFilterCollection hasExt(String ext) {
        col.add(new FileExtension(ext, true));
        return this;
    }

    public FileFilterCollection notExt(String ext) {
        col.add(new FileExtension(ext, true).not());
        return this;
    }

    public FileFilterCollection nameContains(String ext) {
        col.add(new FileNameContains(ext, true));
        return this;
    }

    public FileFilterCollection notContains(String ext) {
        col.add(new FileNameContains(ext, true).not());
        return this;
    }

    public FileFilterCollection endsWith(String ends) {
        col.add(new FileEndsWith(ends, true));
        return this;
    }

    public FileFilterCollection notEndWith(String ends) {
        col.add(new FileEndsWith(ends, true).not());
        return this;
    }
}
