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
import com.hitorro.util.core.Console;
import com.hitorro.util.core.opers.HTPredicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Utility class to allow extraction of class's method annotation. This class will hold the acceptable annotations for
 * the method, as well as the method itself.  The "acceptable" annotations are those in the constraints handed to the
 * constructor.
 * <p/>
 */
public class MethodAnnotation extends BaseAnnotation {
    private Method m_method;

    private Map<Class, Class> m_identityMap = null;

    public MethodAnnotation(Method m, Map<Class, Class> constraint) {
        m_method = m;
        ClassAnoUtil.loadAnnotation(anno, m.getAnnotations(), constraint);
    }

    public MethodAnnotation(Method m, HTPredicate<Class> annotationConstraint) {
        m_method = m;
        ClassAnoUtil.loadAnnotation(anno, m.getAnnotations(), annotationConstraint);
    }

    public Method getMethod() {
        return m_method;
    }

    /**
     * HTPredicate out annotation
     *
     * @param oper
     * @return
     */
    public Annotation[][] getAnnotationForParametersMatching(HTPredicate<Annotation> oper) {
        Annotation params[][] = m_method.getParameterAnnotations();
        Annotation returnMe[][] = new Annotation[params.length][];
        for (int i = 0; i < params.length; i++) {
            int count = 0;
            int l = params[i].length;
            for (int j = 0; j < l; j++) {
                Annotation ano = params[i][j];

                if (ano != null && oper.test(ano)) {
                    count++;
                }
            }
            returnMe[i] = new Annotation[count];
            count = 0;
            for (int j = 0; j < l; j++) {
                Annotation ano = params[i][j];

                if (ano != null && oper.test(ano)) {
                    returnMe[i][count] = ano;
                    count++;
                }
            }
        }
        return returnMe;
    }

    public Class[] getParameters() {
        return m_method.getParameterTypes();
    }

    public Class getReturnType() {
        return m_method.getReturnType();
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        Console.bprint(sb, "%s = %s(", getReturnType().getName(), m_method.getName());
        Class cArr[] = getParameters();

        if (!ArrayUtil.nullOrEmpty(cArr)) {
            boolean flag = false;
            for (Class c : cArr) {
                if (flag) {
                    sb.append(", ");
                }
                flag = true;
                sb.append(c.getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Only used if this is the default annotations class
     *
     * @return identity apply
     */
    public Map<Class, Class> getClassIdentityMap() {
        if (m_identityMap == null) {
            m_identityMap = ClassAnnotations.getClassIdentityMap(anno);
        }
        return m_identityMap;
    }
}
