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
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FilteringIterator Tests")
class FilteringIteratorTest {

    @Nested
    @DisplayName("Basic Filtering")
    class BasicFiltering {

        @Test
        @DisplayName("Should filter even numbers")
        void shouldFilterEvenNumbers() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isEven);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(2, 4, 6, 8, 10);
        }

        @Test
        @DisplayName("Should filter odd numbers")
        void shouldFilterOddNumbers() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            Predicate<Integer> isOdd = i -> i % 2 != 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isOdd);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(1, 3, 5, 7, 9);
        }

        @Test
        @DisplayName("Should filter strings by length")
        void shouldFilterStringsByLength() {
            List<String> input = Arrays.asList("a", "ab", "abc", "abcd", "abcde");
            Predicate<String> longerThanTwo = s -> s.length() > 2;
            
            com.hitorro.util.core.iterator.FilteringIterator<String> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), longerThanTwo);
            
            List<String> result = collectToList(iter);
            
            assertThat(result).containsExactly("abc", "abcd", "abcde");
        }

        @Test
        @DisplayName("Should filter strings by prefix")
        void shouldFilterStringsByPrefix() {
            List<String> input = Arrays.asList("apple", "banana", "apricot", "cherry", "avocado");
            Predicate<String> startsWithA = s -> s.startsWith("a");
            
            com.hitorro.util.core.iterator.FilteringIterator<String> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), startsWithA);
            
            List<String> result = collectToList(iter);
            
            assertThat(result).containsExactly("apple", "apricot", "avocado");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty iterator")
        void shouldHandleEmptyIterator() {
            List<Integer> input = Arrays.asList();
            Predicate<Integer> alwaysTrue = i -> true;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), alwaysTrue);
            
            assertThat(iter.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle predicate that matches nothing")
        void shouldHandlePredicateThatMatchesNothing() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Predicate<Integer> alwaysFalse = i -> false;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), alwaysFalse);
            
            assertThat(iter.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle predicate that matches everything")
        void shouldHandlePredicateThatMatchesEverything() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Predicate<Integer> alwaysTrue = i -> true;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), alwaysTrue);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle single matching element")
        void shouldHandleSingleMatchingElement() {
            List<Integer> input = Arrays.asList(1, 2, 3);
            Predicate<Integer> equalsTwo = i -> i == 2;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), equalsTwo);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(2);
        }

        @Test
        @DisplayName("Should handle all elements matching")
        void shouldHandleAllElementsMatching() {
            List<Integer> input = Arrays.asList(2, 4, 6, 8);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isEven);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(2, 4, 6, 8);
        }
    }

    @Nested
    @DisplayName("Complex Predicates")
    class ComplexPredicates {

        @Test
        @DisplayName("Should use compound predicate with AND")
        void shouldUseCompoundPredicateWithAnd() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            Predicate<Integer> greaterThanFive = i -> i > 5;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(
                    input.iterator(), 
                    isEven.and(greaterThanFive)
            );
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(6, 8, 10);
        }

        @Test
        @DisplayName("Should use compound predicate with OR")
        void shouldUseCompoundPredicateWithOr() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Predicate<Integer> lessThanTwo = i -> i < 2;
            Predicate<Integer> greaterThanFour = i -> i > 4;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(
                    input.iterator(), 
                    lessThanTwo.or(greaterThanFour)
            );
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(1, 5);
        }

        @Test
        @DisplayName("Should use negated predicate")
        void shouldUseNegatedPredicate() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(
                    input.iterator(), 
                    isEven.negate()
            );
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should filter objects by multiple criteria")
        void shouldFilterObjectsByMultipleCriteria() {
            List<Person> input = Arrays.asList(
                    new Person("Alice", 30),
                    new Person("Bob", 25),
                    new Person("Charlie", 35),
                    new Person("David", 28),
                    new Person("Eve", 32)
            );
            Predicate<Person> ageFilter = p -> p.age >= 30;
            
            com.hitorro.util.core.iterator.FilteringIterator<com.hitorro.util.core.iterator.FilteringIteratorTest.Person> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), ageFilter);
            
            List<Person> result = collectToList(iter);
            
            assertThat(result)
                    .hasSize(3)
                    .extracting(p -> p.name)
                    .containsExactly("Alice", "Charlie", "Eve");
        }
    }

    @Nested
    @DisplayName("Iterator Protocol")
    class IteratorProtocol {

        @Test
        @DisplayName("Should allow multiple hasNext calls")
        void shouldAllowMultipleHasNextCalls() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isEven);
            
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.hasNext()).isTrue();
            
            Integer first = iter.next();
            assertThat(first).isEqualTo(2);
        }

        @Test
        @DisplayName("Should support standard iteration pattern")
        void shouldSupportStandardIterationPattern() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
            Predicate<Integer> isOdd = i -> i % 2 != 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isOdd);
            
            List<Integer> result = new ArrayList<>();
            while (iter.hasNext()) {
                result.add(iter.next());
            }
            
            assertThat(result).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should return false after exhaustion")
        void shouldReturnFalseAfterExhaustion() {
            List<Integer> input = Arrays.asList(2, 4);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isEven);
            
            assertThat(iter.hasNext()).isTrue();
            iter.next();
            assertThat(iter.hasNext()).isTrue();
            iter.next();
            assertThat(iter.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle interleaved hasNext and next calls")
        void shouldHandleInterleavedHasNextAndNextCalls() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6);
            Predicate<Integer> isEven = i -> i % 2 == 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isEven);
            
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.next()).isEqualTo(2);
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.next()).isEqualTo(4);
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.next()).isEqualTo(6);
            assertThat(iter.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("Chaining Filters")
    class ChainingFilters {

        @Test
        @DisplayName("Should chain multiple filters")
        void shouldChainMultipleFilters() {
            List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            
            // First filter: get even numbers
            com.hitorro.util.core.iterator.FilteringIterator<Integer> evenIter = new com.hitorro.util.core.iterator.FilteringIterator<>(
                    input.iterator(), 
                    i -> i % 2 == 0
            );
            
            // Second filter: get numbers > 5
            com.hitorro.util.core.iterator.FilteringIterator<Integer> filteredIter = new com.hitorro.util.core.iterator.FilteringIterator<>(
                    evenIter, 
                    i -> i > 5
            );
            
            List<Integer> result = collectToList(filteredIter);
            
            assertThat(result).containsExactly(6, 8, 10);
        }
    }

    @Nested
    @DisplayName("Real World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Should filter valid email addresses")
        void shouldFilterValidEmailAddresses() {
            List<String> input = Arrays.asList(
                    "test@example.com",
                    "invalid-email",
                    "user@domain.org",
                    "bad@",
                    "good@test.net"
            );
            Predicate<String> isValidEmail = s -> s.contains("@") && s.contains(".");
            
            com.hitorro.util.core.iterator.FilteringIterator<String> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isValidEmail);
            
            List<String> result = collectToList(iter);
            
            assertThat(result).containsExactly(
                    "test@example.com",
                    "user@domain.org",
                    "good@test.net"
            );
        }

        @Test
        @DisplayName("Should filter positive numbers from mixed list")
        void shouldFilterPositiveNumbersFromMixedList() {
            List<Integer> input = Arrays.asList(-5, -2, 0, 3, -1, 7, 10, -8, 15);
            Predicate<Integer> isPositive = i -> i > 0;
            
            com.hitorro.util.core.iterator.FilteringIterator<Integer> iter = new com.hitorro.util.core.iterator.FilteringIterator<>(input.iterator(), isPositive);
            
            List<Integer> result = collectToList(iter);
            
            assertThat(result).containsExactly(3, 7, 10, 15);
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
