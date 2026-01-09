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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanStyleMethodAnnotation extends BaseAnnotation {
    private String _name;
    private Method _setter;
    private Method _getter;
    private Class _type;

    public BeanStyleMethodAnnotation(String fieldName,
                                     Method meth,
                                     boolean isGetter,
                                     Class fieldType,
                                     Map<Class, Class> constraint) {
        _name = fieldName;
        _type = fieldType;
        addMethod(isGetter, meth, constraint);
    }

    public boolean hasAnnotation(Class c) {
        for (Annotation a : anno) {
            if (a.annotationType().equals(c)) {
                return true;
            }
        }
        return false;
    }

    public String getFieldName() {
        return _name;
    }

    public Method getGetterMethod() {
        return _getter;
    }

    public Method getSetterMethod() {
        return _setter;
    }

    public Class getFieldType() {
        return _type;
    }

    public void addMethod(boolean isGetter, Method meth, Map<Class, Class> constraint) {
        if (isGetter) {
            _getter = meth;
        } else {
            _setter = meth;
        }

        // get the annotations for the field (from the method) and apply them into previous annotations, if any
        // if there are collisions, annotations from the getter have precedence
        if (anno.size() == 0) {
            // this is the first pass (or no previous annotations) - just build the listFiles
            ClassAnoUtil.loadAnnotation(anno, meth.getAnnotations(), constraint);
        }

        // need to worry about merging

        List<Annotation> annos = new ArrayList<Annotation>();
        ClassAnoUtil.loadAnnotation(annos, meth.getAnnotations(), constraint);
        if (annos.size() == 0) {
            // no new annotations, we're done
            return;
        }

        // figure out which listFiles is the setters and which is the getters, to establish priority
        List<Annotation> getterList;
        List<Annotation> setterList;
        if (isGetter) {
            getterList = annos;
            setterList = anno;
        } else {
            getterList = anno;
            setterList = annos;
        }

        // put the setters into a apply, keyed by class, then clobber with getters
        // and pull out the resulting values as the final listFiles
        Map<Class, Annotation> tempMap = new HashMap<Class, Annotation>();
        for (Annotation sa : setterList) {
            tempMap.put(sa.getClass(), sa);
        }
        for (Annotation ga : getterList) {
            tempMap.put(ga.getClass(), ga);
        }

        anno = new ArrayList<Annotation>();
        anno.addAll(tempMap.values());
    }
}
