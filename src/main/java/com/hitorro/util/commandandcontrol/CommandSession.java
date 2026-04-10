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
package com.hitorro.util.commandandcontrol;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.commandandcontrol.responsemappings.KeyValuePairMapping;
import com.hitorro.util.core.*;
import com.hitorro.util.core.iterator.LineReaderIterator;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.io.TeeOutputStream;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import org.quartz.JobExecutionException;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.*;

import static com.hitorro.util.core.Env.getBin;
import static com.hitorro.util.core.Env.getHome;

/*
 *
 * User: chris
 */

public class CommandSession implements Runnable {
    public static final StringProperty CSVFile = new StringProperty("csvfile", "", null);
    public static final StringProperty TempCSVFile = new StringProperty("temp.csvfile", "", null);
    protected static final String StartupScript = "startup.command";
    private static ResponseShape header = initList();
    private static Map<String, String> substMap = new HashMap<String, String>();
    public LineTerminator lineTerminator = LineTerminator.SemiColon;
    protected String prompt = "HT>";
    @SuppressWarnings("unused")
    protected Socket m_socket;
    protected int m_counter = 0;
    protected InputStream m_is;
    protected OutputStream m_os;
    protected OutputStream scriptOs;
    protected String promptInteractive = "%s>";
    // if a telnet session
    protected boolean closeConnectionOnExit = true;
    protected TerminalInputHandler terminalInputHandler = null;
    protected boolean characterMode = false;
    protected File scriptFile = null;
    protected boolean m_exit = false;
    protected Map<String, Object> m_sessionVars = new HashMap<String, Object>();
    protected Command interactiveCommand = null;
    // keep a history of commands executed.
    List<CommandMap> m_commands = new ArrayList<CommandMap>();
    private UserHistoryBuffer userHistoryBuffer = null;
    private int m_decimalPlaces = 4;

    public CommandSession() {
        this(true);
    }
	public CommandSession(boolean assumeUser) {
		if (assumeUser)
		{substMap.put("csvfile", "");
			try {
				assumeUser("system");
			} catch (FileNotFoundException e) {
				com.hitorro.util.core.Log.commands.error("Unable to open cli buffer for user system %s %e", e, e);
			} catch (UnsupportedEncodingException e) {
				com.hitorro.util.core.Log.commands.error("Unable to open cli buffer for user system %s %e", e, e);
			}}

	}

