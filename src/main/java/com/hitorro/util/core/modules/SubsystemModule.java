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
package com.hitorro.util.core.modules;

/**
 * Copyright (c) 2003-2008 HiTorro, Inc.
 * <p/>
 * User: chris Date: Apr 18, 2004 Time: 9:38:22 AM
 * <p/>
 * Description:
 */
public abstract class SubsystemModule {
    private boolean m_valid = true;

    /*
        Initialized this subsystem.
        This method is called by the system when the module is added to the
        subsystem compContext
        @return true if the subsystem initializes successfully
    */
    public abstract boolean init();

    /*
        registered name for this module.  Allows getting access by this name
    */
    public abstract String getName();

    /*
        Allow debugging info to be sent to the log per request of engineer
    */
    public abstract void dumpDebugState();

    /*
        Called if the system is being orderly shutdown.
    */
    public abstract boolean deinit();

    public boolean isValid() {
        return m_valid;
    }
}
