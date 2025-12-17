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

import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.commandandcontrol.telnet.shell.ShellIo;
import com.hitorro.util.commandandcontrol.telnet.shell.Welcome;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.trie.Trie;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.io.terminal.BasicTerminal;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionData;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Map;

public class TelnetCommandSession extends CommandSession implements Shell {
    /**
     * ESC sequence for deleting a character.
     */
    private static final byte[] deleteChar
            = new byte[]{BasicTerminal.ESC, BasicTerminal.LSB, '1', 'P'};

    /**
     * ESC sequence for deleting a character.
     */
    private static final byte[] insertChar
            = new byte[]{BasicTerminal.ESC, BasicTerminal.LSB, '1', '@'};

    /**
     * ESC sequence for moving back a character.
     */
    private static final byte[] moveBack
            = new byte[]{BasicTerminal.ESC, BasicTerminal.LSB, '1', 'D'};

    /**
     * ESC sequence for saving cursor position.
     */
    private static final byte[] saveCursor
            = new byte[]{BasicTerminal.ESC, BasicTerminal.LSB, 's'};

    /**
     * ESC sequence for restoring cursor position.
     */
    private static final byte[] restoreCursor
            = new byte[]{BasicTerminal.ESC, BasicTerminal.LSB, 'u'};

    /**
     * Connection this shell works on.
     */
    private Connection connection;

    /**
     * For low level terminal IO.
     */
    private ShellIo shellIo = null;
    private String trieCommand = null;
    private Trie argsTrie = null;
    private Command currentCommand = null;
    private int lineNumber = m_commands.size();

    public TelnetCommandSession() {
        Console.println();
    }

    public static Shell createShell() {
        return new TelnetCommandSession();
    }

    /**
     * Get ESC sequece for deleting <code>number</code> characters.
     *
     * @param number Delete this amount of characters.
     */
    private static final byte[] deleteChars(final int number) {
        final StringBuffer result = new StringBuffer();
        result.append((char) BasicTerminal.ESC);
        result.append((char) BasicTerminal.LSB);
        result.append(number);
        result.append('P');
        System.out.println(">>>" + result.toString());
        return result.toString().getBytes();
    }

    public void run(final Connection con) {
        connection = con;
        this.m_os = null;
        shellIo = new ShellIo(connection);
        connection.addConnectionListener(this);
        try {
            shellIo.eraseScreen();
            shellIo.homeCursor();
            writePicture();
            writeWelcomeBanner();
            runAux();

        } catch (Exception e) {
            Log.commands.error(e, e);
        } catch (Error e) {
            Log.commands.error(e, e);
            throw e;
        } catch (Throwable e) {
            Log.commands.error(e, e);
        } finally {
            // close shell process if connection is closed

        }
    }

    private void writeWelcomeBanner() throws IOException {
        shellIo.setBold(true);
        shellIo.setForegroundColor(BasicTerminalIO.RED);
        shellIo.write("HiTorro");
        shellIo.setForegroundColor(BasicTerminalIO.BLUE);
        shellIo.write("/");
        shellIo.setForegroundColor(BasicTerminalIO.GREEN);
        shellIo.write("/");
        shellIo.setForegroundColor(BasicTerminalIO.YELLOW);
        shellIo.write("/");
        shellIo.setForegroundColor(BasicTerminalIO.RED);
        shellIo.write("/");
        shellIo.setForegroundColor(BasicTerminalIO.GREEN);
        shellIo.write("Command Shell");
        shellIo.setBold(false);
        shellIo.write("\r\n\r\n");

        shellIo.setForegroundColor(BasicTerminalIO.GREEN);
        final ConnectionData cd = connection.getConnectionData();
        final String connected = "Welcome " + cd.getHostName() +
                " [" + cd.getHostAddress() + ":" + cd.getPort() + "]";
        shellIo.write(connected + "\r\n\r\n");
        shellIo.setBold(false);
        shellIo.resetAttributes();
        shellIo.flush();
    }

    private void writePicture() throws IOException {
        shellIo.setItalic(false);
        final String[] welcome = Welcome.getPicture(46);
        for (int i = 0; i < welcome.length; i++) {
            shellIo.write(welcome[i]);
        }
        shellIo.write("\r\n");
    }

    public void connectionTimedOut(ConnectionEvent ce) {
        try {
            shellIo.setBold(true);
            shellIo.setForegroundColor(BasicTerminalIO.RED);
            shellIo.write("\r\n");
            shellIo.write("CONNECTION TIMEDOUT");
            shellIo.write("\r\n");
            shellIo.write("Bye bye");
            shellIo.write("\r\n");
            shellIo.flush();

        } catch (Exception e) {
            Log.commands.error(e, e);
        }
        connection.close();
    }

