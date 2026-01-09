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

import com.hitorro.util.core.opers.HTPredicate;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BaseAnnotation {
    protected List<Annotation> anno = new ArrayList<Annotation>();

    public List<Annotation> getAnnotations() {
        return anno;
    }

    public Annotation getAnnotation(Class c) {
        for (Annotation ano : anno) {
            if (ano.annotationType().equals(c)) {
                return ano;
            }
        }
        return null;
    }

    /**
     * look for an exact test by class
     *
     * @param c
     * @return
     */
    public boolean containsAnnotation(Class c) {
        return getAnnotation(c) != null;
    }

    /**
     * @param oper
     * @return
     */
    public boolean containsAnnotation(HTPredicate<Class> oper) {
        for (Annotation ano : anno) {
            if (oper.test(ano.annotationType())) {
                return true;
            }
        }
        return false;
    }

}
