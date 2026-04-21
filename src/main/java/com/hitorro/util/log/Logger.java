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
package com.hitorro.util.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.log4j.*;
import org.apache.log4j.net.SyslogAppender;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Wrapper around log4j for clean formatting of log messages and some log level control.
 *
 * @author Chris
 */
public class Logger extends org.apache.log4j.Logger {
    public static final String ExitEvent = "ExitEvent";
    public static StringProperty ConsoleLoggerPattern =
            new StringProperty("logging.console.pattern",
                    "Pattern for standard logger",
                    "%d{ISO8601} (%t) %c{1} %p: %m%n");
    public static StringProperty ConsoleLogLevel =
            new StringProperty("logging.console.threshold",
                    "Logging level threshold for sending messages to the stdout",
                    "INFO");
    public static StringProperty StandardLoggerPattern =
            new StringProperty("logging.standard.pattern",
                    "Pattern for standard logger",
                    "%d{ISO8601} (%t) %c{1} %p: %m%n");
    public static StringProperty DefaultLogAppenderLevel =
            new StringProperty("logging.standard.defaultlevel",
                    "Default logging level for the default appender",
                    "info");
    public static StringProperty SyslogPattern =
            new StringProperty("logging.syslog.pattern",
                    "Pattern used for log output to the syslog",
                    "%d{ISO8601} (%t) %c{1} %p: %m%n");
    public static StringProperty SyslogLogLevel =
            new StringProperty("logging.syslog.threshold",
                    "Logging level threshold for sending messages to the syslog",
                    "ERROR");
    public static BooleanProperty SyslogEnabledKey =
            new BooleanProperty("logging.syslog.enabled",
                    "enable or disable the sylogging.", false);
    // we should be able to change the default logging level
    private static final Level s_defaultLogLevel = Level.INFO;
    public static Properties s_props = new Properties();
    public static File s_archiveDir = null;
    private static ConsoleAppender s_consoleAppender = null;
    private static ArchivingAppender s_archiveAppender = null;
    private static TreeMap<String, Level> s_levels;
    private static boolean initialized = false;
    private static boolean initializedProperly = false;
    private static boolean primordialSetup = false;
    private static TreeMap<String, LoggerToLevelMapping> s_categories = new TreeMap<String, LoggerToLevelMapping>();

    static {
        initLevels();
    }

    protected Logger(String name) {
        super(name);
        setLogLevelOnLogger(this);
    }

    public static void removeAppenders() {
        Logger.getRootLogger().removeAllAppenders();
    }

    public static void configurePropsFile() {
        if (initializedProperly == true) {
            return;
        }
        initializedProperly = true;


        try {
            File f = new File(Env.getBin(), "config/log4j.properties");
            s_props.load(FileUtil.getBufferedFileInputStream(f));

        } catch (IOException e) {
            Console.eprintln("Unable to load properties file %s %e", e, e);
        }
        init();
    }

    private static void init() {
        if (initialized == false) {
            initialized = true;
            LoggerHierarchy h = new LoggerHierarchy(new RootLogger(Level.DEBUG));
            DefaultRepositorySelector repositorySelector = new DefaultRepositorySelector(h);
            LogManager.setRepositorySelector(repositorySelector, h);

            s_props.put(PropertyConfigurator.LOGGER_FACTORY_KEY, LoggerFactory.class.getCanonicalName());

            PropertyConfigurator.configure(s_props);
            getRootLogger().setLevel(Level.DEBUG);
            setupPrimordialLogging();
        }
    }

    public static void setupPrimordialLogging() {
        if (primordialSetup) {
            return;
        }
        primordialSetup = true;

        Logger.configurePropsFile();
        Logger.addConsoleAppenderPrimordial();
    }

    public static Level getDefaultLevel() {
        return getLevelFromString(DefaultLogAppenderLevel.apply());
    }

    /**
     * Create a standard logger log file based upon:
     * <p/>
     * $HT_HOME/logs/<servicename>-<time>.log
     *
     * @param processName
     * @throws IOException
     */
    public static void addStandardAppenderWithProcessName(String processName) throws IOException {
        File logDir = Env.getLogDir();
        logDir.mkdirs();
        File logFile = FileUtil.getDatedFileFromPattern(logDir, processName, "log");
        addStandardAppender(logFile.getAbsolutePath());
    }

    public static void addArchivingAppenderWithProcessName(String processName) throws IOException {
        File logDir = Env.getLogDir();
        logDir.mkdirs();
        addArchivingAppender(processName);
    }

