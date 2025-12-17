package ht.util.startupframework;

import ht.util.cmdline.BaseCommandLine;
import ht.util.core.Log;
import ht.util.core.events.EventListener;
import ht.util.core.events.LocalEventHub;
import ht.util.log.Logger;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 19, 2006 Time: 10:11:39 AM
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
