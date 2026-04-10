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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.commandandcontrol.CommandRegistry;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.commandandcontrol.TestCommands;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.filedirwatch.DirWatcherUtil;
import com.hitorro.util.io.filedirwatch.DirectoryWatch;
import com.hitorro.util.log.Logger;
import com.hitorro.util.startupframework.ServiceContext;

import java.io.File;
import java.io.IOException;

/**
 Standard entry point for starting a command / server
 */
public class CommandLine extends BaseCommandLine<HTProperties> {
    public static DirectoryWatch logWatcher = null;

    /**
     * Some specialized args passed in are: JAVA_HOME=/jre/path command=ServiceToRun (class name or short name if we
     * have a way to register) ....rest of arguments as we know it
     *
     * @param args
     */
    public static void main(String args[]) {
        AspectJUtil.setupAspectJ();
        CommandRegistry.getRegistry().addAllFromObject(new TestCommands());
        BaseCommandLine.setCommandLine(new CommandLine());

        BaseCommandLine.getCommandLine().mainAux(args, null);
    }

    public boolean haveJVSConfigsChanged() {
        // Override in subclass that has access to property readers
        return false;
    }

    protected void setupPrimordialLogging() {
        Logger.setupPrimordialLogging();
    }

    protected void initConfigChangeWatching() {
        ConfigChangeWatcher.enableConfigWatching();
    }

    protected boolean setupLogWatching() {
        return true;
    }

    protected void setupLogging() {
        Logger.setLogLevelsFromProps();
        try {
            if (StringUtil.nullOrEmptyOrBlankString(commandLine)) {
                commandLine = "<<CommandNameNotSet>>";
            }
            int id = Env.getNodeId();
            String logName = Fmt.S("%s.%s", commandLine, id);
            Logger.addArchivingAppenderWithProcessName(logName);
            Logger.addSyslogAppender(logName);

            if (setupLogWatching()) {
                // lets setup a log watcher.
                logWatcher = DirWatcherUtil.getWatcherFromParams();
                if (logWatcher != null) {
                    LocalEventHub.get().addEventListener(logWatcher, CheckLogEvent);
                }
            }
        } catch (IOException e) {
            Console.eprintln("Unable to create a standard appender for %s error %s %e", commandLine, e, e);
        }
    }

    public JsonNode reloadJVSProps(boolean runDiff) {
        // Override in subclass that has access to property readers (e.g., in hitorro-util module).
        // Base implementation returns empty properties.
        ObjectNode props = JsonNodeFactory.instance.objectNode();
        GlobalProperties.setDefaultProperties(props);
        return props;
    }

    /**
     * Setup default service compContext
     *
     * @return
     */
    protected ServiceContext getServiceContext() {
        ServiceContext.setServiceContext(new ServiceContext());
        return ServiceContext.getSC();
    }

}


