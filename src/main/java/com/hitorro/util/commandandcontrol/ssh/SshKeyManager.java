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
package com.hitorro.util.commandandcontrol.ssh;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;

/**
 * Manages SSH host keys for the SSH server.
 * Generates and persists RSA host keys for server identification.
 */
public class SshKeyManager {
    private final Path keyPath;
    private SimpleGeneratorHostKeyProvider keyProvider;

    /**
     * Creates a key manager with default key location in config directory
     */
    public SshKeyManager() {
        this(getDefaultKeyPath());
    }

    /**
     * Creates a key manager with specified key file path
     *
     * @param keyPath Path to the host key file
     */
    public SshKeyManager(Path keyPath) {
        this.keyPath = keyPath;
        initializeKeyProvider();
    }

    /**
     * Gets the default key path in the config directory
     *
     * @return Path to default key location
     */
    private static Path getDefaultKeyPath() {
        String configDirPath = Env.getConfigDir();
        File configDir = new File(configDirPath);
        File sshDir = new File(configDir, "ssh");
        if (!sshDir.exists()) {
            sshDir.mkdirs();
        }
        return new File(sshDir, "hostkey.ser").toPath();
    }

    /**
     * Initializes the key provider, generating keys if necessary
     */
    private void initializeKeyProvider() {
        try {
            keyProvider = new SimpleGeneratorHostKeyProvider(keyPath);
            keyProvider.setAlgorithm("RSA");

            // Load or generate the key
            Iterable<KeyPair> keys = keyProvider.loadKeys(null);
            if (keys.iterator().hasNext()) {
                Log.util.info("Loaded existing SSH host key from %s", keyPath);
            } else {
                Log.util.info("Generated new SSH host key at %s", keyPath);
            }
        } catch (Exception e) {
            Log.util.error("Failed to initialize SSH key provider: %s %e", e, e);
            throw new RuntimeException("Failed to initialize SSH keys", e);
        }
    }

    /**
     * Gets the key provider for use by SSH server
     *
     * @return KeyPairProvider instance
     */
    public KeyPairProvider getKeyProvider() {
        return keyProvider;
    }

    /**
     * Gets the path to the key file
     *
     * @return Path to key file
     */
    public Path getKeyPath() {
        return keyPath;
    }
}
