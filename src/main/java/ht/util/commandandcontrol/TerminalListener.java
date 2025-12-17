package ht.util.commandandcontrol;

import ht.util.core.Log;
import ht.util.core.string.Fmt;
import ht.util.core.thread.EnhancedThreadFactory;
import ht.util.json.keys.IntegerProperty;

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
