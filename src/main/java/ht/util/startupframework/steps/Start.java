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
package ht.util.startupframework.steps;

import ht.util.core.Log;
import ht.util.core.error.ErrorCode;
import ht.util.core.string.StringUtil;
import ht.util.startupframework.ServiceContext;
import ht.util.startupframework.ServiceWrapper;


/**
 * Call each service that has a start method to get that service to finally start up.  Start comes after all services
 * have been initialized. Call this a two phase init.  All services are initialized and then all services are started.
 */
public class Start implements ServiceStep {
    public static final String EventName = "Start";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "Start";
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        ServiceContext.getSC().setState(ServiceContext.State.Initializing);
        for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
            Log.servicecontext.info("Starting %s module", module.getShortName());
            String text = module.start(initDb);

            if (!StringUtil.nullOrEmptyString(text)) {
                return new ErrorCode(50, "Unable to start module %s with error %s",
                        new Object[]{module.getShortName(), text});

            }
        }
        return null;
    }
}
