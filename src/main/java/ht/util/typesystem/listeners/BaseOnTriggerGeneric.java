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
