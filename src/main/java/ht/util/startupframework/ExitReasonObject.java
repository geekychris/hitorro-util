package ht.util.startupframework;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 19, 2006 Time: 10:24:08 AM
 */
public class ExitReasonObject {
    public String reason = "";
    public int exitCode = 0;

    public ExitReasonObject(String reason, int exitCode) {
        this.reason = reason;
        this.exitCode = exitCode;
    }

}
