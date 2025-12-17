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
package com.hitorro.util.basefile.tools;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.jarfile.JarItemIterator;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.string.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper to get the chaining iterator that is appropriate to the file extension of the base file
 * <p/>
 * Subclass this guy to provide the chainingiterator and its file extension
 */
public abstract class BaseFileMapperMapper<E> extends BaseMapper<BaseFile, AbstractIterator<E>> {
    private Map<String, BaseMapper<BaseFile, AbstractIterator<E>>> map = new HashMap();
    private boolean followJar;

    public BaseFileMapperMapper(boolean followJar) {
        this.followJar = followJar;
        BaseMapper<BaseFile, AbstractIterator<E>> m[] = getMappers();
        String ext[] = getExtensions();

        for (int i = 0; i < m.length; i++) {
            map.put(ext[i], m[i]);
        }
    }

    public abstract BaseMapper<BaseFile, AbstractIterator<E>>[] getMappers();

    public abstract String[] getExtensions();

    @Override
    public AbstractIterator<E> apply(final BaseFile e) {
        String ext = e.getFileExtension(true);
        if (StringUtil.nullOrEmptyString(ext)) {
            return null;
        }
        ext = ext.toLowerCase();
        if ("jar".equalsIgnoreCase(ext)) {
            return new JarItemIterator(e).nest(this);
        }
        BaseMapper<BaseFile, AbstractIterator<E>> mapper = map.get(ext);

        return mapper.apply(e);
    }
}
