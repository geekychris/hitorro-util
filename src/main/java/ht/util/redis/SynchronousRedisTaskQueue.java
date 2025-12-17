package ht.util.redis;

import ht.jsontypesystem.JVS;
import ht.util.core.string.Fmt;
import ht.util.json.keys.propaccess.Propaccess;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.BiConsumer;

public class SynchronousRedisTaskQueue {
    public static Propaccess payload = new Propaccess("payload");
    public static Propaccess returnChannelKey = new Propaccess("return_channel");
    static Mono<String> o;
    private RedisTaskQueueBase qBase;
    private String taskQueueName;
    private int timeoutSeconds;

    SynchronousRedisTaskQueue(RedisTaskQueueBase qBase, String taskQueueName, int timeoutSeconds) {
        this.qBase = qBase;
        this.taskQueueName = taskQueueName;
        this.timeoutSeconds = timeoutSeconds;
    }

    public JVS execute(JVS in) {
        JVS jobJvs = new JVS();
        jobJvs.setJVSChild(payload, in);

        UUID uuid = UUID.randomUUID();
        String returnChannel = Fmt.S("%s-return-%s", taskQueueName, uuid.toString());
        jobJvs.set(returnChannelKey, returnChannel);
        RedisFuture<Long> out = qBase.connection.async().rpush(taskQueueName, jobJvs.getStringRepresentation());

        KeyValue keyValue = qBase.connection.sync().blpop(timeoutSeconds, returnChannel);
        JVS retJvs = JVS.read(keyValue.getValue().toString());

        if (retJvs != null) {
            return jobJvs.getJVSChild(payload);
        }
        return null;
    }

    public void executeAsync(JVS in) {
        JVS jobJvs = new JVS();
        jobJvs.setJVSChild(payload, in);

        UUID uuid = UUID.randomUUID();
        String returnChannel = Fmt.S("%s-return-%s", taskQueueName, uuid.toString());
        jobJvs.set(returnChannelKey, returnChannel);
        RedisFuture<Long> out = qBase.connection.async().rpush(taskQueueName, jobJvs.getStringRepresentation());


        //String s = qBase.connection.sync().lpop(returnChannel);
        qBase.connection.reactive().blpop(100000, returnChannel).subscribe(x -> {
            System.out.println("xxxx" + x);
        });


        //CompletableFuture.supplyAsync(() -> keyFuture);
        /*String ss = keyFuture.getError();



        keyFuture.thenApplyAsync(x-> {
            System.out.println("got:" + x);
            return x;
        });
        keyFuture.thenAccept(x -> {
            System.out.println("got:" + x);
        });*/

        //keyFuture.handleAsync(new ConsFunc());


        /*
        RedisFuture<String> keyFuture = qBase.connection.async().lpop(returnChannel);
        keyFuture.whenCompleteAsync(new Cons());
        */


    }
}

class Cons implements BiConsumer<String, Throwable> {

    @Override
    public void accept(String s, Throwable throwable) {
        System.out.println("Cons got:" + s);
    }
}