package ht.util.core.classes;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class used for implementing a factory, an association between a key and
 *
 * @param <I>
 * @param <C>
 * @author chris
 */
public class ClassFactory<I, C> {
    private Map<I, Class<C>> m_map = new HashMap<I, Class<C>>();

    public void add(I key, Class<C> clazz) {
        m_map.put(key, clazz);
    }

    public Class<C> getClass(I key) {
        return m_map.get(key);
    }

    public C getInstanceSwallowException(I key) {
        try {
            return getInstance(key);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public C getInstance(I key) throws InstantiationException,
            IllegalAccessException {
        Class<C> clazz = getClass(key);
        C c = clazz.newInstance();
        return c;
    }
}
