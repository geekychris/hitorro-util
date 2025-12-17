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
package com.hitorro.util.redis;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.propaccess.Propaccess;
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