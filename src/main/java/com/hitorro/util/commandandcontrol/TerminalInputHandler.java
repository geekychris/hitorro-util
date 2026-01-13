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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles terminal input with support for both line-based and character-based
 * modes.
 * Provides line editing capabilities including backspace, Ctrl+C, Ctrl+D, and
 * command history.
 */
public class TerminalInputHandler {
    private final InputStream in;
    private final OutputStream out;
    private boolean echoEnabled = true;
    private boolean characterMode = false;
    private final StringBuilder commandBuffer = new StringBuilder();
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private String currentCommand = "";

    // Control characters
    private static final int BACKSPACE = 8;
    private static final int DELETE = 127;
    private static final int CTRL_C = 3;
    private static final int CTRL_D = 4;
    private static final int CR = 13; // Carriage return
    private static final int LF = 10; // Line feed
    private static final int SEMICOLON = 59;
    private static final int ESC = 27;

    public TerminalInputHandler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Enable or disable character echo.
     * 
     * @param echo true to echo characters, false to suppress echo
     */
    public void setEchoEnabled(boolean echo) {
        this.echoEnabled = echo;
    }

    /**
     * Set character mode vs line mode.
     * In character mode, commands execute on Enter without requiring semicolon.
     * In line mode, semicolon is required as terminator.
     * 
     * @param characterMode true for character mode (SSH), false for line mode
     *                      (telnet)
     */
    public void setCharacterMode(boolean characterMode) {
        this.characterMode = characterMode;
    }

    /**
     * Clear the current line on the terminal.
     */
    private void clearLine() throws IOException {
        if (echoEnabled) {
            // Move cursor to beginning of line and clear to end
            out.write("\r".getBytes());
            for (int i = 0; i < commandBuffer.length() + 10; i++) {
                out.write(" ".getBytes());
            }
            out.write("\r".getBytes());
            out.flush();
        }
    }

    /**
     * Redraw the current command buffer.
     */
    private void redrawLine(String prompt) throws IOException {
        if (echoEnabled) {
            out.write(prompt.getBytes());
            out.write(commandBuffer.toString().getBytes());
            out.flush();
        }
    }

    /**
     * Read a complete command from the input stream.
     * Handles backspace, Ctrl+C, Ctrl+D, arrow keys, and line editing.
     * 
     * @param prompt the prompt to display (for redrawing with history)
     * @return the command string, or null on EOF
     * @throws IOException if an I/O error occurs
     */
    public String readCommand(String prompt) throws IOException {
        commandBuffer.setLength(0);
        historyIndex = -1;
        currentCommand = "";
        boolean lastWasCR = false;

        while (true) {
            int ch = in.read();

            if (ch == -1) {
                // EOF - return accumulated buffer or null
                if (commandBuffer.length() > 0) {
                    String cmd = commandBuffer.toString();
                    addToHistory(cmd);
                    return cmd;
                }
                return null;
            }

            // Handle Ctrl+C - cancel current command
            if (ch == CTRL_C) {
                commandBuffer.setLength(0);
                if (echoEnabled) {
                    out.write("^C\r\n".getBytes());
                    out.flush();
                }
                historyIndex = -1;
                currentCommand = "";
                continue;
            }

            // Handle Ctrl+D - EOF on empty line
            if (ch == CTRL_D) {
                if (commandBuffer.length() == 0) {
                    return null;
                }
                // If buffer has content, treat as regular character
            }

            // Handle ANSI escape sequences (arrow keys, etc.)
            if (ch == ESC) {
                // Read the next character
                int next = in.read();
                if (next == '[') {
                    // This is a CSI sequence
                    int code = in.read();

                    // Handle arrow keys
                    if (code == 'A') {
                        // Up arrow - previous command in history
                        if (historyIndex == -1) {
                            // Save current command
                            currentCommand = commandBuffer.toString();
                            historyIndex = commandHistory.size() - 1;
                        } else if (historyIndex > 0) {
                            historyIndex--;
                        }

                        if (historyIndex >= 0 && historyIndex < commandHistory.size()) {
                            clearLine();
                            commandBuffer.setLength(0);
                            commandBuffer.append(commandHistory.get(historyIndex));
                            redrawLine(prompt);
                        }
                    } else if (code == 'B') {
                        // Down arrow - next command in history
                        if (historyIndex != -1) {
                            historyIndex++;
                            if (historyIndex >= commandHistory.size()) {
                                // Restore current command
                                historyIndex = -1;
                                clearLine();
                                commandBuffer.setLength(0);
                                commandBuffer.append(currentCommand);
                                redrawLine(prompt);
                            } else {
                                clearLine();
                                commandBuffer.setLength(0);
                                commandBuffer.append(commandHistory.get(historyIndex));
                                redrawLine(prompt);
                            }
                        }
                    }
                    // Ignore left/right arrows (C and D) for now
                    continue;
                } else if (next != -1) {
                    // Put back the character if it's not part of escape sequence
                    // Since we can't unread, just ignore it
                    continue;
                }
            }

            // Handle backspace/delete
            if (ch == BACKSPACE || ch == DELETE) {
                if (commandBuffer.length() > 0) {
                    commandBuffer.setLength(commandBuffer.length() - 1);
                    if (echoEnabled) {
                        // Erase character: backspace, space, backspace
                        out.write("\b \b".getBytes());
                        out.flush();
                    }
                }
                continue;
            }

            // Handle tab completion
            if (ch == '\t') {
                handleTabCompletion();
                continue;
            }

            // Handle line endings
            if (ch == CR || ch == LF) {
                // Skip LF if it follows CR (Windows-style CRLF)
                if (ch == LF && lastWasCR) {
                    lastWasCR = false;
                    continue;
                }
                lastWasCR = (ch == CR);

                if (echoEnabled) {
                    out.write("\r\n".getBytes());
                    out.flush();
                }

                // In character mode, Enter executes the command
                if (characterMode) {
                    String cmd = commandBuffer.toString();
                    addToHistory(cmd);
                    return cmd;
                }

                // In line mode, check for semicolon terminator
                String cmd = commandBuffer.toString();
                String trimmed = cmd.trim();
                if (trimmed.endsWith(";")) {
                    // Return the command WITH the semicolon - executeCommand will handle removal
                    addToHistory(trimmed);
                    return trimmed;
                }

                // No semicolon - this is a continuation line
                // Add newline and continue reading
                commandBuffer.append('\n');
                continue;
            }

            lastWasCR = false;

            // Regular character - add to buffer
            commandBuffer.append((char) ch);

            // Echo character if enabled
            if (echoEnabled) {
                out.write(ch);
                out.flush();
            }

            // Note: In line mode, we wait for Enter after semicolon
            // The semicolon check happens when Enter is pressed (lines 242-249)
        }
    }