    /**
     * Add a syslog appender if enabled.
     */
    public static void addSyslogAppender(String processName) {
        if (SyslogEnabledKey.apply() == true) {
            SyslogAppender appender = new SyslogAppender();
            String host = Env.getHostIP();
            if (StringUtil.nullOrEmptyString(host)) {
                // Dont think this works, we must really have the host name or ip address
                host = "localhost";
            }
            appender.setSyslogHost(host);
            String pattern = SyslogPattern.apply();
            pattern = Fmt.S("%s %s", processName, pattern);
            Layout layout = new PatternLayout(pattern);
            appender.setLayout(layout);
            Level level = getLevelFromString(SyslogLogLevel.apply());
            appender.setFacility("USER");
            appender.setThreshold(level);

            org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
            root.addAppender(appender);
        }
    }

    /**
     * Add stdout output to the root level
     */
    public static void addConsoleAppender() {
        if (s_consoleAppender != null) {
            org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
            root.removeAppender(s_consoleAppender);
        }
        String layoutString = ConsoleLoggerPattern.apply();
        Layout layout = new PatternLayout(layoutString);
        Level consoleLevel = getLevelFromString(ConsoleLogLevel.apply());
        s_consoleAppender = new ConsoleAppender(layout);
        s_consoleAppender.setThreshold(consoleLevel);
        Level level = Logger.getDefaultLevel();
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setAdditivity(true);
        root.setLevel(level);
        root.addAppender(s_consoleAppender);
    }

    /**
     * Add stdout output to the root level..does not assume properties are configured.  Used in basic startup.
     */
    public static void addConsoleAppenderPrimordial() {

        Layout layout = new PatternLayout("%d{ISO8601} (%t) %c{1} %p: %m%n");
        Level consoleLevel = Level.INFO;
        s_consoleAppender = new ConsoleAppender(layout);
        s_consoleAppender.setThreshold(consoleLevel);
        Level level = Level.INFO;
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setAdditivity(true);
        root.setLevel(level);
        root.addAppender(s_consoleAppender);
    }

    /**
     * @param fileName
     * @throws IOException
     */
    public static void addArchivingAppender(String fileName)
            throws IOException {
        s_archiveDir = new File(Env.getLogDir(),
                Fmt.S("archive_%s", fileName));
        String layoutString = StandardLoggerPattern.apply();
        Layout layout = new PatternLayout(layoutString);
        //
        boolean appendFlag = false;
        s_archiveAppender = new ArchivingAppender(layout,
                Env.getLogDir(),
                fileName,
                s_archiveDir,
                1024 * 1024 * 4);

        Level level = Logger.getDefaultLevel();
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setAdditivity(true);
        root.setLevel(level);
        root.addAppender(s_archiveAppender);
    }

    public static void addStandardAppender(String fileName)
            throws IOException {
        s_archiveDir = new File(Env.getLogDir(),
                Fmt.S("archive_%s", fileName));
        String layoutString = StandardLoggerPattern.apply();
        Layout layout = new PatternLayout(layoutString);
        //
        boolean appendFlag = false;
        Appender archivingAppender = new FileAppender(layout, fileName, appendFlag, false, 1024 * 8);

        Level level = Logger.getDefaultLevel();
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setAdditivity(true);
        root.setLevel(level);
        root.addAppender(archivingAppender);
    }

    public static void setLogLevelsFromProps() {
        JsonNode props = GlobalProperties.getProperties();
        JsonNode sub = props.get("env.loglevel");
        if (sub != null) {
            String levels = JSONUtil.mergeValues(sub, ",");
            if (!StringUtil.nullOrEmptyOrBlankString(levels)) {
                setLogLevels(levels);
            }
        }
    }

    public static Level getLevelFromString(String levelString) {
        Level level = s_levels.get(levelString.toLowerCase());
        if (level == null) {
            return Level.INFO;
        }
        return level;
    }

    /**
     * Debugging aid that allows us to log a message (primarily through the Actions mechanism).
     *
     * @param category
     * @param levelString
     * @param message
     */
    public static boolean log(String category, String levelString,
                                    String message) {
        Level level = getLevel(levelString);
        org.apache.log4j.Logger logger = getLoggerByName(category);
        if (level != null && logger != null
                && !StringUtil.nullOrEmptyString(message)) {
            logger.log(level, message);
            return true;
        }
        return false;
    }

