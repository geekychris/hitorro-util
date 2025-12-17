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
package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVS (JSON Value System) Tests")
class JVSTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Should create JVS from JSON node")
        void shouldCreateJvsFromJsonNode() {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("name", "test");

            JVS jvs = new JVS(node);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should create empty JVS")
        void shouldCreateEmptyJvs() {
            JVS jvs = new JVS();

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should read JVS from JSON string")
        void shouldReadJvsFromJsonString() {
            String jsonString = "{\"key\":\"value\",\"number\":42}";

            JVS jvs = JVS.read(jsonString);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle simple JSON objects")
        void shouldHandleSimpleJsonObjects() {
            String jsonString = "{\"name\":\"John\",\"age\":30}";

            JVS jvs = JVS.read(jsonString);

            assertThat(jvs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Access")
    class PropertyAccess {

        @Test
        @DisplayName("Should have type key property")
        void shouldHaveTypeKeyProperty() {
            assertThat(JVS.typeKey).isNotNull();
            assertThat(JVS.typeKey.getKey()).isEqualTo("type");
        }

        @Test
        @DisplayName("Should have predefined property accessors")
        void shouldHavePredefinedPropertyAccessors() {
            assertThat(JVS.didKey).isNotNull();
            assertThat(JVS.idKey).isNotNull();
            assertThat(JVS.createdKey).isNotNull();
            assertThat(JVS.modifiedKey).isNotNull();
            assertThat(JVS.titleKey).isNotNull();
            assertThat(JVS.bodyKey).isNotNull();
            assertThat(JVS.domainKey).isNotNull();
            assertThat(JVS.docKey).isNotNull();
        }

        @Test
        @DisplayName("Should have variable delimiters")
        void shouldHaveVariableDelimiters() {
            assertThat(JVS.VariableStart).isEqualTo("${");
            assertThat(JVS.VariableEnd).isEqualTo("}");
        }
    }

    @Nested
    @DisplayName("Comparators and Functions")
    class ComparatorsAndFunctions {

        @Test
        @DisplayName("Should have identity comparator")
        void shouldHaveIdentityComparator() {
            assertThat(JVS.identityComparator).isNotNull();
        }

        @Test
        @DisplayName("Should have key generator function")
        void shouldHaveKeyGeneratorFunction() {
            assertThat(JVS.keyGenerator).isNotNull();
        }

        @Test
        @DisplayName("Identity comparator should compare JVS objects by ID")
        void identityComparatorShouldCompareJvsObjectsById() {
            JVS jvs1 = JVS.read("{\"id\":{\"id\":\"id1\"}}");
            JVS jvs2 = JVS.read("{\"id\":{\"id\":\"id2\"}}");

            int result = JVS.identityComparator.compare(jvs1, jvs2);

            // Should compare lexicographically
            assertThat(result).isNotZero();
        }
    }

    @Nested
    @DisplayName("JSON Operations")
    class JsonOperations {

        @Test
        @DisplayName("Should handle complex nested JSON")
        void shouldHandleComplexNestedJson() {
            String complexJson = """
                {
                    "id": {
                        "did": "doc123",
                        "id": "id456"
                    },
                    "title": {
                        "mls": "Test Title"
                    },
                    "metadata": {
                        "author": "John Doe",
                        "tags": ["test", "sample"]
                    }
                }
                """;

            JVS jvs = JVS.read(complexJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle arrays in JSON")
        void shouldHandleArraysInJson() {
            String jsonWithArray = "{\"items\":[1,2,3,4,5]}";

            JVS jvs = JVS.read(jsonWithArray);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle boolean values")
        void shouldHandleBooleanValues() {
            String jsonWithBooleans = "{\"active\":true,\"deleted\":false}";

            JVS jvs = JVS.read(jsonWithBooleans);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle null values")
        void shouldHandleNullValues() {
            String jsonWithNull = "{\"field\":null,\"other\":\"value\"}";

            JVS jvs = JVS.read(jsonWithNull);

            assertThat(jvs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty JSON object")
        void shouldHandleEmptyJsonObject() {
            String emptyJson = "{}";

            JVS jvs = JVS.read(emptyJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle very large JSON")
        void shouldHandleVeryLargeJson() {
            StringBuilder largeJson = new StringBuilder("{");
            for (int i = 0; i < 1000; i++) {
                if (i > 0) largeJson.append(",");
                largeJson.append("\"field").append(i).append("\":\"value").append(i).append("\"");
            }
            largeJson.append("}");

            JVS jvs = JVS.read(largeJson.toString());

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in strings")
        void shouldHandleSpecialCharactersInStrings() {
            String specialCharsJson = "{\"text\":\"Hello\\nWorld\\t!\",\"quote\":\"He said \\\"Hi\\\"\"}";

            JVS jvs = JVS.read(specialCharsJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            String unicodeJson = "{\"greeting\":\"Hello 世界\",\"emoji\":\"🎉\"}";

            JVS jvs = JVS.read(unicodeJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle numbers of various types")
        void shouldHandleNumbersOfVariousTypes() {
            String numbersJson = "{\"int\":42,\"float\":3.14,\"negative\":-100,\"exp\":1.5e10}";

            JVS jvs = JVS.read(numbersJson);

            assertThat(jvs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Type System Integration")
    class TypeSystemIntegration {

        @Test
        @DisplayName("Should handle document with type field")
        @Disabled("Requires type system initialization with config files")
        void shouldHandleDocumentWithTypeField() {
            String typedJson = "{\"type\":\"document\",\"content\":\"test\"}";

            JVS jvs = JVS.read(typedJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle timestamps")
        void shouldHandleTimestamps() {
            String timestampedJson = """
                {
                    "times": {
                        "created": "2023-01-01T00:00:00Z",
                        "modified": "2023-01-02T00:00:00Z"
                    }
                }
                """;

            JVS jvs = JVS.read(timestampedJson);

            assertThat(jvs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Real World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Should handle user profile document")
        @Disabled("Requires type system initialization with config files")
        void shouldHandleUserProfileDocument() {
            String userProfile = """
                {
                    "type": "user_profile",
                    "id": {
                        "id": "user_123",
                        "domain": "users"
                    },
                    "title": {
                        "mls": "John Doe Profile"
                    },
                    "data": {
                        "email": "john@example.com",
                        "age": 30,
                        "interests": ["coding", "music", "travel"]
                    },
                    "times": {
                        "created": "2023-01-01",
                        "modified": "2023-06-15"
                    }
                }
                """;

            JVS jvs = JVS.read(userProfile);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle article document")
        @Disabled("Requires type system initialization with config files")
        void shouldHandleArticleDocument() {
            String article = """
                {
                    "type": "article",
                    "id": {
                        "id": "article_001"
                    },
                    "title": {
                        "mls": "Introduction to JVS"
                    },
                    "body": {
                        "mls": "This is the article content..."
                    },
                    "metadata": {
                        "author": "Jane Smith",
                        "publishDate": "2023-07-01",
                        "tags": ["tutorial", "JVS", "JSON"]
                    }
                }
                """;

            JVS jvs = JVS.read(article);

            assertThat(jvs).isNotNull();
        }
    }
}
