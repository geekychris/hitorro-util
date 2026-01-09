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

/**
 * Utility class to hold the annotations on a class, including method annotations. In addition the class holds
 * information about "fields" and field annotations. User: chris Date: Oct 13, 2006 Time: 11:54:26 AM
 */
public class ClassAnnotations {
    private Class m_c;
    private List<Annotation> classLevelAnnotation = new ArrayList<Annotation>();
    private List<MethodAnnotation> methodAnno = new ArrayList<MethodAnnotation>();
    private ClassAnnotations contraintAnnotation = null;
    private List<MemberVarAnnotations> memberVarAnnos = new ArrayList<MemberVarAnnotations>();
    private List<BeanStyleMethodAnnotation> beanStyleMethodAnnotations;
    private Map<Class, Class> m_classLevelAnnotationIdentityMap = null;

    public ClassAnnotations(Class c) {
        init(c, null);
    }

    public ClassAnnotations(Class c, ClassAnnotations contraintAnnotation) {
        init(c, contraintAnnotation);
    }

    public static Map<Class, Class> getClassIdentityMap(List<Annotation> anos) {
        Map<Class, Class> map = new HashMap<Class, Class>();
        for (Annotation a : anos) {
            map.put(a.getClass(), a.getClass());
        }
        return map;
    }

    public Annotation getMatchingClass(List<Annotation> list, Class c) {
        for (Annotation a : list) {
            Class ac = a.annotationType();
            if (ac.equals(c)) {
                return a;
            }
        }
        return null;
    }

    public List<BeanStyleMethodAnnotation> getMatchingClass(Class c) {
        List<BeanStyleMethodAnnotation> list = new ArrayList();
        for (BeanStyleMethodAnnotation fa : beanStyleMethodAnnotations) {

            if (fa.hasAnnotation(c)) {
                list.add(fa);
            }
        }
        return list;
    }

    public List<MemberVarAnnotations> getAllMembersWithAnnotation(Class anoClass) {
        List<MemberVarAnnotations> list = new ArrayList();
        for (MemberVarAnnotations mva : memberVarAnnos) {
            if (mva.containsAnnotation(anoClass)) {
                list.add(mva);
            }
        }
        return list;
    }

    /**
     * Get the actual annotations objects that were placed on the class.
     *
     * @return a listFiles of Annotations
     */
    public List<Annotation> getClassAnnotations() {
        return classLevelAnnotation;
    }

    /**
     * Get our MethodAnnotation holders for all the methods in the class. To get the actual annotations on the methods,
     * access the MethodAnnotation objects.
     *
     * @return a listFiles of MethodAnnotations
     */
    public List<MethodAnnotation> getMethodAnnotations() {
        return methodAnno;
    }

    /**
     * Get the BeanStyleMethodAnnotation holders for the "fields" in the class. To get the annotations, field names and
     * methods, access the BeanStyleMethodAnnotation object.
     */
    public List<BeanStyleMethodAnnotation> getBeanStyleMethodAnnotations() {
        return beanStyleMethodAnnotations;
    }

    /**
     * get all the annotation for the class and the method
     */
    public void init(Class c) {
        init(c, null);
    }

    public void init(Class c, ClassAnnotations contraintAnnotation) {
        m_c = c;
        MemberVarAnnotations.addToListIfHasAnnotationFromClass(c, memberVarAnnos);
        this.contraintAnnotation = contraintAnnotation;

        // load class level annotation.  If a constraint was
        ClassAnoUtil.loadAnnotation(classLevelAnnotation, c.getAnnotations(),
                getConstraintClassLevelAnnotationIdentityMap());

        // now load the annotations for the methods
        Method[] methods = c.getMethods();
        Map<Class, Class> methodConstraint = getMethodConstraint();

        Map<String, BeanStyleMethodAnnotation> fieldAnnotationMap = new HashMap<String, BeanStyleMethodAnnotation>();
        for (Method m : methods) {
            if (!ClassAnoUtil.addFieldMethod(fieldAnnotationMap, m, methodConstraint)) {
                // this is a regular non-field method, do a regular method annotation
                MethodAnnotation ma = new MethodAnnotation(m, methodConstraint);
                if (ma.getAnnotations() != null && ma.getAnnotations().size() > 0) {
                    // has method with annotation
                    methodAnno.add(ma);
                }
            }
        }

        // pull the field annotations out into a listFiles
        beanStyleMethodAnnotations = new ArrayList<BeanStyleMethodAnnotation>();
        beanStyleMethodAnnotations.addAll(fieldAnnotationMap.values());
    }

    private Map<Class, Class> getMethodConstraint() {
        if (this.contraintAnnotation != null) {
            List<MethodAnnotation> anos = this.contraintAnnotation.getMethodAnnotations();
            if (anos != null && anos.size() > 0) {
                MethodAnnotation ma = anos.get(0);
                return ma.getClassIdentityMap();
            }
        }
        return null;
    }

    private Map<Class, Class> getConstraintClassLevelAnnotationIdentityMap() {
        if (this.contraintAnnotation != null) {
            return this.contraintAnnotation.getClassLevelAnnotationIdentityMap();
        }
        return null;
    }

    /**
     * Method used by annotation constraint instance
     *
     * @return
     */
    private Map<Class, Class> getClassLevelAnnotationIdentityMap() {
        if (m_classLevelAnnotationIdentityMap == null) {
            if (this.contraintAnnotation == null) {
                // constraint object is me!
                m_classLevelAnnotationIdentityMap = getClassIdentityMap(getClassAnnotations());
            }
        }
        return m_classLevelAnnotationIdentityMap;
    }
}
