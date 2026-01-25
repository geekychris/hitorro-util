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
package com.hitorro.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JSONUtil Tests")
class JSONUtilTest {

    @Nested
    @DisplayName("Null or Empty Checks")
    class NullOrEmptyChecks {

        @Test
        @DisplayName("Should detect null node")
        void shouldDetectNullNode() {
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("Should detect null JSON node")
        void shouldDetectNullJsonNode() {
            JsonNode node = JsonNodeFactory.instance.nullNode();
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(node)).isTrue();
        }

        @Test
        @DisplayName("Should detect empty array")
        void shouldDetectEmptyArray() {
            ArrayNode emptyArray = JsonNodeFactory.instance.arrayNode();
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(emptyArray)).isTrue();
        }

        @Test
        @DisplayName("Should detect empty object")
        void shouldDetectEmptyObject() {
            ObjectNode emptyObject = JsonNodeFactory.instance.objectNode();
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(emptyObject)).isTrue();
        }

        @Test
        @DisplayName("Should not detect non-empty array as empty")
        void shouldNotDetectNonEmptyArrayAsEmpty() {
            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            array.add("value");
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(array)).isFalse();
        }

        @Test
        @DisplayName("Should not detect non-empty object as empty")
        void shouldNotDetectNonEmptyObjectAsEmpty() {
            ObjectNode object = JsonNodeFactory.instance.objectNode();
            object.put("key", "value");
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(object)).isFalse();
        }

        @Test
        @DisplayName("Should not detect text node as empty")
        void shouldNotDetectTextNodeAsEmpty() {
            JsonNode textNode = JsonNodeFactory.instance.textNode("text");
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(textNode)).isFalse();
        }

        @Test
        @DisplayName("Should not detect number node as empty")
        void shouldNotDetectNumberNodeAsEmpty() {
            JsonNode numberNode = JsonNodeFactory.instance.numberNode(42);
            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(numberNode)).isFalse();
        }
    }

    @Nested
    @DisplayName("Array Conversions")
    class ArrayConversions {

        @Test
        @DisplayName("Should convert list to JSON string array")
        void shouldConvertListToJsonStringArray() {
            List<String> list = Arrays.asList("apple", "banana", "cherry");

            ArrayNode result = com.hitorro.util.json.JSONUtil.toJsonStringArray(list);

            assertThat(result.size()).isEqualTo(3);
            assertThat(result.get(0).asText()).isEqualTo("apple");
            assertThat(result.get(1).asText()).isEqualTo("banana");
            assertThat(result.get(2).asText()).isEqualTo("cherry");
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() {
            List<String> list = Arrays.asList();

            ArrayNode result = com.hitorro.util.json.JSONUtil.toJsonStringArray(list);

            assertThat(result.size()).isZero();
        }

        @Test
        @DisplayName("Should convert objects using toString")
        void shouldConvertObjectsUsingToString() {
            List<Integer> list = Arrays.asList(1, 2, 3);

            ArrayNode result = com.hitorro.util.json.JSONUtil.toJsonStringArray(list);

            assertThat(result.size()).isEqualTo(3);
            assertThat(result.get(0).asText()).isEqualTo("1");
            assertThat(result.get(1).asText()).isEqualTo("2");
            assertThat(result.get(2).asText()).isEqualTo("3");
        }
    }

    @Nested
    @DisplayName("Map to JSON Conversions")
    class MapToJsonConversions {

        @Test
        @DisplayName("Should convert map to JSON object")
        void shouldConvertMapToJsonObject() {
            Map<String, String> map = new HashMap<>();
            map.put("name", "John");
            map.put("city", "New York");
            map.put("country", "USA");

            ObjectNode result = com.hitorro.util.json.JSONUtil.map2json(map);

            assertThat(result.size()).isEqualTo(3);
            assertThat(result.get("name").asText()).isEqualTo("John");
            assertThat(result.get("city").asText()).isEqualTo("New York");
            assertThat(result.get("country").asText()).isEqualTo("USA");
        }

        @Test
        @DisplayName("Should handle empty map")
        void shouldHandleEmptyMap() {
            Map<String, String> map = new HashMap<>();

            ObjectNode result = com.hitorro.util.json.JSONUtil.map2json(map);

            assertThat(result.size()).isZero();
        }

        @Test
        @DisplayName("Should handle map with null values")
        void shouldHandleMapWithNullValues() {
            Map<String, String> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", null);

            ObjectNode result = com.hitorro.util.json.JSONUtil.map2json(map);

            assertThat(result.has("key1")).isTrue();
            assertThat(result.has("key2")).isTrue();
        }

        @Test
        @DisplayName("Should handle special characters in keys and values")
        void shouldHandleSpecialCharactersInKeysAndValues() {
            Map<String, String> map = new HashMap<>();
            map.put("email", "test@example.com");
            map.put("path", "/users/john/documents");
            map.put("message", "Hello, World!");

            ObjectNode result = com.hitorro.util.json.JSONUtil.map2json(map);

            assertThat(result.get("email").asText()).isEqualTo("test@example.com");
            assertThat(result.get("path").asText()).isEqualTo("/users/john/documents");
            assertThat(result.get("message").asText()).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("Value Merging")
    class ValueMerging {

        @Test
        @DisplayName("Should merge object values with token")
        void shouldMergeObjectValuesWithToken() {
            ObjectNode object = JsonNodeFactory.instance.objectNode();
            object.put("first", "John");
            object.put("last", "Doe");

            String result = com.hitorro.util.json.JSONUtil.mergeValues(object, " ");

            // Note: HashMap ordering may vary, so we check both possibilities
            assertThat(result).matches("(John Doe|Doe John)");
        }

        @Test
        @DisplayName("Should merge with custom delimiter")
        void shouldMergeWithCustomDelimiter() {
            ObjectNode object = JsonNodeFactory.instance.objectNode();
            object.put("a", "1");
            object.put("b", "2");
            object.put("c", "3");

            String result = com.hitorro.util.json.JSONUtil.mergeValues(object, ",");

            assertThat(result).contains("1");
            assertThat(result).contains("2");
            assertThat(result).contains("3");
            assertThat(result).contains(",");
        }

        @Test
        @DisplayName("Should handle single value")
        void shouldHandleSingleValue() {
            ObjectNode object = JsonNodeFactory.instance.objectNode();
            object.put("only", "value");

            String result = com.hitorro.util.json.JSONUtil.mergeValues(object, ";");

            assertThat(result).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandling {

        @Test
        @DisplayName("Should add exception callstack to JSON")
        void shouldAddExceptionCallstackToJson() {
            Exception exception = new RuntimeException("Test exception");

            JsonNode result = com.hitorro.util.json.JSONUtil.addCallstack(exception);

            assertThat(result.isArray()).isTrue();
            assertThat(result.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should include stack trace elements")
        void shouldIncludeStackTraceElements() {
            Exception exception = new RuntimeException("Test exception");

            JsonNode result = com.hitorro.util.json.JSONUtil.addCallstack(exception);

            ArrayNode array = (ArrayNode) result;
            boolean foundTestClass = false;
            for (JsonNode element : array) {
                String trace = element.asText();
                if (trace.contains("JSONUtilTest")) {
                    foundTestClass = true;
                    break;
                }
            }

            assertThat(foundTestClass).isTrue();
        }

        @Test
        @DisplayName("Should add callstack chain for nested exceptions")
        void shouldAddCallstackChainForNestedExceptions() {
            Exception cause = new IllegalArgumentException("Root cause");
            Exception wrapper = new RuntimeException("Wrapper exception", cause);

            JsonNode result = com.hitorro.util.json.JSONUtil.addCallstackChain(wrapper);

            assertThat(result).isNotNull();
            if (result != null) {
                assertThat(result.isObject()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle very large arrays")
        void shouldHandleVeryLargeArrays() {
            ArrayNode largeArray = JsonNodeFactory.instance.arrayNode();
            for (int i = 0; i < 10000; i++) {
                largeArray.add(i);
            }

            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(largeArray)).isFalse();
            assertThat(largeArray.size()).isEqualTo(10000);
        }

        @Test
        @DisplayName("Should handle nested objects")
        void shouldHandleNestedObjects() {
            ObjectNode nested = JsonNodeFactory.instance.objectNode();
            nested.put("inner", "value");

            ObjectNode outer = JsonNodeFactory.instance.objectNode();
            outer.set("nested", nested);

            assertThat(com.hitorro.util.json.JSONUtil.nullOrEmpty(outer)).isFalse();
        }

        @Test
        @DisplayName("Should handle mixed type arrays")
        void shouldHandleMixedTypeArrays() {
            List<Object> mixed = Arrays.asList("string", 123, true);

            ArrayNode result = com.hitorro.util.json.JSONUtil.toJsonStringArray(mixed);

            assertThat(result.size()).isEqualTo(3);
            assertThat(result.get(0).asText()).isEqualTo("string");
            assertThat(result.get(1).asText()).isEqualTo("123");
            assertThat(result.get(2).asText()).isEqualTo("true");
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            Map<String, String> map = new HashMap<>();
            map.put("greeting", "Hello 世界");
            map.put("emoji", "🎉");

            ObjectNode result = com.hitorro.util.json.JSONUtil.map2json(map);

            assertThat(result.get("greeting").asText()).isEqualTo("Hello 世界");
            assertThat(result.get("emoji").asText()).isEqualTo("🎉");
        }
    }
}
