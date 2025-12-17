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
package ht.util.redis;

import ht.util.core.string.Fmt;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisTaskQueueBase {
    protected String host;
    protected int port;
    protected String password;
    protected int timeoutSeconds;

    protected RedisClient redisClient;
    protected StatefulRedisConnection<String, String> connection;

    protected Map<String, SynchronousRedisTaskQueue> queues = new HashMap();

    public RedisTaskQueueBase(String host, int port, String password, int timeoutSeconds) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.timeoutSeconds = timeoutSeconds;
        String url = Fmt.S("redis://%s:%s", host, port);

        redisClient = RedisClient.create(url);
        redisClient.setDefaultTimeout(timeoutSeconds, TimeUnit.SECONDS);
        connection = redisClient.connect();
    }

    public SynchronousRedisTaskQueue getSyncQueue(String queueName) {
        synchronized (queues) {
            SynchronousRedisTaskQueue q = queues.get(queueName);
            if (q != null) {
                return q;
            }
            q = new SynchronousRedisTaskQueue(this, queueName, timeoutSeconds);
            queues.put(queueName, q);
            return q;
        }

    }
}
