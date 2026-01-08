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
package com.hitorro.util.cmdline;

import com.hitorro.jsontypesystem.propreaders.JVSConfigChangeRegistry;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.thread.EnhancedThreadGroup;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;

/**

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
