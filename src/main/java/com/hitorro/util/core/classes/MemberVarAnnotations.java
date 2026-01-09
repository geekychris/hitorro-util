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
package com.hitorro.util.core.classes;

import com.hitorro.util.core.ArrayUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 */
public class MemberVarAnnotations extends BaseAnnotation {
    private Field f;

    public MemberVarAnnotations(Field f, Annotation anos[]) {
        this.f = f;
        f.setAccessible(true);
        for (Annotation a : anos) {
            anno.add(a);
        }
    }

    public static void addToListIfHasAnnotation(Field f, List<MemberVarAnnotations> list) {
        Annotation anos[] = f.getAnnotations();
        if (!ArrayUtil.nullOrEmpty(anos)) {
            list.add(new MemberVarAnnotations(f, anos));
        }
    }

    public static void addToListIfHasAnnotationFromClass(Class c, List<MemberVarAnnotations> list) {
        for (Field f : c.getFields()) {
            addToListIfHasAnnotation(f, list);
        }
    }

    public Object getValue(Object o) throws IllegalAccessException {
        return f.get(o);
    }

    public Field getField() {
        return f;
    }
}
