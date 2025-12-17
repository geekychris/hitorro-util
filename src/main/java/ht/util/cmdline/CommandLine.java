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
package ht.util.cmdline;


import ht.jsontypesystem.JVS;
import ht.jsontypesystem.propreaders.*;
import ht.util.commandandcontrol.CommandRegistry;
import ht.util.commandandcontrol.TestCommands;
import ht.util.core.Console;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.events.LocalEventHub;
import ht.util.core.params.HTProperties;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.io.filedirwatch.DirWatcherUtil;
import ht.util.io.filedirwatch.DirectoryWatch;
import ht.util.log.Logger;
import ht.util.startupframework.ServiceContext;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris Standard entry point for starting a command / server
 */
public class CommandLine extends BaseCommandLine<HTProperties> {
    public static DirectoryWatch m_logWatcher = null;


    public final JVSPropertiesReader[] jvsPropLoaders = {new JVSSystemArgsPropertyReader(),
            new JVSDirectoryReadingPropertiesReader(JVSDirectoryType.Bin, false),
            new JVSDirectoryReadingPropertiesReader(JVSDirectoryType.Home, true),
            new JVSLoadPropsPropertyReader(true),
            new JVSSingleFilePropertyReader(true) {
                @Override
                public File getFile(JVS propsSoFar) {
                    return Env.getSavedJVSProps(propsSoFar);
                }
            }, new JVSCommandLinePropertyReader()};

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
        for (JVSPropertiesReader pr : jvsPropLoaders) {
            if (pr.havePropertiesChanged()) {
                return true;
            }
        }
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
                m_logWatcher = DirWatcherUtil.getWatcherFromParams();
                if (m_logWatcher != null) {
                    LocalEventHub.get().addEventListener(m_logWatcher, CheckLogEvent);
                }
            }
        } catch (IOException e) {
            Console.eprintln("Unable to create a standard appender for %s error %s %e", commandLine, e, e);
        }
    }

    public JVS reloadJVSProps(boolean runDiff) {
        JVS props = new JVS();
        try {
            for (JVSPropertiesReader reader : jvsPropLoaders) {
                reader.getProperties(props, commandLineArguments);
            }
            props.resolveVariables(props);

            JVSProperties.setDefaultProperties(props, runDiff);
            return props;
        } catch (Exception e) {
            Log.util.error("Unable to init configs%s %e", e, e);
            return null;
        }

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


