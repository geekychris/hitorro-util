package ht.util.statemachine;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 1:08:32 PM
 */
public class DoNothingValidator extends Validator<Object> {
    public boolean validate(Object elem, DirectedEdge edge, State nextState) {
        return false;
    }
}
