/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

import java.util.HashMap;

/**
 * <p/>
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
