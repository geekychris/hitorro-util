package ht.util.log;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Logger;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 1, 2006 Time: 8:49:34 AM
 */
public class LoggerHierarchy extends Hierarchy {
    private static final LoggerFactory NewLoggerFactory = new LoggerFactory();

    public LoggerHierarchy(Logger logger) {
        super(logger);
    }

    public Logger getLogger(String name) {
        return super.getLogger(name, NewLoggerFactory);
    }
}