    public static final String generateMessageWithCallstack(String msg, Throwable t) {
        StringBuilder buff = new StringBuilder();
        buff.append(msg);
        Fmt.addCallstack(t, buff, false);
        return buff.toString();
    }

    public static Logger getLogger(Class name) {
        if (initialized == false) {
            init();
        }
        return (Logger) org.apache.log4j.Logger.getLogger(name);
    }

    public static Logger getLogger(String name) {
        if (initialized == false) {
            init();
        }
        return (Logger) org.apache.log4j.Logger.getLogger(name);
    }

    /**
     * Set a number of log levels in string form against the logger objects. If these loggers dont exist, just simply
     * record the fact we have defined a
     *
     * @param logLevels
     * @return true if successfull, false if one of the category/level pairs were invalid
     */
    public synchronized static boolean setLogLevels(String logLevels) {
        String parts[] = StringUtil.tokenizeFromSingleChar(logLevels, ",");
        for (String part : parts) {
            String log[] = StringUtil.tokenizeFromSingleChar(part, ":");
            if (log.length != 2) {
                Log.util
                        .error(
                                "Unable to set logging level, meaningless expression %s",
                                part);
                return false;
            } else {
                setLogLevel(log[0], log[1]);
            }
        }
        return true;
    }

    public synchronized static void setLogLevel(String logCat, String level) {
        Level l = getLevel(level);
        LoggerToLevelMapping llm = new LoggerToLevelMapping(logCat, l);
        s_categories.put(logCat, llm);
        // find the undertylying logger
        org.apache.log4j.Logger logger = getLoggerByName(logCat);
        if (logger != null) {
            // Either it hasnt been initialized yet or will never be
            // initialized.
            // if its going to be initialized then we should trap this for later
            // use.
            logger.setLevel(l);
        }
        // hack for things we dont manage
        if (!logCat.startsWith("ht.")) {
            s_props.put(logCat, level);
        }
    }

    public static org.apache.log4j.Logger getLoggerByName(String name) {
        return org.apache.log4j.Logger.getLogger(name);
    }

    public static Level getLevel(String level) {
        level = level.toLowerCase();
        Level l = s_levels.get(level);
        if (l == null) {
            return Level.ERROR;
        }
        return l;
    }

    /**
     * Map of name to level objects for reverse mapping using string based setters
     */
    private static void initLevels() {
        s_levels = new TreeMap<String, Level>();
        s_levels.put("debug", Level.DEBUG);
        s_levels.put("info", Level.INFO);
        s_levels.put("warn", Level.WARN);
        s_levels.put("error", Level.ERROR);
        s_levels.put("fatal", Level.FATAL);
        s_levels.put("all", Level.ALL);
        s_levels.put("off", Level.OFF);
    }

    /**
     * Given a logger that just got initialized, see if we already have a logging category for this and if so, set it to
     * that level, else lets just record in our listFiles what the level is.
     *
     * @param logger
     */
    private synchronized static void setLogLevelOnLogger(Logger logger) {
        String name = logger.getName();
        LoggerToLevelMapping l = s_categories.get(name);
        if (l == null) {
            // we didnt have one in the listFiles so lets put it in.
            s_categories.put(name, new LoggerToLevelMapping(logger));
            // and set a default level for now.
            if (logger.getLevel() == null) {
                // set to default if null
                //logger.setLevel(s_defaultLogLevel);
            }
        } else {
            // we already added this guy to our listFiles so lets set its level
            l.ensureLoggerAssociation();
        }
    }

    public static void getLogLevels(Response response, ResponseShape shape) {
        Set<Entry<String, LoggerToLevelMapping>> set = s_categories.entrySet();
        response.setResponseShape(shape);
        for (Entry<String, LoggerToLevelMapping> entry : set) {
            LoggerToLevelMapping mapping = entry.getValue();
            response.addRow(mapping.getName(),
                    mapping.getLevel().toString(),
                    mapping.getInheritedFromName());
        }
        response.end();
    }

    /**
     * Set the log level of this logger.
     *
     * @param level to set the log level to.
     */
    public void setLevel(Level level) {
        super.setLevel(level);
    }

    public void fatal(String message) {
        super.fatal(message);
        super.fatal(Env.getCurrentStackTrace());
        LocalEventHub.get().event(ExitEvent, "", null);
    }

    public void fatal(String fmtString, Object... args) {
        fatal(Fmt.S(fmtString, args));
    }

    public void fatal(Throwable t, String str) {
        fatal(str, t);
    }

