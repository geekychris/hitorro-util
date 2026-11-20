/*
 * Copyright (c) 2006-2025 Chris Collins
 */
package com.hitorro.util.redis;

import com.hitorro.util.json.JsonMappers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Jobs pushed to Redis are the job node's compact JSON, as on 3.x, also when the payload nests 600
 * levels deep (Jackson 2's limit was 1000; Jackson 3's {@code JsonNode.toString()} throws past 500).
 */
class RedisJobTextDeepNestingTest {

    @Test
    void jobTextIsCompactJson() {
        String deep = "[".repeat(600) + "]".repeat(600);
        for (String json : new String[]{"{\"payload\":{\"x\":[1,\"\u00e9\"]},\"returnChannel\":\"q-return-1\"}", "{\"payload\":" + deep + "}"}) {
            assertThat(SynchronousRedisTaskQueue.jobText(JsonMappers.json().readTree(json))).isEqualTo(json);
        }
    }
}
