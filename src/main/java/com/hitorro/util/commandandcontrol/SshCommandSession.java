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

import com.hitorro.util.core.Log;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SSH-specific command session that wraps the existing CommandSession
 * infrastructure.
 * Implements Apache SSHD's Command interface to provide SSH shell
 * functionality.
 */
public class SshCommandSession implements Command {
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private CommandSession commandSession;
    private Thread sessionThread;
    private final String remoteAddress;

    public SshCommandSession(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        Log.commands.info("SSH session started from %s", remoteAddress);

        // Print welcome banner (same as telnet)
        printWelcomeBanner();

        // Create and configure the command session
        commandSession = new CommandSession();
        // Use line mode (same as telnet) - semicolon required to execute
        // commandSession.setCharacterMode(true);
        commandSession.setSession(in, out);

        // Run the command session in a separate thread
        sessionThread = new Thread(() -> {
            try {
                commandSession.run();
            } catch (Exception e) {
                Log.commands.error("Error in SSH command session: %s %e", e, e);
            } finally {
                if (callback != null) {
                    callback.onExit(0);
                }
            }
        }, "SSH-Session-" + remoteAddress);

        sessionThread.start();
    }

    private void printWelcomeBanner() throws IOException {
        // Print ASCII art
        String[] welcome = com.hitorro.util.commandandcontrol.telnet.shell.Welcome.getPicture(46);
        for (String line : welcome) {
            out.write(line.getBytes());
        }
        out.write("\r\n".getBytes());

        // Print colored banner (without colors for SSH - just plain text)
        out.write("HiTorro////Command Shell\r\n\r\n".getBytes());

        // Print welcome message with connection info
        String connected = "Welcome " + remoteAddress + "\r\n\r\n";
        out.write(connected.getBytes());
        out.flush();
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        Log.commands.info("SSH session destroyed from %s", remoteAddress);

        // Signal the command session to exit
        if (commandSession != null) {
            commandSession.exitSession();
        }

        // Wait for the session thread to complete
        if (sessionThread != null && sessionThread.isAlive()) {
            try {
                sessionThread.join(5000); // Wait up to 5 seconds
                if (sessionThread.isAlive()) {
                    sessionThread.interrupt();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Close streams
        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            Log.commands.debug("Error closing input stream: %s", e.getMessage());
        }

        try {
            if (out != null)
                out.close();
        } catch (IOException e) {
            Log.commands.debug("Error closing output stream: %s", e.getMessage());
        }

        try {
            if (err != null)
                err.close();
        } catch (IOException e) {
            Log.commands.debug("Error closing error stream: %s", e.getMessage());
        }
    }
}
