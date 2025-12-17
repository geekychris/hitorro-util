package ht.util.typesystem.listeners;

import ht.util.startupframework.ServiceContext;
import ht.util.typesystem.*;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 14, 2006 Time: 6:44:58 PM
 */
public class BaseOnTriggerGeneric implements OnTrigger {
    boolean initialized = false;

    public boolean execute(OnTrigger.TriggerType key, TypeIntf type, Object o) {
        if (initialized == false) {
            //XXX TODO this is a hack, we should really say for just an abstract service name?
            if (ServiceContext.getSC().getInitializedServiceByShortname("basedms") == null) {
                return false;
            }
            initialized = true;
        }

        if (o instanceof BaseType) {
            BaseType<BaseSession> bt = (BaseType<BaseSession>) o;
            if (key == OnTrigger.TriggerType.OnLoad) {
                BaseSession sess = bt.getSession();
                if (sess != null) {
                    String guid = bt.getGuid();
                    sess.addToCache(guid, bt);
                    if (bt instanceof VersionBaseType) {
                        VersionBaseType vbt = (VersionBaseType) bt;
                        vbt.snapshotVersionStamp();
                    }
                }
            }
        }
        return true;
    }

    public String getName() {
        return "BaseType OnTrigger (VersionableObject builtin)";
    }
}
