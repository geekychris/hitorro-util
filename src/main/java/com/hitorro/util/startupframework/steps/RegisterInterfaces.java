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
package com.hitorro.util.startupframework.steps;

import com.hitorro.util.core.error.ErrorCode;
import com.hitorro.util.startupframework.ServiceContext;


/**
 * Call the register mechanism of a service to register any hooks.
 */
public class RegisterInterfaces implements ServiceStep {
    public static final String EventName = "RegisterInterfaces";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "RegisterInterfaces";
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        for (Class intf : ServiceContext.getSC().getNeededInterfaces()) {
            if (ServiceContext.getSC().getServiceInterface(intf) == null) {
                // we dont have a required interface
                return new ErrorCode(30, "Interface %s was required but not defined by any service", new Object[]{intf});
            }
        }
        return null;
    }
}
