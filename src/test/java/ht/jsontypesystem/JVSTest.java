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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.testframework.TestPlus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVS (JSON Value System) Tests")
class JVSTest implements TestPlus {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Should create JVS from JSON node")
        void shouldCreateJvsFromJsonNode() {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("name", "test");

            com.hitorro.jsontypesystem.JVS jvs = new com.hitorro.jsontypesystem.JVS(node);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should create empty JVS")
        void shouldCreateEmptyJvs() {
            com.hitorro.jsontypesystem.JVS jvs = new com.hitorro.jsontypesystem.JVS();

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should read JVS from JSON string")
        void shouldReadJvsFromJsonString() {
            String jsonString = "{\"key\":\"value\",\"number\":42}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(jsonString);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle simple JSON objects")
        void shouldHandleSimpleJsonObjects() {
            String jsonString = "{\"name\":\"John\",\"age\":30}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(jsonString);

            assertThat(jvs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Access")
    class PropertyAccess {

        @Test
        @DisplayName("Should have type key property")
        void shouldHaveTypeKeyProperty() {
            assertThat(com.hitorro.jsontypesystem.JVS.typeKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.typeKey.getKey()).isEqualTo("type");
        }

        @Test
        @DisplayName("Should have predefined property accessors")
        void shouldHavePredefinedPropertyAccessors() {
            assertThat(com.hitorro.jsontypesystem.JVS.didKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.idKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.createdKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.modifiedKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.titleKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.bodyKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.domainKey).isNotNull();
            assertThat(com.hitorro.jsontypesystem.JVS.docKey).isNotNull();
        }

        @Test
        @DisplayName("Should have variable delimiters")
        void shouldHaveVariableDelimiters() {
            assertThat(com.hitorro.jsontypesystem.JVS.VariableStart).isEqualTo("${");
            assertThat(com.hitorro.jsontypesystem.JVS.VariableEnd).isEqualTo("}");
        }
    }

    @Nested
    @DisplayName("Comparators and Functions")
    class ComparatorsAndFunctions {

        @Test
        @DisplayName("Should have identity comparator")
        void shouldHaveIdentityComparator() {
            assertThat(com.hitorro.jsontypesystem.JVS.identityComparator).isNotNull();
        }

        @Test
        @DisplayName("Should have key generator function")
        void shouldHaveKeyGeneratorFunction() {
            assertThat(com.hitorro.jsontypesystem.JVS.keyGenerator).isNotNull();
        }

        @Test
        @DisplayName("Identity comparator should compare JVS objects by ID")
        void identityComparatorShouldCompareJvsObjectsById() {
            com.hitorro.jsontypesystem.JVS jvs1 = com.hitorro.jsontypesystem.JVS.read("{\"id\":{\"id\":\"id1\"}}");
            com.hitorro.jsontypesystem.JVS jvs2 = com.hitorro.jsontypesystem.JVS.read("{\"id\":{\"id\":\"id2\"}}");

            int result = com.hitorro.jsontypesystem.JVS.identityComparator.compare(jvs1, jvs2);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(complexJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle arrays in JSON")
        void shouldHandleArraysInJson() {
            String jsonWithArray = "{\"items\":[1,2,3,4,5]}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(jsonWithArray);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle boolean values")
        void shouldHandleBooleanValues() {
            String jsonWithBooleans = "{\"active\":true,\"deleted\":false}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(jsonWithBooleans);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle null values")
        void shouldHandleNullValues() {
            String jsonWithNull = "{\"field\":null,\"other\":\"value\"}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(jsonWithNull);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(emptyJson);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(largeJson.toString());

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in strings")
        void shouldHandleSpecialCharactersInStrings() {
            String specialCharsJson = "{\"text\":\"Hello\\nWorld\\t!\",\"quote\":\"He said \\\"Hi\\\"\"}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(specialCharsJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            String unicodeJson = "{\"greeting\":\"Hello 世界\",\"emoji\":\"🎉\"}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(unicodeJson);

            assertThat(jvs).isNotNull();
        }

        @Test
        @DisplayName("Should handle numbers of various types")
        void shouldHandleNumbersOfVariousTypes() {
            String numbersJson = "{\"int\":42,\"float\":3.14,\"negative\":-100,\"exp\":1.5e10}";

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(numbersJson);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(typedJson);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(timestampedJson);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(userProfile);

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

            com.hitorro.jsontypesystem.JVS jvs = com.hitorro.jsontypesystem.JVS.read(article);

            assertThat(jvs).isNotNull();
        }
    }
}
