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
package com.hitorro.util.core.iterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MappingIterator Tests")
class MappingIteratorTest {

    @Nested
    @DisplayName("Basic Mapping")
    class BasicMapping {

        @Test
        @DisplayName("Should map integers to strings")
        void shouldMapIntegersToStrings() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Function<Integer, String> mapper = i -> "num" + i;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .hasSize(5)
                    .containsExactly("num1", "num2", "num3", "num4", "num5");
        }

        @Test
        @DisplayName("Should map strings to uppercase")
        void shouldMapStringsToUppercase() {
            List<String> input = Arrays.asList("hello", "world", "test");
            Function<String, String> mapper = String::toUpperCase;
            
            com.hitorro.util.core.iterator.MappingIterator<String, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .containsExactly("HELLO", "WORLD", "TEST");
        }

        @Test
        @DisplayName("Should map to different types")
        void shouldMapToDifferentTypes() {
            List<String> input = Arrays.asList("1", "2", "3");
            Function<String, Integer> mapper = Integer::parseInt;
            
            com.hitorro.util.core.iterator.MappingIterator<String, Integer> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result)
                    .containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandling {

        @Test
        @DisplayName("Should skip null mapped values")
        void shouldSkipNullMappedValues() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Function<Integer, String> mapper = i -> i % 2 == 0 ? "even" + i : null;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .hasSize(2)
                    .containsExactly("even2", "even4");
        }

        @Test
        @DisplayName("Should handle mapper returning all nulls")
        void shouldHandleMapperReturningAllNulls() {
            List<Integer> input = Arrays.asList(1, 2, 3);
            Function<Integer, String> mapper = i -> null;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            assertThat(iter.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle first element mapping to null")
        void shouldHandleFirstElementMappingToNull() {
            List<Integer> input = Arrays.asList(1, 2, 3);
            Function<Integer, String> mapper = i -> i == 1 ? null : "val" + i;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .containsExactly("val2", "val3");
        }
    }

    @Nested
    @DisplayName("Empty and Edge Cases")
    class EmptyAndEdgeCases {

        @Test
        @DisplayName("Should handle empty iterator")
        void shouldHandleEmptyIterator() {
            List<Integer> input = Arrays.asList();
            Function<Integer, String> mapper = Object::toString;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            assertThat(iter.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle single element")
        void shouldHandleSingleElement() {
            List<Integer> input = Arrays.asList(42);
            Function<Integer, String> mapper = i -> "answer" + i;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .hasSize(1)
                    .containsExactly("answer42");
        }

        @Test
        @DisplayName("Should allow multiple hasNext calls")
        void shouldAllowMultipleHasNextCalls() {
            List<Integer> input = Arrays.asList(1, 2, 3);
            Function<Integer, String> mapper = Object::toString;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.hasNext()).isTrue();
            
            String first = iter.next();
            assertThat(first).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("Complex Transformations")
    class ComplexTransformations {

        @Test
        @DisplayName("Should perform complex object transformations")
        void shouldPerformComplexObjectTransformations() {
            List<Person> input = Arrays.asList(
                    new Person("Alice", 30),
                    new Person("Bob", 25),
                    new Person("Charlie", 35)
            );
            Function<Person, String> mapper = p -> p.name + ":" + p.age;
            
            com.hitorro.util.core.iterator.MappingIterator<com.hitorro.util.core.iterator.MappingIteratorTest.Person, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .containsExactly("Alice:30", "Bob:25", "Charlie:35");
        }

        @Test
        @DisplayName("Should chain transformations")
        void shouldChainTransformations() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4);
            Function<Integer, Integer> doubler = i -> i * 2;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, Integer> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), doubler);
            
            Function<Integer, String> stringifier = i -> "doubled:" + i;
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter2 = new com.hitorro.util.core.iterator.MappingIterator<>(iter, stringifier);
            
            List<String> result = collectToList(iter2);
            
            assertThat(result)
                    .containsExactly("doubled:2", "doubled:4", "doubled:6", "doubled:8");
        }

        @Test
        @DisplayName("Should handle conditional mapping")
        void shouldHandleConditionalMapping() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            Function<Integer, String> mapper = i -> {
                if (i < 5) return "small:" + i;
                if (i > 7) return "large:" + i;
                return null; // Skip 5, 6, 7
            };
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = collectToList(iter);
            
            assertThat(result)
                    .containsExactly("small:1", "small:2", "small:3", "small:4", 
                                   "large:8", "large:9", "large:10");
        }
    }

    @Nested
    @DisplayName("Iterator Protocol")
    class IteratorProtocol {

        @Test
        @DisplayName("Should support standard iteration pattern")
        void shouldSupportStandardIterationPattern() {
            List<Integer> input = Arrays.asList(1, 2, 3);
            Function<Integer, String> mapper = Object::toString;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            List<String> result = new ArrayList<>();
            while (iter.hasNext()) {
                result.add(iter.next());
            }
            
            assertThat(result).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("Should return false after exhaustion")
        void shouldReturnFalseAfterExhaustion() {
            List<Integer> input = Arrays.asList(1, 2);
            Function<Integer, String> mapper = Object::toString;
            
            com.hitorro.util.core.iterator.MappingIterator<Integer, String> iter = new com.hitorro.util.core.iterator.MappingIterator<>(input.iterator(), mapper);
            
            iter.next();
            iter.next();
            
            assertThat(iter.hasNext()).isFalse();
        }
    }

    // Helper methods
    private <T> List<T> collectToList(Iterator<T> iter) {
        List<T> result = new ArrayList<>();
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    // Test helper class
    private static class Person {
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
