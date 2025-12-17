/*
    Copyright (c) 2003 - present HiTorro All rights reserved.


    User: chris
*/
package ht.util.core;

import ht.util.core.Audit.AuditStatus;

public interface Auditable {
    String _userName = Constants.EmptyString;
    String _ipAddress = Env.getHostIP();


    void audit(String topic, String userName, AuditStatus status, String message, Object... args);

}
