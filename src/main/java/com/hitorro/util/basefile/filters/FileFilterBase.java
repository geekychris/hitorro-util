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
import com.hitorro.util.core.opers.LogicalNotOperator;

/**
 *
 */
public abstract class FileFilterBase implements HTPredicate<BaseFile> {
    public static HTPredicate<BaseFile> not(HTPredicate<BaseFile> filt) {
        return new LogicalNotOperator(filt);
    }

    public static HTPredicate<BaseFile> isDir() {
        return IsDir.isDir;
    }

    public static HTPredicate<BaseFile> notDir() {
        return IsDir.notDir;
    }

    public static HTPredicate<BaseFile> hasExt(String ext) {
        return new FileExtension(ext, true);
    }

    public static HTPredicate<BaseFile> nameContains(String ext) {
        return new FileNameContains(ext, true);
    }

    public static HTPredicate<BaseFile> endsWith(String ends) {
        return new FileEndsWith(ends, true);
    }

    public HTPredicate<BaseFile> not() {
        return new LogicalNotOperator(this);
    }
}
