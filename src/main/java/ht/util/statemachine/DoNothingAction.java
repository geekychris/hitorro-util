package ht.util.statemachine;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 1:09:19 PM
 */
public class DoNothingAction extends Action {
    public boolean runInSeperateThread() {
        return false;
    }

    public boolean modifyState(Object elem, Object containingStructure) {
        return false;
    }
}