    /**
     * Mechanism to execute a command and output to the log file without needing too
     * much setup by others.
     *
     * @param action
     * @param params
     * @return
     * @throws JobExecutionException
     */
    public static boolean executeToLog(final String action, final String params) throws JobExecutionException {
        PackingResponse logResponse = new PackingResponse(new LogResponse(com.hitorro.util.core.Log.test), 2);

        try {
            ObjectNode jvs = com.hitorro.util.core.CommandArgs.getParameters(params, true, true);
            InputStream is = null;

            Command dc = CommandRegistry.getRegistry().get(action);
            if (dc == null) {
                return true;
            }
            CommandSession cs = new CommandSession();
            cs.m_os = FileUtil.getBufferedFileOutputStream(
                    FileUtil.getUniqueFileName(com.hitorro.util.core.Env.getLogDir(), "startupscriptlog"));
            dc.execute(action, jvs, logResponse, cs, RestOperations.Get);
            logResponse.end();
        } catch (ParseException exc) {
            throw new JobExecutionException(exc);
        } catch (IOException exc) {
            throw new JobExecutionException(exc);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
        return false;
    }

    private static ResponseShape initList() {
        ResponseShape list = new ResponseShape("history", "command");
        list.addHeader("Number", "Command");
        list.addHeaderShortNames("Number", "Command");
        list.addRowTypes(Integer.class, String.class);
        return list;
    }

    private static void copyCommandToFile(String command,
            CommandSession session) {
        if (session.scriptOs != null) {
            try {
                session.scriptOs.write(command.getBytes());
                session.scriptOs.write(com.hitorro.util.core.Constants.NewLineChar);
            } catch (IOException e) {
                com.hitorro.util.core.Log.util.error("Exception %s %e", e, e);
            }
        }
    }

    private static String getCommandPart(String command) {
        if (StringUtil.nullOrEmptyOrBlankString(command)) {
            return null;
        }
        int indexOf = command.indexOf(" ");
        if (indexOf == -1) {
            return command;
        }
        return command.substring(0, indexOf);
    }

    private static String getRest(String command) {
        if (StringUtil.nullOrEmptyOrBlankString(command)) {
            return null;
        }
        int indexOf = command.indexOf(" ");
        if (indexOf != -1) {
            return command.substring(indexOf + 1, command.length());
        } else {
            return null;
        }
    }

    private static ObjectNode getArgs(String rest) {
        ObjectNode map = JsonNodeFactory.instance.objectNode();
        if (StringUtil.nullOrEmptyOrBlankString(rest)) {
            return map;
        }

        try {
            com.hitorro.util.core.CommandArgs.parseArgs(rest, true, true, map);
        } catch (ParseException e) {
            com.hitorro.util.core.Log.commands.error("Parsing error %s %e", e, e);
        } catch (PropaccessError propaccessError) {
            return null;
        }
        return map;
    }

    protected static PrintWriter getWriter(CommandSession session) {
        OutputStream osMod = getTeedOS(session);
        if (osMod == null) {
            return null;
        }
        PrintWriter pw = new PrintWriter(osMod);
        printPrompt(pw, session);
        pw.flush();
        return pw;
    }

    private static OutputStream getTeedOS(CommandSession session) {
        if (session.m_os != null) {
            if (session.scriptFile == null) {
                session.scriptOs = null;
                return session.m_os;
            }

            try {
                session.scriptOs = FileUtil.getBufferedFileOutputStream(
                        session.scriptFile, true);
                return new TeeOutputStream(session.m_os, session.scriptOs);
            } catch (FileNotFoundException e) {
                return session.m_os;
            }
        } else {
            if (session.scriptFile != null) {

                try {
                    session.scriptOs = FileUtil.getBufferedFileOutputStream(
                            session.scriptFile, true);
                } catch (FileNotFoundException e) {
                    com.hitorro.util.core.Log.util.error("Exception %s %e", e, e);
                }
                return session.scriptOs;
            }
        }
        return null;
    }

    private static void printPrompt(PrintWriter pw, CommandSession session) {
        if (session.m_counter > 0) {
            pw.write(Fmt.S(session.promptInteractive, session.m_counter));
        } else {
            pw.write(session.prompt);
        }

        pw.flush();
    }

    @CommandDef(command = "history.list", description = "Dump the history buffer", resultMapper = KeyValuePairMapping.class)
    public static List<com.hitorro.util.core.GenericKeyValue> historyList(
            @DebugArgAno(keyName = "session", description = "session", defaultValue = "", argType = ArgType.Session) CommandSession session) {
        return session.getCommandsAsKeyValue();
    }

    @CommandDef(command = "history.clear", description = "Clear the history")
    public static boolean historyClear(
            @DebugArgAno(keyName = "session", description = "session", defaultValue = "", argType = ArgType.Session) CommandSession session,
            @DebugArgAno(propType = BooleanProperty.class, keyName = "deletefile", description = "include file based history", defaultValue = "") boolean includingFile) {
        session.clearHistory(includingFile);
        return true;
    }

    public void setInteractiveCommand(Command c) {
        interactiveCommand = c;
    }

    public void exitSession() {
        m_exit = true;
    }

    /**
     * assume the role of a user for the purposes of a history buffer
     *
     * @param user
     * @return
     */
    public boolean assumeUser(String user) throws FileNotFoundException, UnsupportedEncodingException {
        userHistoryBuffer = new UserHistoryBuffer(new File(getHome(), "/clibuffers"), user, 500, this);
        return true;
    }

    public void clearHistory(boolean includeFile) {
        if (includeFile && userHistoryBuffer != null) {
            userHistoryBuffer.removeFile();
        }
        this.m_commands.clear();
    }

    /**
     * May have either a temporary or permanent session change (via set) that
     * defines that we have a csv file to output
     * to.
     *
     * @return
     */
    public String getCSVFile() {
        Object o = m_sessionVars.get("csvfile");
        if (o == null) {
            return null;
        }
        String permcsv = o.toString();
        if (!StringUtil.nullOrEmptyString(permcsv)) {
            // because this is per command, we have to write out a different csv file per
            // invocation of this call.
            String path = FileUtil.getFilePath(permcsv);
            permcsv = FileUtil.getFileNameSansExtension(permcsv);
            permcsv = Fmt.S("%s/%s-%s.csv", path, permcsv, System.currentTimeMillis());
            return permcsv;
        }

        String temppermcsv = getTempProperty("temp.csvfile");
        if (!StringUtil.nullOrEmptyString(temppermcsv)) {

            return temppermcsv;
        }
        return null;
    }

    private String getTempProperty(String propKey) {
        Object o = m_sessionVars.get(propKey.toLowerCase());
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    public Object removeVar(String varName) {
        return m_sessionVars.remove(varName);
    }

    public Object getVar(String varName) {
        return m_sessionVars.get(varName.toLowerCase());
    }

    public String getVarAsString(String varName) {
        Object o = getVar(varName);
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    public int getVarAsInt(String varName, int defaultVal) {
        Object o = getVar(varName);
        if (o == null) {
            return defaultVal;
        }
        return Integer.parseInt(o.toString());
    }

    public void setVar(String varName, Object var) {
        m_sessionVars.put(varName.toLowerCase(), var);
    }

    public List<com.hitorro.util.core.GenericKeyValue> getVars() {
        return MapUtil.getKeyValueListFromMap(m_sessionVars);
    }

    public void setSession(InputStream is, OutputStream os) {
        prompt = BaseCommandLine.getCommandLine().commandLine;
        m_is = is;
        m_os = os;
        terminalInputHandler = new TerminalInputHandler(is, os);
        terminalInputHandler.setCharacterMode(characterMode);
        init();
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getDoubleDecimalPlaces() {
        return m_decimalPlaces;
    }

    public void init() {

    }

    /**
     * Set character mode for terminal input.
     * In character mode, commands execute on Enter without requiring semicolon.
     * In line mode, semicolon is required as terminator.
     * 
     * @param characterMode true for character mode (SSH), false for line mode
     *                      (telnet)
     */
    public void setCharacterMode(boolean characterMode) {
        this.characterMode = characterMode;
        if (terminalInputHandler != null) {
            terminalInputHandler.setCharacterMode(characterMode);
        }
    }

    /**
     * Look for a script file in the following places: using its literal path. in
     * the home/scripts directory in the
     * bin/scripts directory
     *
     * @param candidate
     * @return
     */
    public File findScriptFile(String candidate) {
        File abs = new File(candidate);
        if (abs.exists()) {
            return abs;
        }

        String scriptDir = Fmt.S("scripts/%s", candidate);
        File home = new File(getHome(), scriptDir);
        if (home.exists()) {
            return home;
        }

        File bin = new File(getBin(), scriptDir);
        if (bin.exists()) {
            return bin;
        }
        return null;
    }

    public void addExecutedCommand(String command, ObjectNode args, String rawArgs) {
        boolean addMe = true;
        if (userHistoryBuffer != null) {
            try {
                if (!userHistoryBuffer.addRow(command, rawArgs)) {
                    addMe = false;
                }
            } catch (IOException e) {
                com.hitorro.util.core.Log.commands.error("Unable to write to cli history %s %e", e, e);
            }
        }
        if (addMe) {
            m_commands.add(new CommandMap(command, args, rawArgs));
        }
    }

    public void history(Response response) {
        response.setResponseShape(header);
        for (int i = 0; i < m_commands.size(); i++) {
            response.addRow(i, m_commands.get(i).toString());
        }
        response.end();
    }

    public CommandMap getCommand(int i) {
        if (m_commands.size() > i) {
            return m_commands.get(i);
        }
        return null;
    }

    public List<CommandMap> getCommands() {
        return m_commands;
    }

    public List<com.hitorro.util.core.GenericKeyValue> getCommandsAsKeyValue() {
        List<com.hitorro.util.core.GenericKeyValue> list = new ArrayList<com.hitorro.util.core.GenericKeyValue>();
        int i = 0;
        for (CommandMap map : m_commands) {
            list.add(new com.hitorro.util.core.GenericKeyValue(Integer.toString(i++),
                    StringUtil.strcat(map.getCommand(), " ", map.getRawArgs())));
        }
        return list;
    }

    public void executeRawCommandAux(String command, ObjectNode map,
            Response resp, String rawArgs) throws PropaccessError {
        Command debugCommand = CommandRegistry.getRegistry().get(command);
        if (debugCommand == null) {
            CommandRegistry.listCommands(resp, this);
            return;
        }
        CommandRegistry.getRegistry().execute(rawArgs, command, map, resp, this);

    }

    public void run() {
        // non buffered
        // executeStartupScript(this);
        StringBuilder builder = new StringBuilder();
        try {
            processTerminal(builder, this);
        } catch (IOException e) {
            com.hitorro.util.core.Log.commands.error("The following exception occured %s %e", e, e);
        } catch (PropaccessError e) {
            com.hitorro.util.core.Log.commands.error("The following exception occured %s %e", e, e);
        } finally {
            if (this.closeConnectionOnExit) {
                com.hitorro.util.core.Log.commands.debug("About to flush and close connection");
                try {
                    m_os.flush();
                } catch (IOException e) {
                    com.hitorro.util.core.Log.util.error("Exception %s %e", e, e);
                }
                try {
                    m_os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    com.hitorro.util.core.Log.util.error("Exception %s %e", e, e);
                }
            }
        }
    }

    public void processTerminal(StringBuilder builder,
            CommandSession session) throws IOException, PropaccessError {
        com.hitorro.util.core.Log.commands.debug("Processing standard terminal response.");

        // Use TerminalInputHandler if available, otherwise fall back to BufferedReader
        if (session.terminalInputHandler != null) {
            while (session.m_exit == false) {
                // Build prompt string with line number (like telnet)
                String promptStr;
                if (session.m_counter > 0) {
                    promptStr = Fmt.S(session.promptInteractive, session.m_counter);
                } else {
                    // Include line number (history count) in prompt
                    int lineNumber = session.m_commands.size();
                    promptStr = Fmt.S("%s-%s>", session.prompt, lineNumber);
                }

                // Print prompt in green (like telnet) before reading
                try {
                    // ANSI codes: green text, then reset after prompt
                    session.m_os.write(("\u001B[32m" + promptStr + "\u001B[0m").getBytes());
                    session.m_os.flush();
                } catch (IOException e) {
                    com.hitorro.util.core.Log.commands.error("Error writing prompt: %s %e", e, e);
                }

                // Read command (pass prompt for history redrawing)
                String s = session.terminalInputHandler.readCommand(promptStr);

                com.hitorro.util.core.Log.commands.info("SSH command received: [%s]", s);

                // Get writer for output (with autoFlush enabled)
                OutputStream osMod = getTeedOS(session);
                PrintWriter pw = null;
                if (osMod != null) {
                    pw = new PrintWriter(osMod, true); // true = autoFlush
                }

                com.hitorro.util.core.Log.commands.info("About to exec command: [%s], pw=%s", s, pw);

                exec(s, builder, pw, session, true);

                // Flush the PrintWriter to ensure all output is written
                if (pw != null) {
                    pw.flush();
                }

                com.hitorro.util.core.Log.commands.info("Command executed: [%s]", s);
            }
        } else {
            // Fallback to old line-based reading
            InputStreamReader reader = new InputStreamReader(session.m_is);
            BufferedReader in = new BufferedReader(reader);
            while (session.m_exit == false) {
                PrintWriter pw = getWriter(session);
                String s = in.readLine();
                exec(s, builder, pw, session, true);
            }
        }
        com.hitorro.util.core.Log.commands.debug("About to exit session");
    }

    protected boolean exec(String s, StringBuilder builder, PrintWriter pw,
            CommandSession session, boolean addToHistory) throws PropaccessError {
        boolean executed = false;
        if (s == null) {
            // End of command stream; equivalent to "exit" command
            s = "exit;";
        }
        if (!StringUtil.nullOrEmptyOrBlankString(s)) {
            copyCommandToFile(s, session);
            executed = executeCommand(pw, s, builder, session, addToHistory);
        } else {
            if (pw != null) {
                pw.println("Bad command \"" + s + "\"");
                pw.flush();
            }
            session.writeShellLine("Bad command \"" + s + "\"");
        }
        return executed;
    }

    protected void writeShellLine(String line) {

    }

    /**
     * Run the default startup script.
     */
    public void executeStartupScript(File file) throws PropaccessError {
        if (!file.exists()) {
            com.hitorro.util.core.Log.commands.info("Startup Script does not exist");
        }
        executeScript(this, file);
    }

    public void executeScript(CommandSession session, File file) throws PropaccessError {
        InputStream in = null;
        try {
            if (!file.exists()) {
                com.hitorro.util.core.Log.commands.info("Script does not exist %s", file.getAbsolutePath());
            }
            in = FileUtil.getBufferedFileInputStream(file);
        } catch (FileNotFoundException e) {
            com.hitorro.util.core.Log.commands.error("Unable to execute script %s %e", e, e);
        }
        if (in != null) {
            StringBuilder builder = new StringBuilder();
            Iterator lr = new LineReaderIterator(IOUtil.getBufferedReader(in));
            executeIterator(builder, lr, session);
        }
    }

    public void executeScriptToResponse(File file, Response response) throws PropaccessError {
        InputStream in = null;
        try {
            if (!file.exists()) {
                com.hitorro.util.core.Log.commands.info("Script does not exist %s", file.getAbsolutePath());
            }
            in = FileUtil.getBufferedFileInputStream(file);
        } catch (FileNotFoundException e) {
            com.hitorro.util.core.Log.commands.error("Unable to execute script %s %e", e, e);
        }
        if (in != null) {
            Iterator lr = new LineReaderIterator(IOUtil.getBufferedReader(in));
            executeIteratorToResponse(lr, this, response);
        }
    }

    public File getScript() {
        return scriptFile;
    }

    public void setScript(File scr) {
        scriptFile = scr;
    }

    private boolean executeCommand(PrintWriter pw, String sIn,
            StringBuilder builder, CommandSession session, boolean addToHistory) throws PropaccessError {
        if (builder.length() > 0) {
            builder.append(" ");
        }

        builder.append(sIn);
        String s = builder.toString().trim();

        if (session.lineTerminator == CommandSession.LineTerminator.DoubleReturn) {
            if (sIn.trim().length() == 0) {
                // its a double return or at least a blank line return
            } else {
                // dont execute yet.
                session.m_counter++;
                return false;
            }
        } else if (session.lineTerminator == CommandSession.LineTerminator.SemiColon) {
            if (s.endsWith(";")) {
                s = s.substring(0, s.length() - 1);
            } else {
                // dont execute yet.
                session.m_counter++;
                return false;
            }
        }
        String command;
        String rest;
        ObjectNode map;
        if (interactiveCommand != null) {
            if (s.equalsIgnoreCase("exit")) {
                interactiveCommand = null;
                builder.setLength(0);
                return true;
            }
            command = interactiveCommand.getCommand();
            rest = Fmt.S("%s = \"%s\"", interactiveCommand.interactiveArgument(), s);
            map = JsonNodeFactory.instance.objectNode();
            map.put(interactiveCommand.interactiveArgument(), s);

        } else {
            command = getCommandPart(s);
            rest = getRest(s);
            map = getArgs(rest);
        }

        if (!StringUtil.nullOrEmptyOrBlankString(command)) {
            session.m_counter = 0;
            if (command.equalsIgnoreCase("execute")) {
                shellExecute(sIn, rest, pw, builder, session);
            } else if (command.startsWith("!")) {
                executePling(pw, command, session, addToHistory);
            } else {
                executeCommandAux(pw, command, map, false, session, rest, addToHistory);
            }
        }
        // now reset the command
        builder.setLength(0);
        return true;
    }

    /**
     * Look in the history buffer for a previously executed command and execute it.
     * If no number or an invalid index is
     * provided, the history is printed.
     *
     * @param pw
     * @param commandIn
     */
    private boolean executePling(PrintWriter pw, String commandIn,
            CommandSession session, boolean addToHistory) throws PropaccessError {
        String number = "0";
        CommandMap command = null;
        if (commandIn.startsWith("!!")) {
            if (session.getCommands().size() == 0) {
                command = new CommandMap("commands", JsonNodeFactory.instance.objectNode(), "");
            } else {
                number = Integer.toString(session.getCommands().size() - 1);
            }
        } else {
            number = commandIn.substring(1);
        }
        if (command == null) {
            if (StringUtil.nullOrEmptyOrBlankString(number)) {
                // we dont have a number and no response object yet so
                // just print the commands executed again.
                command = new CommandMap("history", JsonNodeFactory.instance.objectNode(), "");
            } else {
                int index = Integer.parseInt(number);
                command = session.getCommand(index);
                if (command == null) {
                    // not a valid number
                    // just print the commands executed again.
                    command = new CommandMap("history", JsonNodeFactory.instance.objectNode(), "");
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        String commandStr = StringUtil.strcat(command.getCommand(), " ", command.getRawArgs());
        if (session.lineTerminator == LineTerminator.SemiColon) {
            commandStr = StringUtil.strcat(commandStr, ";");
        }
        writePromptCommand(pw, session, commandStr);

        return executeCommand(pw, commandStr, builder, session, addToHistory);
    }

    protected void writePromptCommand(final PrintWriter pw, final CommandSession session, final String commandStr) {
        if (pw != null) {
            pw.print(session.prompt);
            pw.print(commandStr);
        }
    }

    private void shellExecute(String command, String rest,
            PrintWriter pw, StringBuilder builder, CommandSession session) {
        if (!StringUtil.nullOrEmptyOrBlankString(rest)) {
            File f = new File(rest);
            if (FileUtil.nullOrNotExist(f)) {
                pw.println(Fmt.S("File %s does not exist", f));
                pw.flush();
                return;
            }

            try {
                Iterator lr = FileUtil.getLineReaderIteratorFromFile(f);
                StringBuilder newBuilder = new StringBuilder();
                executeIterator(newBuilder, lr, session);
            } catch (FileNotFoundException e) {
                pw.println(Fmt.S("Error reading file %s %s %e", f, e, e));
                pw.flush();
            } catch (PropaccessError propaccessError) {
                return;
            }
        } else {
            pw.println("execute requires a file to execute");
            pw.flush();
        }
    }

    private boolean executeIterator(StringBuilder builder, Iterator lr,
            CommandSession session) throws PropaccessError {
        while (lr.hasNext()) {
            PrintWriter pw2 = getWriter(session);

            String line = lr.next().toString();
            if (pw2 != null) {
                pw2.println(line);
                pw2.flush();
            }
            executeCommand(pw2, line, builder, session, false);
        }
        return true;
    }

    private boolean executeIteratorToResponse(Iterator lr, CommandSession session, Response response)
            throws PropaccessError {
        while (lr.hasNext()) {
            PrintWriter pw2 = getWriter(session);

            String line = lr.next().toString();
            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1);
            }

            String command = getCommandPart(line);
            String rest = getRest(line);
            ObjectNode map = getArgs(rest);
            session.executeRawCommandAux(command, map, response, rest);
        }
        return true;

    }

    protected void executeCommandAux(PrintWriter pw, String command,
            ObjectNode args, boolean printCommand,
            CommandSession session, String rawArgs, boolean addToHistory) throws PropaccessError {
        Response resp = getResponse(pw, session);
        if (pw != null) {
            if (printCommand) {
                pw.print(session.prompt);
                pw.println(command);
            }
            pw.println();
            pw.flush();
        }
        // store the command executed
        if (addToHistory) {
            session.addExecutedCommand(command, args, rawArgs);
        }
        session.executeRawCommandAux(command, args, resp, rawArgs);

        if (pw != null) {
            pw.println();
            pw.flush();
        }
    }

    /**
     * Get the response and associate it with a session, so that such things as the
     * console can setup column seperators
     * etc
     *
     * @param pw
     * @param session
     * @return
     */
    protected Response getResponse(PrintWriter pw, CommandSession session) {
        Response response = null;
        response = new ConsoleResponse(pw, 0);
        Response resp = getResponseWithCSV(new PackingResponse(response, 2), session);
        resp.setCommandSession(session);
        return resp;
    }

    protected Response getResponseWithCSV(Response response, CommandSession session) {
        String csvFile = session.getCSVFile();
        if (!StringUtil.nullOrEmptyString(csvFile)) {
            File f = new File(csvFile);
            CSVResponse csvResp = new CSVResponse(f);
            return new TeeResponse(response, csvResp);

        }
        return response;
    }

    public enum LineTerminator {
        SingleReturn, DoubleReturn, SemiColon
    }
}
