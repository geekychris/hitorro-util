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
package com.hitorro.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Counter Tests")
class CounterTest {

    private com.hitorro.util.Counter<String> counter;

    @BeforeEach
    void setUp() {
        counter = new com.hitorro.util.Counter<>();
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should initialize with zero size")
        void shouldInitializeWithZeroSize() {
            assertThat(counter.size()).isZero();
            assertThat(counter.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should return zero for non-existent keys")
        void shouldReturnZeroForNonExistentKeys() {
            assertThat(counter.getCount("nonexistent")).isZero();
        }

        @Test
        @DisplayName("Should set and retrieve counts")
        void shouldSetAndRetrieveCounts() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 3.0);

            assertThat(counter.getCount("apple")).isEqualTo(5.0);
            assertThat(counter.getCount("banana")).isEqualTo(3.0);
            assertThat(counter.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should overwrite existing counts")
        void shouldOverwriteExistingCounts() {
            counter.setCount("apple", 5.0);
            counter.setCount("apple", 10.0);

            assertThat(counter.getCount("apple")).isEqualTo(10.0);
            assertThat(counter.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should clear all entries")
        void shouldClearAllEntries() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 3.0);
            
            counter.clear();

            assertThat(counter.isEmpty()).isTrue();
            assertThat(counter.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Increment Operations")
    class IncrementOperations {

        @Test
        @DisplayName("Should increment count for new key")
        void shouldIncrementCountForNewKey() {
            counter.incrementCount("apple", 5.0);

            assertThat(counter.getCount("apple")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should increment count for existing key")
        void shouldIncrementCountForExistingKey() {
            counter.setCount("apple", 5.0);
            counter.incrementCount("apple", 3.0);

            assertThat(counter.getCount("apple")).isEqualTo(8.0);
        }

        @Test
        @DisplayName("Should increment with negative values")
        void shouldIncrementWithNegativeValues() {
            counter.setCount("apple", 10.0);
            counter.incrementCount("apple", -3.0);

            assertThat(counter.getCount("apple")).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should increment all items in collection")
        void shouldIncrementAllItemsInCollection() {
            counter.incrementAll(Arrays.asList("apple", "banana", "cherry"), 2.0);

            assertThat(counter.getCount("apple")).isEqualTo(2.0);
            assertThat(counter.getCount("banana")).isEqualTo(2.0);
            assertThat(counter.getCount("cherry")).isEqualTo(2.0);
            assertThat(counter.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should increment all from another counter")
        void shouldIncrementAllFromAnotherCounter() {
            com.hitorro.util.Counter<String> other = new com.hitorro.util.Counter<>();
            other.setCount("apple", 5.0);
            other.setCount("banana", 3.0);

            counter.setCount("apple", 2.0);
            counter.incrementAll(other);

            assertThat(counter.getCount("apple")).isEqualTo(7.0);
            assertThat(counter.getCount("banana")).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperations {

        @Test
        @DisplayName("Should check if key exists")
        void shouldCheckIfKeyExists() {
            counter.setCount("apple", 5.0);

            assertThat(counter.containsKey("apple")).isTrue();
            assertThat(counter.containsKey("banana")).isFalse();
        }

        @Test
        @DisplayName("Should distinguish zero count from non-existent key")
        void shouldDistinguishZeroCountFromNonExistentKey() {
            counter.setCount("apple", 0.0);

            assertThat(counter.getCount("apple")).isZero();
            assertThat(counter.containsKey("apple")).isTrue();
            assertThat(counter.containsKey("banana")).isFalse();
        }

        @Test
        @DisplayName("Should return correct key set")
        void shouldReturnCorrectKeySet() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 3.0);
            counter.setCount("cherry", 7.0);

            Set<String> keys = counter.keySet();

            assertThat(keys)
                    .hasSize(3)
                    .containsExactlyInAnyOrder("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("Should calculate total count")
        void shouldCalculateTotalCount() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 3.0);
            counter.setCount("cherry", 7.0);

            assertThat(counter.totalCount()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("Should find key with maximum count")
        void shouldFindKeyWithMaximumCount() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 10.0);
            counter.setCount("cherry", 3.0);

            assertThat(counter.argMax()).isEqualTo("banana");
        }

        @Test
        @DisplayName("Should return null argMax for empty counter")
        void shouldReturnNullArgMaxForEmptyCounter() {
            assertThat(counter.argMax()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle very large counts")
        void shouldHandleVeryLargeCounts() {
            counter.setCount("large", Double.MAX_VALUE / 2);
            counter.incrementCount("large", Double.MAX_VALUE / 2);

            assertThat(counter.getCount("large")).isCloseTo(Double.MAX_VALUE, withinPercentage(1));
        }

        @Test
        @DisplayName("Should handle zero counts")
        void shouldHandleZeroCounts() {
            counter.setCount("zero", 0.0);

            assertThat(counter.getCount("zero")).isZero();
            assertThat(counter.containsKey("zero")).isTrue();
            assertThat(counter.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle negative counts")
        void shouldHandleNegativeCounts() {
            counter.setCount("negative", -5.0);

            assertThat(counter.getCount("negative")).isEqualTo(-5.0);
        }

        @Test
        @DisplayName("Should handle null values in collection")
        void shouldHandleNullValuesInCollection() {
            assertThatCode(() -> 
                counter.incrementAll(Arrays.asList("apple", null, "banana"), 1.0)
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Priority Queue Conversion")
    class PriorityQueueConversion {

        @Test
        @DisplayName("Should convert to priority queue")
        void shouldConvertToPriorityQueue() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 10.0);
            counter.setCount("cherry", 3.0);

            com.hitorro.util.PriorityQueue<String> pq = counter.asPriorityQueue();

            assertThat(pq).isNotNull();
            // Priority queue should contain all elements
            assertThat(pq.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("Should have string representation")
        void shouldHaveStringRepresentation() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 3.0);

            String str = counter.toString();

            assertThat(str).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle toString with max keys")
        void shouldHandleToStringWithMaxKeys() {
            counter.setCount("apple", 5.0);
            counter.setCount("banana", 10.0);
            counter.setCount("cherry", 3.0);

            String str = counter.toString(2);

            assertThat(str).isNotEmpty();
        }
    }
}
