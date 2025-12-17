package ht.util.osprocessexec;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 31, 2006 Time: 11:20:38 PM
 */
public class WaitOnProcessRunner implements Runnable {
    private ExecContext m_context = null;

    public WaitOnProcessRunner(ExecContext context) {
        m_context = context;
    }

    public void run() {
        int exitCode = 0;
        try {
            exitCode = m_context.getProcess().waitFor();
        } catch (InterruptedException e) {

        }
        m_context.notifyComplete(exitCode);
    }
}