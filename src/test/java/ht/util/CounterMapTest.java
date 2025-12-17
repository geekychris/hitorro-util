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
package ht.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CounterMap Tests")
class CounterMapTest {

    private com.hitorro.util.CounterMap<String, String> counterMap;

    @BeforeEach
    void setUp() {
        counterMap = new com.hitorro.util.CounterMap<>();
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should initialize with zero size")
        void shouldInitializeWithZeroSize() {
            assertThat(counterMap.size()).isZero();
            assertThat(counterMap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should return zero for non-existent key-value pairs")
        void shouldReturnZeroForNonExistentPairs() {
            assertThat(counterMap.getCount("key", "value")).isZero();
        }

        @Test
        @DisplayName("Should set and retrieve counts")
        void shouldSetAndRetrieveCounts() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("animal", "dog", 3.0);
            counterMap.setCount("fruit", "apple", 2.0);

            assertThat(counterMap.getCount("animal", "cat")).isEqualTo(5.0);
            assertThat(counterMap.getCount("animal", "dog")).isEqualTo(3.0);
            assertThat(counterMap.getCount("fruit", "apple")).isEqualTo(2.0);
            assertThat(counterMap.size()).isEqualTo(2); // Two keys: "animal" and "fruit"
        }

        @Test
        @DisplayName("Should overwrite existing counts")
        void shouldOverwriteExistingCounts() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("animal", "cat", 10.0);

            assertThat(counterMap.getCount("animal", "cat")).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Should clear all entries")
        void shouldClearAllEntries() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("fruit", "apple", 2.0);

            counterMap.clear();

            assertThat(counterMap.isEmpty()).isTrue();
            assertThat(counterMap.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Increment Operations")
    class IncrementOperations {

        @Test
        @DisplayName("Should increment count for new key-value pair")
        void shouldIncrementCountForNewPair() {
            counterMap.incrementCount("animal", "cat", 5.0);

            assertThat(counterMap.getCount("animal", "cat")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should increment count for existing key-value pair")
        void shouldIncrementCountForExistingPair() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.incrementCount("animal", "cat", 3.0);

            assertThat(counterMap.getCount("animal", "cat")).isEqualTo(8.0);
        }

        @Test
        @DisplayName("Should increment with negative values")
        void shouldIncrementWithNegativeValues() {
            counterMap.setCount("animal", "cat", 10.0);
            counterMap.incrementCount("animal", "cat", -3.0);

            assertThat(counterMap.getCount("animal", "cat")).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should handle multiple values for same key")
        void shouldHandleMultipleValuesForSameKey() {
            counterMap.incrementCount("animal", "cat", 5.0);
            counterMap.incrementCount("animal", "dog", 3.0);
            counterMap.incrementCount("animal", "bird", 2.0);

            assertThat(counterMap.getCount("animal", "cat")).isEqualTo(5.0);
            assertThat(counterMap.getCount("animal", "dog")).isEqualTo(3.0);
            assertThat(counterMap.getCount("animal", "bird")).isEqualTo(2.0);
            assertThat(counterMap.size()).isEqualTo(1); // Only one key: "animal"
        }
    }

    @Nested
    @DisplayName("Sub-Counter Operations")
    class SubCounterOperations {

        @Test
        @DisplayName("Should get counter for key")
        void shouldGetCounterForKey() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("animal", "dog", 3.0);

            com.hitorro.util.Counter<String> animalCounter = counterMap.getCounter("animal");

            assertThat(animalCounter).isNotNull();
            assertThat(animalCounter.getCount("cat")).isEqualTo(5.0);
            assertThat(animalCounter.getCount("dog")).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should create counter for non-existent key")
        void shouldCreateCounterForNonExistentKey() {
            com.hitorro.util.Counter<String> counter = counterMap.getCounter("newKey");

            assertThat(counter).isNotNull();
            assertThat(counter.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should return same counter instance for repeated calls")
        void shouldReturnSameCounterInstanceForRepeatedCalls() {
            com.hitorro.util.Counter<String> counter1 = counterMap.getCounter("animal");
            com.hitorro.util.Counter<String> counter2 = counterMap.getCounter("animal");

            assertThat(counter1).isSameAs(counter2);
        }
    }

    @Nested
    @DisplayName("Aggregate Operations")
    class AggregateOperations {

        @Test
        @DisplayName("Should calculate total count")
        void shouldCalculateTotalCount() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("animal", "dog", 3.0);
            counterMap.setCount("fruit", "apple", 2.0);
            counterMap.setCount("fruit", "banana", 4.0);

            assertThat(counterMap.totalCount()).isEqualTo(14.0);
        }

        @Test
        @DisplayName("Should calculate total size")
        void shouldCalculateTotalSize() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("animal", "dog", 3.0);
            counterMap.setCount("fruit", "apple", 2.0);

            assertThat(counterMap.totalSize()).isEqualTo(3); // Three key-value pairs
        }

        @Test
        @DisplayName("Should return correct key set")
        void shouldReturnCorrectKeySet() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("fruit", "apple", 2.0);
            counterMap.setCount("color", "red", 1.0);

            Set<String> keys = counterMap.keySet();

            assertThat(keys)
                    .hasSize(3)
                    .containsExactlyInAnyOrder("animal", "fruit", "color");
        }

        @Test
        @DisplayName("Should return zero total for empty counter map")
        void shouldReturnZeroTotalForEmptyCounterMap() {
            assertThat(counterMap.totalCount()).isZero();
            assertThat(counterMap.totalSize()).isZero();
        }
    }

    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCases {

        @Test
        @DisplayName("Should track word-POS tag pairs (bigrams)")
        void shouldTrackWordPOSTagPairs() {
            // Example: tracking part-of-speech tags for words
            counterMap.incrementCount("run", "VERB", 10.0);
            counterMap.incrementCount("run", "NOUN", 5.0);
            counterMap.incrementCount("fast", "ADJ", 8.0);
            counterMap.incrementCount("fast", "ADV", 12.0);

            assertThat(counterMap.getCount("run", "VERB")).isEqualTo(10.0);
            assertThat(counterMap.getCount("run", "NOUN")).isEqualTo(5.0);
            assertThat(counterMap.getCount("fast", "ADJ")).isEqualTo(8.0);
            assertThat(counterMap.getCount("fast", "ADV")).isEqualTo(12.0);

            // Get distribution for "run"
            com.hitorro.util.Counter<String> runCounter = counterMap.getCounter("run");
            assertThat(runCounter.totalCount()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("Should track categorical data distribution")
        void shouldTrackCategoricalDataDistribution() {
            // Example: tracking user actions per category
            counterMap.incrementCount("electronics", "view", 100.0);
            counterMap.incrementCount("electronics", "purchase", 10.0);
            counterMap.incrementCount("books", "view", 50.0);
            counterMap.incrementCount("books", "purchase", 15.0);

            assertThat(counterMap.totalCount()).isEqualTo(175.0);
            assertThat(counterMap.getCounter("electronics").totalCount()).isEqualTo(110.0);
            assertThat(counterMap.getCounter("books").totalCount()).isEqualTo(65.0);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle zero counts")
        void shouldHandleZeroCounts() {
            counterMap.setCount("key", "value", 0.0);

            assertThat(counterMap.getCount("key", "value")).isZero();
            assertThat(counterMap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle negative counts")
        void shouldHandleNegativeCounts() {
            counterMap.setCount("key", "value", -5.0);

            assertThat(counterMap.getCount("key", "value")).isEqualTo(-5.0);
        }

        @Test
        @DisplayName("Should handle very large counts")
        void shouldHandleVeryLargeCounts() {
            counterMap.setCount("key", "value", Double.MAX_VALUE / 2);
            counterMap.incrementCount("key", "value", Double.MAX_VALUE / 2);

            assertThat(counterMap.getCount("key", "value"))
                    .isCloseTo(Double.MAX_VALUE, withinPercentage(1));
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("Should have string representation")
        void shouldHaveStringRepresentation() {
            counterMap.setCount("animal", "cat", 5.0);
            counterMap.setCount("fruit", "apple", 2.0);

            String str = counterMap.toString();

            assertThat(str).isNotEmpty();
            assertThat(str).contains("animal");
            assertThat(str).contains("fruit");
        }
    }
}