    public void fatal(Throwable t, String fmtString, Object... args) {
        fatal(Fmt.S(fmtString, args), t);
    }

    public void fatal(Throwable t) {
        fatal(t.getMessage(), t);
    }

    public void error(String fmtString, Object... args) {
        if (isEnabledFor(Level.ERROR)) {
            super.error(Fmt.S(fmtString, args));
        }
    }

    public void error(Throwable t, String fmtString, Object... args) {

        if (isEnabledFor(Level.ERROR)) {
            super.error(Fmt.S(fmtString, args), t);
        }
    }

    public void error(Throwable t) {

        if (isEnabledFor(Level.ERROR)) {
            super.error(t.getMessage(), t);
        }
    }

    public void info(String fmtString, Object... args) {
        if (isInfoEnabled()) {
            super.info(Fmt.S(fmtString, args));
        }
    }

    public void info(Throwable t, String fmtString, Object... args) {
        if (isInfoEnabled()) {
            super.info(Fmt.S(fmtString, args), t);
        }
    }

    public void info(Throwable t, String str) {
        if (isInfoEnabled()) {
            super.info(str, t);
        }
    }

    public void info(Throwable t) {
        if (isInfoEnabled()) {
            super.info(t.getMessage(), t);
        }
    }

    public void warn(String fmtString, Object... args) {
        if (isEnabledFor(Level.WARN)) {
            super.warn(Fmt.S(fmtString, args));
        }
    }

    public void warn(Throwable t, String fmtString, Object... args) {
        if (isEnabledFor(Level.WARN)) {
            super.warn(Fmt.S(fmtString, args), t);
        }
    }

    public void warn(Throwable t, String str) {
        if (isEnabledFor(Level.WARN)) {
            super.warn(str, t);
        }
    }

    public void warn(Throwable t) {
        if (isEnabledFor(Level.WARN)) {
            super.warn(t.getMessage(), t);
        }
    }

    public void fatal(String message, Throwable t) {
        fatal(message, (Object) t);
    }

    public void error(String message) {
        if (isEnabledFor(Level.ERROR)) {
            super.error(message);
        }
    }

    public void error(String message, Throwable t) {
        if (isEnabledFor(Level.ERROR)) {
            error(message, (Object) t);
        }
    }

    public void error(Throwable t, String message) {
        if (isEnabledFor(Level.ERROR)) {
            super.error(message, t);
        }
    }

    public void warn(String message) {
        if (isEnabledFor(Level.WARN)) {
            super.warn(message);
        }
    }

    public void warn(String message, Throwable t) {

        if (isEnabledFor(Level.WARN)) {
            warn(message, (Object) t);
        }
    }

    public void info(String message) {
        if (isInfoEnabled()) {
            super.info(message);
        }
    }

    public void info(String message, Throwable t) {
        if (isInfoEnabled()) {
            super.info(message, t);
        }
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            super.debug(message);
        }
    }

    public void debug(String fmtString, Object... args) {
        if (isDebugEnabled()) {
            super.debug(Fmt.S(fmtString, args));
        }
    }

    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            super.debug(message, t);
        }
    }

    public void trace(String message) {
        if (isTraceEnabled()) {
            super.trace(message);
        }
    }

    public void trace(String fmtString, Object... args) {
        if (isTraceEnabled()) {
            super.trace(Fmt.S(fmtString, args));
        }
    }

    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            super.trace(message, t);
        }
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void info(Object message) {
        super.info(message);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void info(Object message, Throwable t) {
        super.info(message, t);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void warn(Object message) {
        super.warn(message);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void warn(Object message, Throwable t) {
        super.warn(message, t);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void error(Object message) {
        super.error(message);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void error(Object message, Throwable t) {
        super.error(message, t);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void fatal(Object message) {
        super.fatal(message);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void fatal(Object message, Throwable t) {
        super.fatal(message, t);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void debug(Object message) {
        super.debug(message);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void debug(Object message, Throwable t) {
        super.debug(message, t);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void trace(Object message) {
        super.trace(message);
    }

    /**
     * Got to have the wrapper else method / class scanning screws up in log output for LocationInfo
     *
     * @param message
     */
    public void trace(Object message, Throwable t) {
        super.trace(message, t);
    }

    /**
     * This method creates a new logging event and logs the event without further checks.
     */
    protected void forcedLog(String fqcn, Priority level, Object message, Throwable t) {
        callAppenders(new LoggingEvent(Logger.class.getCanonicalName(), this, level, message, t));
    }
}

