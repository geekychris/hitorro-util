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
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.string.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ClassAnoUtil {
    private static final Logger logger = LoggerFactory.getLogger(ClassAnoUtil.class);
    //********************** CLASS ***************************

    /**
     * Get the first annotation on the class level that matches the constraint
     *
     * @param c
     * @param constraint
     * @return
     */
    public static Annotation getClassLevelAnnotation(Class c, HTPredicate<Class> constraint) {
        return getMatchingClass(c.getAnnotations(), constraint);
    }

    public static int getClassLevelAnnotation(Class c, HTPredicate<Class> constraint, List<Annotation> annos) {
        return getAllMatchingClass(c.getAnnotations(), constraint, annos);
    }


    //********************** FIELD ***************************

    /**
     * Get the first member var annotations object ONLY if it has annotations and that the MemberVarAnnotations matches.
     * the MemberVarAnnotations constraint of course can limit to field properties and annotation properties.
     *
     * @param c
     * @param annosConstraint
     * @return
     */
    public static MemberVarAnnotations getMemberVariable(Class c, HTPredicate<MemberVarAnnotations> annosConstraint) {
        for (Field field : c.getFields()) {
            Annotation[] annos = field.getAnnotations();
            if (!ArrayUtil.nullOrEmpty(annos)) {
                MemberVarAnnotations mva = new MemberVarAnnotations(field, annos);
                if (annosConstraint == null || annosConstraint.test(mva)) {
                    return mva;
                }
            }
        }
        return null;
    }

    /**
     * Get member variables that meet the criteria at this class level and optionally from the super class chain.
     *
     * @param c
     * @param annosConstraint
     * @param list
     * @param includeSuper
     * @return
     */
    public static int getAllMemberVariable(Class c, HTPredicate<MemberVarAnnotations> annosConstraint, List<MemberVarAnnotations> list, boolean includeSuper) {
        if (includeSuper) {
            int cnt = 0;
            while (c != null) {
                cnt += getAllMemberVariable(c, annosConstraint, list);
                c = c.getSuperclass();
            }
            return cnt;
        } else {
            return getAllMemberVariable(c, annosConstraint, list);
        }
    }

    /**
     * Get all var annotations object ONLY if it has annotations and that the MemberVarAnnotations matches. the
     * MemberVarAnnotations constraint of course can limit to field properties and annotation properties.
     *
     * @param c
     * @param annosConstraint
     * @return count of matches
     */
    public static int getAllMemberVariable(Class c, HTPredicate<MemberVarAnnotations> annosConstraint, List<MemberVarAnnotations> list) {
        int cnt = 0;
        for (Field field : c.getDeclaredFields()) {
            Annotation[] annos = field.getAnnotations();
            if (!ArrayUtil.nullOrEmpty(annos)) {
                MemberVarAnnotations mva = new MemberVarAnnotations(field, annos);
                if (annosConstraint == null || annosConstraint.test(mva)) {
                    list.add(mva);
                    cnt++;
                }
            }
        }
        return cnt;
    }

    //********************** FIELD ***************************

    /**
     * This is somewhat expensive as we are trying to marry setters and getters together into on wrapper, combining
     * their annotations together
     *
     * @param c
     * @param annosConstraint
     * @param list
     * @return
     */
    public static int getAllBeanStyleMemberFunctions(Class c, HTPredicate<BeanStyleMethodAnnotation> annosConstraint, List<BeanStyleMethodAnnotation> list) {
        Map<String, BeanStyleMethodAnnotation> fieldAnnotationMap = new HashMap();
        for (Method m : c.getDeclaredMethods()) {
            addFieldMethod(fieldAnnotationMap, m, null);
        }
        int cnt = 0;
        for (BeanStyleMethodAnnotation bean : fieldAnnotationMap.values()) {
            if (annosConstraint == null || annosConstraint.test(bean)) {
                list.add(bean);
                cnt++;
            }
        }
        return cnt;
    }

    public static int getAllMemberFunctions(Class c, HTPredicate<MethodAnnotation> methodConstraint,
                                            HTPredicate<Class> annotationConstraint,
                                            List<MethodAnnotation> list) {
        int cnt = 0;
        try {
            for (Method m : c.getDeclaredMethods()) {
                MethodAnnotation ma = new MethodAnnotation(m, annotationConstraint);
                if (methodConstraint == null || methodConstraint.test(ma)) {
                    cnt++;
                    list.add(ma);
                }
            }
        } catch (NoClassDefFoundError e) {
            // Class references unavailable types (e.g., Jetty not on classpath)
            // Skip this class gracefully - services can still load without debug commands
            logger.warn("Skipping method scanning for {} due to missing dependency: {}", 
                    c.getName(), e.getMessage());
        }

        return cnt;
    }

    public static MethodAnnotation getMemberFunction(Class c, HTPredicate<MethodAnnotation> methodConstraint,
                                                     HTPredicate<Class> annotationConstraint) {
        try {
            for (Method m : c.getDeclaredMethods()) {
                MethodAnnotation ma = new MethodAnnotation(m, annotationConstraint);
                if (methodConstraint == null || methodConstraint.test(ma)) {
                    return ma;
                }
            }
        } catch (NoClassDefFoundError e) {
            // Class references unavailable types (e.g., Jetty not on classpath)
            logger.warn("Skipping method scanning for {} due to missing dependency: {}", 
                    c.getName(), e.getMessage());
        }

        return null;
    }


    /**
     * Attempt to put a method as part of a "field".
     *
     * @param fieldAnnotationMap   working apply that stores our current field annotations keyed by field name
     * @param meth                 the method to be added
     * @param annotationConstraint the constraints on which annotations we track
     * @return true if the method was part of a field definition, false otherwise
     */
    public static boolean addFieldMethod(Map<String, BeanStyleMethodAnnotation> fieldAnnotationMap,
                                         Method meth,
                                         Map<Class, Class> annotationConstraint) {
        // if this is a "void setXxx (type zz)" or "zz getXxx ()" method, then it is part of the field xxx
        String methodName = meth.getName();
        String fieldName;
        Class fieldType;
        boolean isGetter = false;
        if (methodName.startsWith("set")) {
            // does the method have a void return?
            if (meth.getReturnType() != Void.TYPE) {
                return false;
            }
            // does the method take one argument?
            Class[] argTypes = meth.getParameterTypes();
            if (argTypes == null || argTypes.length != 1) {
                return false;
            }
            fieldName = methodName.substring(3);
            fieldType = argTypes[0];
        } else if (methodName.startsWith("get") || methodName.startsWith("is")) {
            // does the method have no arguments?
            Class[] argTypes = meth.getParameterTypes();
            if (argTypes != null && argTypes.length > 0) {
                return false;
            }
            // does the method have non-null return type
            fieldType = meth.getReturnType();
            if (fieldType == null) {
                return false;
            }
            isGetter = true;
            if (methodName.startsWith("is")) {
                // this only works for boolean
                fieldName = methodName.substring(2);
                if (fieldType != Boolean.TYPE) {
                    return false;
                }
            } else {
                fieldName = methodName.substring(3);
            }
        } else {
            return false;
        }

        // we must have a nonemtpy field name
        if (fieldName.length() == 0) {
            return false;
        }

        // fix the camelback nature of the method name - lowercase the first letter of the field name
        fieldName = StringUtil.strcat(Character.toLowerCase(fieldName.charAt(0)), fieldName.substring(1));

        // see if we already have a field annotation for this field (because we've already seen a get or set)
        BeanStyleMethodAnnotation previousA = fieldAnnotationMap.get(fieldName);
        if (previousA != null) {
            previousA.addMethod(isGetter, meth, annotationConstraint);
        } else {
            BeanStyleMethodAnnotation annot = new BeanStyleMethodAnnotation(fieldName, meth, isGetter, fieldType, annotationConstraint);
            fieldAnnotationMap.put(fieldName, annot);
        }

        return true;
    }


    public static Annotation getMatchingClass(Annotation[] arr, HTPredicate<Class> c) {
        for (Annotation a : arr) {
            Class ac = a.annotationType();
            if (c.test(ac)) {
                return a;
            }
        }
        return null;
    }


    public static int getAllMatchingClass(Annotation[] arr, HTPredicate<Class> c, List<Annotation> annos) {
        int cnt = 0;
        for (Annotation a : arr) {
            Class ac = a.annotationType();
            if (c == null || c.test(ac)) {
                annos.add(a);
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Add annotations to a result listFiles, only those that are in a set of possibilities. This is used to allow us to
     * filter annotations so we only track ones of interest.
     *
     * @param list       the result listFiles of annotations, added to if the potential annotation is acceptable
     * @param ano        The annotations we are considering
     * @param constraint The filtering apply.  To be added to the result, the annotations class must be a key in the apply.
     *                   If the constraint is null, all annotations are added to the result.
     */
    public static void loadAnnotation(List<Annotation> list, Annotation ano[], Map<Class, Class> constraint) {
        for (Annotation a : ano) {
            if (constraint != null) {
                if (constraint.get(a.getClass()) != null) {
                    // its in our allowed listFiles, put it
                    list.add(a);
                }
            } else {
                list.add(a);
            }
        }
    }

    /**
     * Add annotations to a result listFiles, only those that are in a set of possibilities. This is used to allow us to
     * filter annotations so we only track ones of interest.
     *
     * @param list                 the result listFiles of annotations, added to if the potential annotation is acceptable
     * @param ano                  The annotations we are considering
     * @param annotationConstraint
     */
    public static void loadAnnotation(List<Annotation> list, Annotation ano[], HTPredicate<Class> annotationConstraint) {
        for (Annotation a : ano) {
            if (annotationConstraint != null) {
                if (annotationConstraint.test(a.getClass())) {
                    list.add(a);
                }
            } else {
                list.add(a);
            }
        }
    }
}


