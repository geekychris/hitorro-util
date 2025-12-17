package ht.util.core.thread;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 16, 2005 Time: 5:29:11 PM
 */
public class HTThread extends Thread {
    private String description = null;

    public HTThread(java.lang.ThreadGroup threadGroup, java.lang.Runnable runnable, java.lang.String string, long l) {
        super(threadGroup, runnable, string, l);
    }

    public HTThread(java.lang.ThreadGroup threadGroup, java.lang.Runnable runnable, java.lang.String string) {
        super(threadGroup, runnable, string);
    }

    public HTThread(java.lang.ThreadGroup threadGroup, java.lang.String string) {
        super(threadGroup, string);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
