/*
    Copyright (c) 2003 - present HiTorro All rights reserved.


    User: chris
*/

package ht.util.core.classes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holder of annotations and methods for a "field". A field is an abstraction on top of standard bean-like setters and
 * getters in a class. User: chris Date: Oct 23, 2006 Time: 9:27:44 PM
 */
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
        for (Annotation a : m_anno) {
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
        if (m_anno.size() == 0) {
            // this is the first pass (or no previous annotations) - just build the listFiles
            ClassAnoUtil.loadAnnotation(m_anno, meth.getAnnotations(), constraint);
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
            setterList = m_anno;
        } else {
            getterList = m_anno;
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

        m_anno = new ArrayList<Annotation>();
        m_anno.addAll(tempMap.values());
    }
}
