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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.String2JsonMapper;
import com.hitorro.util.json.keys.propaccess.PAContext;
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

    public JsonNode execute(JsonNode in) {
        ObjectNode jobNode = JsonNodeFactory.instance.objectNode();
        payload.set(null, jobNode, PAContext.AlwaysCreate, in);

        UUID uuid = UUID.randomUUID();
        String returnChannel = Fmt.S("%s-return-%s", taskQueueName, uuid.toString());
        returnChannelKey.set(null, jobNode, PAContext.AlwaysCreate, com.hitorro.util.json.JSONUtil.ensureJsonNode(returnChannel));
        RedisFuture<Long> out = qBase.connection.async().rpush(taskQueueName, jobNode.toString());

        KeyValue keyValue = qBase.connection.sync().blpop(timeoutSeconds, returnChannel);
        JsonNode retNode = new String2JsonMapper().apply(keyValue.getValue().toString());

        if (retNode != null) {
            return payload.get(null, jobNode, PAContext.AlwaysCreate);
        }
        return null;
    }

    public void executeAsync(JsonNode in) {
        ObjectNode jobNode = JsonNodeFactory.instance.objectNode();
        payload.set(null, jobNode, PAContext.AlwaysCreate, in);

        UUID uuid = UUID.randomUUID();
        String returnChannel = Fmt.S("%s-return-%s", taskQueueName, uuid.toString());
        returnChannelKey.set(null, jobNode, PAContext.AlwaysCreate, com.hitorro.util.json.JSONUtil.ensureJsonNode(returnChannel));
        RedisFuture<Long> out = qBase.connection.async().rpush(taskQueueName, jobNode.toString());


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