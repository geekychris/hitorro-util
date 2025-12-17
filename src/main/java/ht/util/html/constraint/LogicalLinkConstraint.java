package ht.util.html.constraint;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 6:33:45 PM
 */
public abstract class LogicalLinkConstraint implements LinkConstraint {
    protected LinkConstraint m_constraints[];

    public LogicalLinkConstraint(LinkConstraint... constraints) {
        m_constraints = constraints;
    }
}
