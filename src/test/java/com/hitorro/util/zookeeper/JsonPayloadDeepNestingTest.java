/*
 * Copyright (c) 2006-2025 Chris Collins
 */
package com.hitorro.util.zookeeper;

import com.hitorro.util.json.JsonMappers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ZooKeeper payloads are the node's compact JSON, as on 3.x, also when it nests 600 levels deep
 * (Jackson 2's limit was 1000; Jackson 3's {@code JsonNode.toString()} throws past 500).
 */
class JsonPayloadDeepNestingTest {

    static final String DEEP = "[".repeat(600) + "]".repeat(600);
    static final String SMALL = "{\"b\":1,\"a\":[true,null,\"caf\u00e9 \\\"q\\\" \\n\"],\"d\":1.5}";

    @Test
    void jsonPayloadStoresCompactJson() {
        for (String json : new String[]{SMALL, "{\"deep\":" + DEEP + "}"}) {
            JsonPayload p = new JsonPayload();
            p.setJsonNode(JsonMappers.json().readTree(json));
            assertThat(p.message).isEqualTo(json);
            assertThat(JsonMappers.json().writeValueAsString(p.getAsJsonNode())).isEqualTo(json);
        }
    }

    @Test
    void znodeBytesAreCompactJson() {
        for (String json : new String[]{SMALL, "{\"deep\":" + DEEP + "}"}) {
            assertThat(new String(ZKContext.nodeBytes(JsonMappers.json().readTree(json)))).isEqualTo(json);
        }
    }
}
