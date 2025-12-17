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
package ht.util.zookeeper.models;

import org.apache.curator.x.async.modeled.NodeName;

import java.util.Objects;
import java.util.UUID;

public class Instance implements NodeName {
    private final String id;
    private final InstanceType type;
    private final String hostname;
    private final int port;

    public Instance() {
        this(UUID.randomUUID().toString(), InstanceType.proxy, "", 0);
    }

    public Instance(String id, InstanceType type, String hostname, int port) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.hostname = Objects.requireNonNull(hostname, "hostname cannot be null");
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public InstanceType getType() {
        return type;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String nodeName() {
        return id;
    }

    @Override
    public String toString() {
        return "Instance{" + "id='" + id + '\'' + ", type=" + type + ", hostname='" + hostname + '\'' + ", port=" + port + '}';
    }
}