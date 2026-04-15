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

import com.hitorro.util.commandandcontrol.ssh.SshKeyManager;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;

import java.io.IOException;

/**
 * SSH server listener for the command and control framework.
 * Provides SSH access to the command execution infrastructure.
 */
public class SshListener implements Runnable {
    // Configuration properties
    public static IntegerProperty PortNumber = new IntegerProperty("ssh.port",
            "port that the SSH service runs on", 2222);
    public static BooleanProperty Enabled = new BooleanProperty("ssh.enabled",
            "enable SSH server", true);
    public static StringProperty Username = new StringProperty("ssh.auth.username",
            "SSH authentication username", "admin");
    public static StringProperty Password = new StringProperty("ssh.auth.password",
            "SSH authentication password", "admin");

    private SshServer sshServer;
    private final SshKeyManager keyManager;
    private final int port;
    private final boolean enabled;

    public SshListener() {
        this.port = PortNumber.apply();
        this.enabled = Enabled.apply();
        this.keyManager = new SshKeyManager();
    }

    /**
     * Initializes and configures the SSH server
     */
    private void initializeSshServer() throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);

        // Set up host key provider
        sshServer.setKeyPairProvider(keyManager.getKeyProvider());

        // Configure password authentication
        sshServer.setPasswordAuthenticator(createPasswordAuthenticator());

        // Set up shell factory to create command sessions
        sshServer.setShellFactory(channel -> {
            String remoteAddress = channel.getSession().getClientAddress().toString();
            return new SshCommandSession(remoteAddress);
        });

        Log.util.info("SSH server configured on port %s with host key at %s",
                port, keyManager.getKeyPath());
    }

    /**
     * Creates a password authenticator based on configuration
     */
    private PasswordAuthenticator createPasswordAuthenticator() {
        final String configuredUsername = Username.apply();
        final String configuredPassword = Password.apply();

        return (username, password, session) -> {
            boolean authenticated = configuredUsername.equals(username) &&
                    configuredPassword.equals(password);

            if (authenticated) {
                Log.commands.info("SSH authentication successful for user '%s' from %s",
                        username, session.getClientAddress());
            } else {
                Log.commands.warn("SSH authentication failed for user '%s' from %s",
                        username, session.getClientAddress());
            }

            return authenticated;
        };
    }

    /**
     * Gets the configured port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Checks if SSH server is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void run() {
        if (!enabled) {
            Log.util.info("SSH server is disabled via configuration");
            return;
        }

        try {
            initializeSshServer();
            sshServer.start();
            Log.util.info("SSH server started successfully on port %s", port);

            // Keep the server running
            synchronized (this) {
                while (sshServer.isStarted()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Log.util.error("Failed to start SSH server on port %s: %s %e", port, e, e);
        } finally {
            shutdown();
        }
    }

    /**
     * Shuts down the SSH server gracefully
     */
    public void shutdown() {
        if (sshServer != null && sshServer.isStarted()) {
            try {
                Log.util.info("Shutting down SSH server on port %s", port);
                sshServer.stop();
            } catch (IOException e) {
                Log.util.error("Error shutting down SSH server: %s %e", e, e);
            }
        }
    }
}
