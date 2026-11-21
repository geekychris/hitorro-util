/*
 * Copyright (c) 2006-2025 Chris Collins
 */
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.json.JsonMappers;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ActionRequest} serializes its args as compact JSON, as on 3.x, also when they nest 600
 * levels deep (Jackson 2's limit was 1000; Jackson 3's {@code JsonNode.toString()} throws past 500),
 * and reads them back.
 */
class ActionRequestDeepNestingTest {

    @Test
    void argsRoundTripAsCompactJson() throws Exception {
        String deep = "[".repeat(600) + "]".repeat(600);
        String args = "{\"q\":\"caf\u00e9 \\\"x\\\"\",\"deep\":" + deep + "}";
        ActionRequest req = new ActionRequest();
        req.setMethod("search");
        req.setArgs((ObjectNode) JsonMappers.json().readTree(args));

        List<Object> written = new ArrayList<>();
        HTObjectOutputStream out = (HTObjectOutputStream) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HTObjectOutputStream.class}, (p, m, a) -> { if (a != null && a.length == 1) written.add(a[0]); return null; });
        req.serialize(out);
        assertThat(written.get(1)).isEqualTo("search");
        assertThat(written.get(2)).isEqualTo(args);

        Deque<Object> in = new ArrayDeque<>(written);
        HTObjectInputStream inStream = (HTObjectInputStream) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HTObjectInputStream.class}, (p, m, a) -> in.poll());
        ActionRequest back = new ActionRequest();
        back.deserialize(inStream);
        assertThat(back.getMethod()).isEqualTo("search");
        assertThat(JsonMappers.json().writeValueAsString(back.getArgs())).isEqualTo(args);
    }
}
