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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.TerminalListener;
import com.hitorro.util.core.*;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.thread.EnhancedThreadGroup;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.startupframework.ExitReasonObject;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.ServiceWrapper;
import net.wimpi.telnetd.TelnetD;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

//import com.hitorro.util.swig.SwigUtils;

public abstract class BaseCommandLine<T> {
    public static final String CheckLogEvent = "checklogevent";
    public static final String CommandKey = "command";
    protected static BaseCommandLine bcl;
    /**
     * Code stored by exit handler to report reason for shutdown
     */
    public ExitReasonObject exitReasonCode = null;
    /**
     * Time when system started
     */
    public long processStartTime = System.currentTimeMillis();
    public String command;
    public String commandLine;
    /**
     * Process id if we got it!
     */
    public int processId = -1;
    protected String parameterLine;
    // property related basics
    protected JVS commandLineArguments = new JVS();
    protected boolean enableCommandLine = false;
    protected Thread commandLineThread;
    protected EnhancedThreadGroup commandLineThreadGroup = new EnhancedThreadGroup("CommandLine");
    protected TelnetD myTD;

    public static BaseCommandLine getCommandLine() {
        return bcl;
    }

    public static void setCommandLine(BaseCommandLine bclIn) {
        bcl = bclIn;
    }

    /**
     * Logging may need two parts to the setup, once before the system is fully up, that needs to allow logging even
     * without all the logging facilities available.
     */
    protected abstract void setupPrimordialLogging();

    /**
     * Assumes that we are now mostly configured and we can now setup logging properly
     */
    protected abstract void setupLogging();

    public abstract boolean haveJVSConfigsChanged();

    /**
     * load or reload properties.
     *
     * @param runDiff if true then we assume we have a prior load of the properties and we should diff against the prior
     *                version to identify any changes.
     * @return Command being executed
     */
    public abstract JVS reloadJVSProps(boolean runDiff);

    /**
     * Get my PID since java doesnt let you do that, we use our swig wrapper todo it.
     */

    /**
     * Register whatever config change watching logic you so choose.
     */
    protected abstract void initConfigChangeWatching();

    /**
     * Do we want log watching to be enabled which will allow us to compresss and remove old logs.
     *
     * @return
     */
    protected abstract boolean setupLogWatching();

    /**
     * public method purely for the purposes of debugging.  Here you can have an eclipse debug project that
     * specifically
     *
     * @param args
     * @param commandIn
     */
    public void mainAux(String[] args, String commandIn) {
        setupCmdArgs(args, commandIn);
        setupPrimordialLogging();
        // if all else failse, we will at least report some basic error

        Thread.currentThread().setUncaughtExceptionHandler(new CommandLineUnhandledExceptionHandler());

        reloadJVSProps(false);

        commandLine = getProcessName(command);

        setupLogging();
        startupDebugTerminal();
        // get global id of system
        com.hitorro.util.core.Env.getGlobalId();
        initCommandLine();
        initConfigChangeWatching();
        try {
            executeCommand(parameterLine, command);
        } catch (IOException e) {
            com.hitorro.util.core.Console.println("Unable to execute command %s %e", e, e);
        }
    }


    //******* TERMINAL STUFF ********

    private void setupCmdArgs(final String[] args, final String commandIn) {
        this.command = commandIn;
        parameterLine = StringUtil.mergeWithJoinToken(args, " ");
        try {
            com.hitorro.util.core.CommandArgs.parseArgs(parameterLine, true, true, commandLineArguments);
            if (StringUtil.nullOrEmptyOrBlankString(command)) {
                command = commandLineArguments.getString(CommandKey);
            }

        } catch (ParseException e) {
            com.hitorro.util.core.Console.eprintln("Unable to parse command line arguments %s \n %s %e",
                    parameterLine, e, e);
            System.exit(-1);
        }
    }

    /**
     * Subclass is able to define the startup and shutdown steps that the service compContext issues.
     *
     * @return
     */
    protected abstract ServiceContext getServiceContext();

    protected void executeCommand(String parameterLine, String command) throws IOException {
        com.hitorro.util.core.Timer timer = new com.hitorro.util.core.Timer();
        timer.start();
        ServiceContext sc = getServiceContext();
        if (StringUtil.nullOrEmptyOrBlankString(command)) {
            com.hitorro.util.core.Console.eprintln("command not provided on command line %s",
                    parameterLine);
            System.exit(-1);
        }

        if (sc.addModule(command)) {
            // we added the service successfully
            String error = ServiceContext.validateConfigKeys();
            if (!StringUtil.nullOrEmptyOrBlankString(error)) {
                com.hitorro.util.core.Console.eprintln("Unable to validate services, got error %s", error);
                System.exit(-1);
            }

            error = sc.init();
            if (!StringUtil.nullOrEmptyString(error)) {
                com.hitorro.util.core.Console.eprintln("Unable to initialize services, got error %s", error);
                com.hitorro.util.core.Env.exitSystem("Exiting", -1);
            }
            Class clazz = ClassUtil.getClassForName(command, null);
            if (clazz == null) {
                com.hitorro.util.core.Env.exitSystem("Unable to find starup class", -1);
            }
            ServiceWrapper s = sc.getInitializedServiceWrapper(clazz);
            timer.stop();
            com.hitorro.util.core.Log.util.info("Took %sms to startup", timer.getTime());
            s.runIfRunnable();
            ServiceContext.getSC().deInit();
            if (exitReasonCode != null) {
                com.hitorro.util.core.Env.exitSystem(exitReasonCode.reason, exitReasonCode.exitCode);
            } else {
                com.hitorro.util.core.Env.exitSystem("Exiting", 1);
            }
        } else {
            com.hitorro.util.core.Console.eprintln("Unable to construct service/command %s", command);
            System.exit(-1);
        }
    }

    public String getProcessName(String command) {
        Class c;
        try {
            if (!StringUtil.nullOrEmptyString(command)) {
                c = Class.forName(command);
                if (c != null) {
                    ServiceWrapper sw = new ServiceWrapper();
                    if (sw.initServiceWrapper(c)) {
                        return sw.getShortName();
                    }
                }
            }
        } catch (ClassNotFoundException e1) {
            // do nothing
        }
        return "unknown";
    }

    /**
     * Allow input directly from the startup console
     */
    protected void initCommandLine() {
        if (enableCommandLine) {
            CommandSession cs = new CommandSession();
            cs.setSession(System.in, System.out);
            commandLineThread = new Thread(commandLineThreadGroup, cs);
            commandLineThread.start();
        }
    }

    public void startupDebugTerminal() {
        try {
            Properties props = new Properties();
            props.load(FileUtil.fsInputStream.apply(new File(com.hitorro.util.core.Env.getConfigDir(), "telnetd.properties")));
            props.put("std.port", Integer.toString(TerminalListener.PortNumber.apply()));
            myTD = TelnetD.createTelnetD(props);
            myTD.start();
        } catch (Exception e) {
            com.hitorro.util.core.Log.util.error("Unable to startup terminal with error %s, %e", e, e);
        }
    }
}
