package ht.util.cmdline;

import ht.jsontypesystem.propreaders.JVSConfigChangeRegistry;
import ht.util.core.Env;
import ht.util.core.thread.EnhancedThreadGroup;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.IntegerProperty;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * Responsible for watching changes to config files.  If there are any then should reload configs and run the
 * appropriate diffing.
 */
public class ConfigChangeWatcher implements Runnable {
    public static final IntegerProperty CheckPeriod = new IntegerProperty("config.changecheck.seconds", "how frequently to check for changes to configs", 60);

    public static final BooleanProperty Check = new BooleanProperty("config.changecheck.enabled", "whether to check for property changes", false);

    private static ConfigChangeWatcher ccw;
    private static EnhancedThreadGroup etg = new EnhancedThreadGroup("Config Watcher");
    private static Thread t;
    private static int sleepSecs;

    private static Object notifier = new Object();

    public ConfigChangeWatcher() {

    }

    public static void notifyOfChange() {
        synchronized (notifier) {
            notifier.notify();
        }
    }

    public static void enableConfigWatching() {
        if (Check.apply()) {
            sleepSecs = CheckPeriod.apply();
            ccw = new ConfigChangeWatcher();
            t = new Thread(etg, ccw);
            t.start();
        }
    }

    public static void forceReload() {
        if (BaseCommandLine.getCommandLine().haveJVSConfigsChanged()) {
            BaseCommandLine.getCommandLine().reloadJVSProps(true);
        }
    }

    public void run() {
        while (true) {
            Env.sleepNSeconds(sleepSecs, notifier);
            if (JVSConfigChangeRegistry.getRegistry().getSize() > 0) {
                // only do this if we have observers.  Not much point doing file checks else
                forceReload();
            }
        }
    }
}
