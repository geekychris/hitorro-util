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

import com.hitorro.util.core.Log;
import com.hitorro.util.core.error.ErrorCode;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.ServiceWrapper;

import java.util.List;

/**
 * We are done with the process, lets walk backwards through the set of services and ask them to cleanly shut down.
 */
public class ShutdownServices implements ServiceStep {
    public static final String EventName = "ShutdownServices";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "ShutdownServices";
    }

    @Override
    public ErrorCode execute(final boolean initDb) {
        Log.servicecontext.info("Deinitializing services");
        List<ServiceWrapper> swList = ServiceContext.getSC().getServices();
        for (int i = swList.size() - 1; i >= 0; i--) {
            ServiceWrapper module = swList.get(i);
            if (module.isInitialized()) {
                String text = module.deInit();
                if (!StringUtil.nullOrEmptyString(text)) {
                    return new ErrorCode(60, "Unable to deInitialize module %s with error %s",
                            new Object[]{module.getShortName(), text});
                }
            }
        }
        return null;
    }
}
