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
