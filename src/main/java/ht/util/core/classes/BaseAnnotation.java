package ht.util.core.classes;

import ht.util.core.opers.HTPredicate;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BaseAnnotation {
    protected List<Annotation> m_anno = new ArrayList<Annotation>();

    public List<Annotation> getAnnotations() {
        return m_anno;
    }

    public Annotation getAnnotation(Class c) {
        for (Annotation ano : m_anno) {
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
        for (Annotation ano : m_anno) {
            if (oper.test(ano.annotationType())) {
                return true;
            }
        }
        return false;
    }

}
