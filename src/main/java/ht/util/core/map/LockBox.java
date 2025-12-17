package ht.util.core.map;

import java.util.Map;

/**
 * Box of objects that are used as locks.  Given an object, we rely upon hashcode and equals operators to find equiv
 * objects (like string internalizing).  If there is nothing found, then the object is held in the lockbox, if it is
 * found the equivelant lock box object is provided.  All access to this object is synchronized to prevent corruption.
 */

public class LockBox {
    private Map m_locks = MapUtil.map();

    public synchronized Object getLock(Object test) {
        Object result = m_locks.get(test);
        if (result == null) {
            m_locks.put(test, test);
            return test;
        }
        return result;
    }
}