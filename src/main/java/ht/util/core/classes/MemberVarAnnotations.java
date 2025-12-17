package ht.util.core.classes;

import ht.util.core.ArrayUtil;

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
            m_anno.add(a);
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
