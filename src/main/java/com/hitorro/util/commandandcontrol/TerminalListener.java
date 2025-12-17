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
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.EnhancedThreadFactory;
import com.hitorro.util.json.keys.IntegerProperty;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class TerminalListener implements Runnable {
    public static final IntegerProperty PortNumber = new IntegerProperty("telnet.port", "port that the telnet service runs on", 5050);
    private static EnhancedThreadFactory m_factory = new EnhancedThreadFactory("Terminal",
            "Terminal", true);
    private static BlockingQueue<Runnable> m_workQueue = new PriorityBlockingQueue<Runnable>(20);
    private static ThreadPoolExecutor s_exec = new ThreadPoolExecutor(5, 20, 100, TimeUnit.SECONDS, m_workQueue, m_factory);
    public CommandRegistry m_Command_registry;
    private ServerSocket m_server;
    private boolean m_running = true;
    private int m_portNumber = 5000;
    private boolean m_incrementPortIfUnavailable = true;

    public TerminalListener() {
        m_Command_registry = CommandRegistry.getRegistry();
    }

    public int getPort() {
        return m_portNumber;
    }

    public int listenSocket() {
        m_portNumber = PortNumber.apply();
        while (true) {
            try {
                m_server = new ServerSocket(m_portNumber);
                return m_portNumber;
            } catch (IOException e) {
                Log.commands.info("Could not listen on port %s", m_portNumber);
                if (m_incrementPortIfUnavailable == false) {
                    return -1;
                }
            }
            m_portNumber++;
        }

    }

    public void acceptLoop() {
        CommandSession session = null;
        Socket connection = null;
        while (m_running) {
            try {
                connection = m_server.accept();

                session = new CommandSession();
                session.setSession(connection.getInputStream(), connection
                        .getOutputStream());
                String name = Fmt.S("From: %s", connection.getInetAddress()
                        .toString());
                Log.commands.debug("Creating telnet thread for connection %s",
                        name);
                s_exec.execute(session);
            } catch (IOException e) {
                Log.commands.error("Accept failed: %s", this.m_portNumber);
            } catch (RejectedExecutionException e) {
                Log.commands.info("Request got rejected due max capacity %s %e", e, e);
                try {
                    connection.close();
                } catch (IOException e1) {
                    Log.commands.error(
                            "Got an exception closing connection , %s %e", e1,
                            e1);
                }
            }
        }
    }

    public void run() {
        int port = listenSocket();
        if (port == -1) {
            Log.commands.error("Unable to configure tel");
            return;
        }

        Log.commands.info("Listening on port %s", port);
        acceptLoop();
    }

    public void initTCP(int initialPort, boolean allowFreePortScaning,
                        boolean httpOnly) {
        m_incrementPortIfUnavailable = allowFreePortScaning;
        m_portNumber = initialPort;
    }
}
