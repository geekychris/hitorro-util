package ht.util.log;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 1, 2006 Time: 12:11:23 AM
 */
public class LoggerFactory implements org.apache.log4j.spi.LoggerFactory {
    public LoggerFactory() {
    }

    public org.apache.log4j.Logger makeNewLoggerInstance(String s) {
        return new Logger(s);
    }
}
