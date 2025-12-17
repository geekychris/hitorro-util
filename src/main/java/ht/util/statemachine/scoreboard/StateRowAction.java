package ht.util.statemachine.scoreboard;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 6:26:07 PM
 */
public interface StateRowAction<P> {
    boolean execute(StateRow<P> row);
}
