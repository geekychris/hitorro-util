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
package com.hitorro.util.startupframework;

import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.EventListener;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.log.Logger;

import java.io.IOException;

/**
 * <p/>
 * Exit handler that knows how to shutdown the system in an orderly way that means, asking the ServicesContext to
 * shutdown.
 */
public class CleanShutdownExitHandler implements EventListener {
    public CleanShutdownExitHandler() {
        // register for exit
        LocalEventHub.get().addEventListener(this, Logger.ExitEvent);
    }

    public boolean event(String topic, String subTopic, Object o) {
        int code = -1;
        String reason = "CleanShutdownExitHandler with exit code";

        if (o instanceof ExitReasonObject) {
            BaseCommandLine.getCommandLine().exitReasonCode = (ExitReasonObject) o;
        }

        // note that deInit may never return if the de-initialization logic locks up
        // for now we will not deal with it here, instead, have a force option on the
        // command line exit.
        Log.util.info("Guru Meditation......exiting");
        try {
            ServiceContext.getSC().deInit();
        } catch (IOException e) {
            Log.servicecontext.error("Unable to deinit %s %e", e, e);
        }
        return false;
    }

    public String eventName() {
        return null;
    }

    public boolean runAsync() {
        return false;
    }
}
