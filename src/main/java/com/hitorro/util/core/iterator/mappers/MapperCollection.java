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
package com.hitorro.util.core.iterator.mappers;

import com.hitorro.util.core.iterator.Mapper;

/**
 * Used to chain a set of mappers together to avoid chaining a whole bunch of mapping iterators together.  This is done
 * at the expense of loosing the type contract verification provided by the generics E, F
 */
public class MapperCollection<I, O> extends BaseMapper<I, O> {
    protected BaseMapper maps[];

    public MapperCollection(BaseMapper maps[]) {
        this.maps = maps;
    }

    private MapperCollection(MapperCollection mc) {
        maps = new BaseMapper[mc.maps.length];
        for (int i = 0; i < mc.maps.length; i++) {
            maps[i] = mc.maps[i].getCopy();
        }
    }

    public MapperCollection(BaseMapper left, BaseMapper right) {
        this.maps = new BaseMapper[2];
        maps[0] = left;
        maps[1] = right;
    }

    public boolean isThreadSafe() {
        for (BaseMapper map : maps) {
            if (!map.isThreadSafe()) {
                return false;
            }
        }
        return true;
    }

    public BaseMapper getCopy() {
        return new MapperCollection(this);
    }

    public O apply(final I e) {
        Object t = e;
        for (Mapper mapper : maps) {
            t = mapper.apply(t);
        }
        return (O) t;
    }

    public <E> MapperCollection<I, E> combine(BaseMapper<O, E> r) {
        BaseMapper arr[];
        if (r instanceof DummyBaseMapper) {
            return (MapperCollection<I, E>) this;
        }
        if (r instanceof MapperCollection) {
            MapperCollection right = (MapperCollection) r;
            arr = new BaseMapper[maps.length + right.maps.length];
            fill(arr, maps, 0);
            fill(arr, right.maps, maps.length);
        } else {
            arr = new BaseMapper[maps.length + 1];
            fill(arr, maps, 0);
            arr[maps.length] = r;
        }
        return new MapperCollection(arr);

    }

    private void fill(Mapper target[], Mapper src[], int offset) {
        for (int i = 0; i < src.length; i++) {
            target[offset + i] = src[i];
        }
    }
}