    /**
     * Add a command to history if it's not empty and not a duplicate of the last
     * command.
     */
    private void addToHistory(String command) {
        if (command != null && !command.trim().isEmpty()) {
            // Don't add if it's the same as the last command
            if (commandHistory.isEmpty() || !commandHistory.get(commandHistory.size() - 1).equals(command)) {
                commandHistory.add(command);
                // Limit history size to 100 commands
                if (commandHistory.size() > 100) {
                    commandHistory.remove(0);
                }
            }
        }
    }

    /**
     * Get the underlying input stream.
     * 
     * @return the input stream
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Get the underlying output stream.
     * 
     * @return the output stream
     */
    public OutputStream getOutputStream() {
        return out;
    }

    /**
     * Handle tab completion for command names.
     * Attempts to complete the current command based on registered commands.
     */
    private void handleTabCompletion() throws IOException {
        String current = commandBuffer.toString().trim();

        // Only complete if we're at the beginning (no spaces = command name completion)
        if (current.contains(" ")) {
            // For now, don't handle argument completion
            return;
        }

        // Get matching commands from registry
        java.util.List<String> matches = new java.util.ArrayList<>();
        CommandRegistry registry = CommandRegistry.getRegistry();

        for (Command cmd : registry.getCommands()) {
            String cmdName = cmd.getCommand();
            if (cmdName.startsWith(current)) {
                matches.add(cmdName);
            }
        }

        if (matches.isEmpty()) {
            // No matches - do nothing
            return;
        } else if (matches.size() == 1) {
            // Single match - complete it
            String completion = matches.get(0).substring(current.length());
            commandBuffer.append(completion);
            if (echoEnabled) {
                out.write(completion.getBytes());
                out.flush();
            }
        } else {
            // Multiple matches - find common prefix
            String commonPrefix = findCommonPrefix(matches);
            if (commonPrefix.length() > current.length()) {
                // Complete to common prefix
                String completion = commonPrefix.substring(current.length());
                commandBuffer.setLength(0);
                commandBuffer.append(commonPrefix);
                if (echoEnabled) {
                    out.write(completion.getBytes());
                    out.flush();
                }
            } else {
                // Show all matches
                if (echoEnabled) {
                    out.write("\r\n".getBytes());
                    for (String match : matches) {
                        out.write((match + "\r\n").getBytes());
                    }
                    // Redraw prompt and current command
                    out.write(("\u001B[32m" + "prompt>" + "\u001B[0m" + commandBuffer.toString()).getBytes());
                    out.flush();
                }
            }
        }
    }

    /**
     * Find the longest common prefix among a list of strings.
     */
    private String findCommonPrefix(java.util.List<String> strings) {
        if (strings.isEmpty()) {
            return "";
        }
        String prefix = strings.get(0);
        for (int i = 1; i < strings.size(); i++) {
            while (!strings.get(i).startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) {
                    return "";
                }
            }
        }
        return prefix;
    }
}
