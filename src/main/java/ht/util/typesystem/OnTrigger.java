package ht.util.typesystem;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 16, 2006 Time: 1:17:36 PM Triggers are
 * fired  at specific points in time.
 */
public interface OnTrigger {
    /**
     * Execute a trigger on a base type object
     *
     * @param type
     * @param bt
     */
    boolean execute(TriggerType key, TypeIntf type, Object bt);

    String getName();

    enum TriggerType {
        OnNew, OnLoad, BeforeSave, BeforePersist, BeforeFlush, BeforeDelete, OnIndex, PostIndex
    }
}
