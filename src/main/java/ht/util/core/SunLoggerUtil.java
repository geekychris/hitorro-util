package ht.util.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 28, 2005 Time: 8:19:39 AM
 */
public class SunLoggerUtil {

    public static final void setLogLevel(String className) {
        Logger.getLogger(className).setLevel(Level.SEVERE);
    }
}
