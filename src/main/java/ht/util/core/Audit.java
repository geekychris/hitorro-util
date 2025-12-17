package ht.util.core;

import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

import java.util.HashMap;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 16, 2005 Time: 12:05:56 PM
 */
public class   Audit {
    //   global audit topics
    public static final String AuthenticationTopic = "auth";
    public static final String WorkflowTopic = "workflow";
    public static final String AdministrateTopic = "admin";

    public static final void audit(String host, String username, String topic, AuditStatus status, String message, Object... args) {
        if (Log.audit.isInfoEnabled()) {
            if (args != null && args.length > 0) {
                message = Fmt.S(message, args);
            }

            Log.audit.info("topic: %s, host: %s, username: %s, message: %s, status: %s", topic, host, username, message, status.toString());
        }
    }


    //   audit statii
    public enum AuditStatus {
        Success("succeeded"), Failure("failed"), InProgress("in progress"), None("none");
        private static HashMap<String, AuditStatus> m_statusMap = new HashMap<String, AuditStatus>() {
            //   initialize static hashmap with a few key-values pairs
            {
                put("approve", Success);
                put("deny", Failure);
                put("true", Success);
                put("false", Failure);
            }
        };
        private String m_status = Constants.EmptyString;


        AuditStatus(String status) {
            m_status = status;
        }

        public static AuditStatus getStatus(boolean status) {
            if (status) {
                return Success;
            }

            return Failure;
        }

        public static AuditStatus getStatus(String statusKey) {
            AuditStatus status = null;

            if (!StringUtil.nullOrEmptyOrBlankString(statusKey)) {
                status = m_statusMap.get(statusKey);
            }

            if (status == null) {
                status = None;
            }

            return status;
        }

        public String toString() {
            return m_status;
        }
    }
}