    public void connectionIdle(ConnectionEvent ce) {
        try {
            shellIo.write("\r\n");
            shellIo.write("CONNECTION IDLE (ignored)");
            shellIo.write("\r\n");
            shellIo.flush();
        } catch (Exception e) {
            Log.commands.error(e, e);
        }
    }

    public void connectionLogoutRequest(ConnectionEvent ce) {
        try {
            shellIo.setBold(false);
            shellIo.setForegroundColor(BasicTerminalIO.GREEN);
            shellIo.write("\r\n");
            shellIo.write("CONNECTION LOGOUTREQUEST");
            shellIo.write("\r\n");
            shellIo.write("Bye bye");
            shellIo.write("\r\n");
            shellIo.flush();

        } catch (Exception e) {
            Log.commands.error(e, e);
        }
        connection.close();
    }

    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            shellIo.write("\r\n");
            shellIo.write("CONNECTION BREAK (ignored)");
            shellIo.write("\r\n");
            shellIo.flush();
        } catch (Exception e) {
            Log.commands.error(e, e);
        }
    }

    private void printPrompt() {
        String s = null;
        s = getPrompt();

        try {
            shellIo.setForegroundColor(BasicTerminalIO.GREEN);
            shellIo.write(s);
            //shellIo.setForegroundColor(BasicTerminalIO.BLACK);
            shellIo.resetAttributes();
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        }
    }

    private String getPrompt() {
        String s;
        if (this.m_counter > 0) {
            s = Fmt.S(this.m_promptInteractive, m_counter);
        } else {
            if (getLineNumber() == -1) {
                s = Fmt.S("%s>", this.m_prompt);
            } else {
                s = Fmt.S("%s-%s>", this.m_prompt, getLineNumber());
            }
        }
        return s;
    }

    private boolean computeArgs(String command) {
        if (StringUtil.nullOrEmptyOrBlankString(command)) {
            trieCommand = null;
            return false;
        }
        if (command.equals(trieCommand)) {
            return true;
        }
        currentCommand = CommandRegistry.getRegistry().get(command);
        if (currentCommand == null) {
            return false;
        }
        argsTrie = CommandRegistry.getRegistry().getTrieForArgs(command);
        return argsTrie != null;
    }

    private int runAux() {
        setPrompt(BaseCommandLine.getCommandLine().commandLine);
        printPrompt();
        Trie trie = CommandRegistry.getRegistry().getTrie();

        StringBuilder builder = new StringBuilder();
        try {
            // position within lines

            // line buffer
            final StringBuffer inputBuffer = new StringBuffer();
            // position within line buffer
            int cursor = 0;
            int c = 0;
            int prior = 0;
            do {
                prior = c;
                c = shellIo.read();
                switch (c) {
                    case BasicTerminalIO.DELETE:
                        if (cursor > 0) {
                            inputBuffer.deleteCharAt(cursor - 1);
                            cursor--;
                            shellIo.write((char) TerminalIO.BS);
//                            shellIo.moveLeft(1);
                            shellIo.write(deleteChar);
                        }
                        break;
                    case BasicTerminalIO.TABULATOR:
                        cursor = performTab(inputBuffer, trie, prior, cursor);
                        break;
                    case BasicTerminalIO.BACKSPACE:
                        if (cursor < inputBuffer.length()) {
                            inputBuffer.deleteCharAt(cursor);
                            shellIo.write(deleteChar);
                        }
                        break;

                    case BasicTerminalIO.LEFT:
                        if (cursor > 0) {
                            cursor--;
                            shellIo.write((char) TerminalIO.BS);
                        }
                        break;
                    case BasicTerminalIO.RIGHT:
                        if (cursor < inputBuffer.length()) {
                            shellIo.moveRight(1);
                            cursor++;
                        }
                        break;
                    case BasicTerminalIO.UP:
                        if (m_commands.size() > 0 && getLineNumber() >= 0) {

                            if (inputBuffer.length() > cursor) {
                                shellIo.write(deleteChars(inputBuffer.length() - cursor));
                            }
                            deleteIncludingPrompt(cursor);

                            if (getLineNumber() > 0) {
                                setLineNumber(getLineNumber() - 1);
                            }

                            cursor = writeLine(inputBuffer, builder);

                        }
                        break;
                    case BasicTerminalIO.DOWN:
                        if (getLineNumber() >= 0 && getLineNumber() < m_commands.size()) {
                            if (inputBuffer.length() > cursor) {
                                shellIo.write(deleteChars(inputBuffer.length() - cursor));
                            }
                            deleteIncludingPrompt(cursor);

                            if (getLineNumber() < m_commands.size()) {
                                setLineNumber(getLineNumber() + 1);
                            }

                            cursor = writeLine(inputBuffer, builder);
                        }
                        break;
                    case BasicTerminalIO.ENTER:
                        shellIo.write(BasicTerminalIO.CRLF);
                        String command = inputBuffer.toString();
                        PrintWriter pw = getWriter(this);
                        try {
                            if (exec(command, builder, pw, this, true)) {
                                // XXX lineNumber = m_commands.size() - 1;
                                setLineNumber(m_commands.size());
                            }
                        } catch (PropaccessError propaccessError) {

                        }
                        cursor = 0;
                        inputBuffer.setLength(0);
                        printPrompt();
                        break;
                    default:
                        if (c < 256) {
                            shellIo.write(insertChar);
                            cursor = insertChar(c, inputBuffer, cursor);
                        } else {
                            //unknown char
                        }
                }
            }
            while (connection.isActive() && !m_exit);
        } catch (SocketException e) {
            Log.commands.warn("connection closed");
        } catch (IOException e) {
            Log.commands.warn(e, e);
        }
        return 1;

    }

    private int writeLine(StringBuffer inputBuffer, StringBuilder builder)
            throws IOException {
        int cursor;
        printPrompt();
        String cmd;
        if (m_commands.size() <= getLineNumber()) {
            cmd = "";
        } else {
            CommandMap cm = m_commands.get(getLineNumber());
            cmd = cm.toString();
        }
        shellIo.write(cmd);
        inputBuffer.setLength(0);
        inputBuffer.append(cmd);
        cursor = cmd.length();
        builder.setLength(0);
        return cursor;
    }

    private void deleteIncludingPrompt(int cursor)
            throws IOException {
        String p = getPrompt();
        for (int i = 0; i < cursor + p.length(); i++) {
            shellIo.write((char) TerminalIO.BS);
            shellIo.write(deleteChar);
        }
    }

    private void printArgs(int prior, String command, String startsWith, String completeBuffer) throws IOException {
        if (prior == BasicTerminalIO.TABULATOR) {
            Command help = CommandRegistry.getRegistry().get("help");
            if (help != null) {
                shellIo.write("\n");
                try {
                    exec(Fmt.S("help command=%s startswith=%s;", command, startsWith), new StringBuilder(), null, this, false);
                } catch (PropaccessError propaccessError) {

                }
                this.printPrompt();
                shellIo.write(completeBuffer);
            }
        }
    }

    /**
     * This could be the crappiest code I have ever written....hmm, dont think so hard! Poor state machine thing to
     * attempt some level of auto complete and self help with the verb part of a command. Goes as far as printing all
     * the arguments.
     * <p/>
     * If one wanted to be real smart you could track the complete state machine of arguments and then from each
     * argument allow tab autocomplete by type, of such things as FileProperty or List of valid strings property
     * doodad.
     *
     * @param inputBuffer
     * @param trie
     * @param prior
     * @param cursor
     * @return
     * @throws IOException
     */
    private int performTab(final StringBuffer inputBuffer, final Trie trie, final int prior, int cursor) throws IOException {
        String command = inputBuffer.toString();
        String justCommand = command.trim();
        int endOfCommand = command.indexOf(" ");
        if (endOfCommand > 0) {
            justCommand = command.substring(0, endOfCommand);
            //potentially we already have a command and we are doing argument completion
            if (computeArgs(justCommand)) {
                // we have a trie, lets work backwards from the cursor...that way we may have a better chance at tab completion
                // anywhere in the line
                int lastIndex = command.lastIndexOf(" ", cursor);
                if (lastIndex != -1) {
                    String arg = command.substring(lastIndex, command.length());
                    arg = arg.trim();
                    if (!StringUtil.nullOrEmptyOrBlankString(arg)) {
                        Object k = getComplete(arg, this.argsTrie);
                        if (k == null) {
                            Object o2 = argsTrie.getTrieFor(arg);
                            if (o2 instanceof Trie) {
                                printArgs(prior, justCommand, arg, command);
                            } else if (o2 instanceof DebugCommandArg) {
                                cursor = isDebugArg(o2, arg, inputBuffer, cursor, prior, justCommand, command);
                            }
                        } else {
                            if (k instanceof String) {
                                String str = (String) k;
                                if (!StringUtil.nullOrEmptyString(str)) {
                                    cursor = insertChars(str, inputBuffer, cursor);
                                }
                            } else if (k instanceof DebugCommandArg) {
                                cursor = isDebugArg(k, arg, inputBuffer, cursor, prior, justCommand, command);
                                return cursor;
                            }
                        }
                    }
                }
            }
        }
        Object k = getComplete(command, trie);

        if (k == null) {
            Object o2 = trie.getTrieFor(command);
            if (o2 instanceof Trie) {
                if (prior == BasicTerminalIO.TABULATOR) {
                    Trie t2 = (Trie) o2;
                    if (t2.length() < 100) {
                        shellIo.write("\n");
                        LinkedList ll = t2.getSortedList();
                        for (Object o : ll) {
                            if (o instanceof Command) {
                                shellIo.write(((Command) o).getCommand());
                                shellIo.write("\n");
                            }
                        }
                        this.printPrompt();
                        shellIo.write(command);
                        return cursor;
                    }
                }
            } else if (o2 instanceof Command) {
                cursor = isCommand(o2, command, inputBuffer, cursor, prior);
            }
        } else if (k instanceof String) {
            String str = (String) k;
            if (!StringUtil.nullOrEmptyString(str)) {
                cursor = insertChars(str, inputBuffer, cursor);
            }

        } else if (k instanceof Command) {

            cursor = isCommand(k, command, inputBuffer, cursor, prior);
        }
        printArgs(prior, justCommand, "", command);
        return cursor;
    }

    private int isDebugArg(final Object o2, final String arg, final StringBuffer inputBuffer, int cursor, final int prior, final String justCommand, final String command) throws IOException {
        DebugCommandArg da = (DebugCommandArg) o2;
        String str = da.getName().substring(arg.length());
        cursor = insertChars(str, inputBuffer, cursor);
        printArgs(prior, justCommand, da.getName(), command);
        return cursor;
    }

    private int isCommand(final Object k, final String command, final StringBuffer inputBuffer, int cursor, final int prior) throws IOException {
        Command thisCommand = (Command) k;
        String str = thisCommand.getCommand().substring(command.length());
        cursor = insertChars(str, inputBuffer, cursor);
        if (prior == BasicTerminalIO.TABULATOR) {
            Command help = CommandRegistry.getRegistry().get("help");
            if (help != null) {
                shellIo.write("\n");
                try {
                    exec(Fmt.S("help %s;", thisCommand.getCommand()), new StringBuilder(), null, this, false);
                } catch (PropaccessError propaccessError) {

                }
                this.printPrompt();
                shellIo.write(inputBuffer.toString());
            }
        }
        return cursor;
    }

    private int insertChar(final int c, final StringBuffer inputBuffer, int cursor) throws IOException {
        shellIo.write((char) c);
        inputBuffer.insert(cursor, (char) c);
        cursor++;
        return cursor;
    }

    private int insertChars(String chars, final StringBuffer inputBuffer, int cursor) throws IOException {
        shellIo.write(chars);
        inputBuffer.insert(cursor, chars);
        cursor += chars.length();
        return cursor;
    }

    private Object getComplete(String prefix, final Trie trie) {
        Object o = trie.getTrieFor(prefix);

        if (o instanceof Trie) {
            Trie t = (Trie) o;
            Map m = t.getEntries();
            if (m.size() == 1) {
                for (Object key : m.keySet()) {
                    Object val = m.get(key);
                    if (val instanceof Trie) {
                        Trie t2 = (Trie) val;
                        StringBuilder sb = new StringBuilder();
                        sb.append(key);
                        return getMaxTrie(sb, t2);
                    }
                    return key;
                }
            }
        } else if (o instanceof String) {
            return o;
        }
        return null;
    }

    private String getMaxTrie(StringBuilder sb, Trie t) {
        Map m = t.getEntries();
        if (m.size() == 1) {
            for (Object key : m.keySet()) {
                Object val = m.get(key);
                if (val instanceof Trie) {
                    Trie t2 = (Trie) val;
                    sb.append(key);
                    return getMaxTrie(sb, t2);
                }
                return sb.toString();
            }
        }
        return sb.toString();
    }

    protected void writeShellLine(String line) {
        if (this.shellIo != null) {

            try {
                shellIo.write(line);
                shellIo.write("\n");
            } catch (IOException e) {
                Log.util.error("Exception %s %e", e, e);
            }
        }
    }

    protected void writePromptCommand(final PrintWriter pw, final CommandSession session, final String commandStr) {
        super.writePromptCommand(pw, session, commandStr);
        if (shellIo != null) {
            printPrompt();
            try {
                shellIo.write(commandStr);
                shellIo.write("\n");
            } catch (IOException e) {
                Log.util.error("Exception %s %e", e, e);
            }
        }
    }

    protected Response getResponse(PrintWriter pw, CommandSession session) {
        Response response = null;
        if (shellIo != null) {
            response = new TelnetResponse(shellIo);
        } else {
            response = new ConsoleResponse(pw, 0);
        }
        Response resp = getResponseWithCSV(new PackingResponse(response, 2), session);
        resp.setCommandSession(session);
        return resp;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }
}

