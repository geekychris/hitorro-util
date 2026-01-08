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
package com.hitorro.util.typesystem.listeners;

import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.typesystem.*;

/**
 */
public class BaseOnTriggerGeneric implements com.hitorro.util.typesystem.OnTrigger {
    boolean initialized = false;

    public boolean execute(com.hitorro.util.typesystem.OnTrigger.TriggerType key, com.hitorro.util.typesystem.TypeIntf type, Object o) {
        if (initialized == false) {
            //XXX TODO this is a hack, we should really say for just an abstract service name?
            if (ServiceContext.getSC().getInitializedServiceByShortname("basedms") == null) {
                return false;
            }
            initialized = true;
        }

        if (o instanceof com.hitorro.util.typesystem.BaseType) {
            com.hitorro.util.typesystem.BaseType<com.hitorro.util.typesystem.BaseSession> bt = (com.hitorro.util.typesystem.BaseType<com.hitorro.util.typesystem.BaseSession>) o;
            if (key == com.hitorro.util.typesystem.OnTrigger.TriggerType.OnLoad) {
                com.hitorro.util.typesystem.BaseSession sess = bt.getSession();
                if (sess != null) {
                    String guid = bt.getGuid();
                    sess.addToCache(guid, bt);
                    if (bt instanceof com.hitorro.util.typesystem.VersionBaseType) {
                        com.hitorro.util.typesystem.VersionBaseType vbt = (com.hitorro.util.typesystem.VersionBaseType) bt;
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
