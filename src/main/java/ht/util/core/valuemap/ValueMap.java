package ht.util.core.valuemap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 3, 2006 Time: 11:51:51 AM
 */
public interface ValueMap<E> {

    void setDomain(String domain);

    /**
     * The domain is hierarchically oriented.
     *
     * @return
     */
    boolean isHierarchical();


    Set<Map.Entry<String, E>> getEntrySet();

    Set<String> getKeys();

    Collection<E> getValues();

    /**
     * If this value is applied to a versionableobject category, it must be unique over all system versions.  This is
     * specific for "labels" such as "release"
     *
     * @return
     */
    boolean isUniqueOverSystemVersions();

    /**
     * Get the value or null if not found.  If this is a hierarchical mechanism, do not traverse up the tree if a
     * matching value can not be found.
     *
     * @param key
     * @return
     */
    E getValueNonDefaulting(String key);

    /**
     * get a value and if there is no matching value and this is a hierarchical apply and one knows how to manipulate the
     * key, traverse up the apply. to find the best test.
     *
     * @param key to search for a value for.
     * @return Value if found
     */
    E getValue(String key);


    /**
     * put a value to the domain
     *
     * @param value
     * @param key
     * @return
     */
    E setValue(E value, String key);

    /**
     * Determine if this key is valid.  In some domains, we are not managing the values such as with labels.
     *
     * @param key to validate
     * @return true if valid
     */
    boolean validate(String key);

}
