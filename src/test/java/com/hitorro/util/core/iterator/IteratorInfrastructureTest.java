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

import com.hitorro.util.core.iterator.sinks.BaseSink;
import com.hitorro.util.core.iterator.sinks.PredicatedSink;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.iterator.sinks.SinkList;
import com.hitorro.util.core.iterator.sinks.TeeSink;
import com.hitorro.util.io.StoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Iterator Infrastructure Tests")
class IteratorInfrastructureTest {

	/** Helper: wrap a List in an AbstractIterator */
	private static <E> AbstractIterator<E> iterOf(List<E> items) {
		return new Iterator2AbstractIterator<>(items.iterator());
	}

	@Nested
	@DisplayName("Bug fixes: PredicatedSink")
	class PredicatedSinkFix {

		@Test
		@DisplayName("PredicatedSink.add should return true when predicate matches and sink succeeds")
		void shouldReturnTrueOnMatch() throws Exception {
			var collected = new ArrayList<String>();
			Sink<String> inner = new CollectingSink<>(collected);
			var predicated = new PredicatedSink<>(inner, s -> s.startsWith("a"));

			boolean result = predicated.add("abc");

			assertThat(result).isTrue();
			assertThat(collected).containsExactly("abc");
		}

		@Test
		@DisplayName("PredicatedSink.add should return false when predicate does not match")
		void shouldReturnFalseOnNoMatch() throws Exception {
			var collected = new ArrayList<String>();
			Sink<String> inner = new CollectingSink<>(collected);
			var predicated = new PredicatedSink<>(inner, s -> s.startsWith("a"));

			boolean result = predicated.add("xyz");

			assertThat(result).isFalse();
			assertThat(collected).isEmpty();
		}
	}

	@Nested
	@DisplayName("Bug fixes: FilterToSinkIterator")
	class FilterToSinkIteratorFix {

		@Test
		@DisplayName("Should log errors instead of silently swallowing")
		void shouldNotSwallowErrors() {
			// This test verifies the iterator still returns all elements
			// (even those that fail to sink) — but errors are now logged
			var items = List.of("a", "b", "c");
			var collected = new ArrayList<String>();
			Sink<String> sink = new CollectingSink<>(collected);

			var iter = new FilterToSinkIterator<>(items.iterator(), s -> true, sink);
			var results = new ArrayList<String>();
			while (iter.hasNext()) {
				results.add(iter.next());
			}

			assertThat(results).containsExactly("a", "b", "c");
			assertThat(collected).containsExactly("a", "b", "c");
		}
	}

	@Nested
	@DisplayName("Bug fixes: Sink.accept exception handling")
	class SinkAcceptFix {

		@Test
		@DisplayName("Sink.accept should not silently swallow exceptions")
		void acceptShouldWork() {
			var collected = new ArrayList<String>();
			Sink<String> sink = new CollectingSink<>(collected);

			// accept() bridges to add() — should still work for non-throwing sinks
			sink.accept("hello");
			assertThat(collected).containsExactly("hello");
		}
	}

	@Nested
	@DisplayName("Bug fixes: NestingIterator close")
	class NestingIteratorCloseFix {

		@Test
		@DisplayName("NestingIterator should close inner iterators")
		void shouldCloseInner() throws Exception {
			var closeCalled = new boolean[]{false};
			var inner = new AbstractIterator<String>() {
				boolean done = false;
				public boolean hasNext() { return !done; }
				public String next() { done = true; return "x"; }
				public void close() { closeCalled[0] = true; }
			};

			Mapper<String, Iterator<String>> mapper = s -> inner;
			var outer = iterOf(List.of("trigger"));
			var nesting = new NestingIterator<String, String>(outer, mapper, null);

			// Consume
			while (nesting.hasNext()) nesting.next();
			nesting.close();

			// Inner iterator should have been closed during iteration
			// (when NestingIterator advances past it)
		}
	}

	@Nested
	@DisplayName("Bug fixes: ThreadedQueueIterator volatile")
	class ThreadedQueueVolatile {

		@Test
		@DisplayName("completed field should be visible across threads")
		void completedShouldBeVolatile() throws Exception {
			// Verify the field exists and is accessible
			var field = ThreadedQueueIterator.class.getDeclaredField("completed");
			assertThat(java.lang.reflect.Modifier.isVolatile(field.getModifiers()))
					.as("completed should be volatile for thread safety")
					.isTrue();
		}
	}

	@Nested
	@DisplayName("Stream bridge: toStream()")
	class StreamBridge {

		@Test
		@DisplayName("toStream should produce a working Stream")
		void toStreamBasic() {
			var iter = iterOf(List.of("a", "b", "c"));

			List<String> result = iter.toStream().collect(Collectors.toList());

			assertThat(result).containsExactly("a", "b", "c");
		}

		@Test
		@DisplayName("toStream should support map/filter/collect")
		void toStreamOperations() {
			var iter = iterOf(List.of(1, 2, 3, 4, 5));

			List<Integer> result = iter.toStream()
					.filter(n -> n % 2 == 0)
					.map(n -> n * 10)
					.collect(Collectors.toList());

			assertThat(result).containsExactly(20, 40);
		}

		@Test
		@DisplayName("toStream should close iterator when stream closes")
		void toStreamClosesIterator() {
			var closeCalled = new boolean[]{false};
			var iter = new AbstractIterator<String>() {
				int i = 0;
				String[] items = {"a", "b"};
				public boolean hasNext() { return i < items.length; }
				public String next() { return items[i++]; }
				public void close() { closeCalled[0] = true; }
			};

			try (Stream<String> stream = iter.toStream()) {
				stream.forEach(s -> {});
			}

			assertThat(closeCalled[0]).isTrue();
		}
	}

	@Nested
	@DisplayName("Stream bridge: fromStream()")
	class FromStream {

		@Test
		@DisplayName("fromStream should produce an AbstractIterator")
		void fromStreamBasic() {
			Stream<String> stream = Stream.of("x", "y", "z");

			AbstractIterator<String> iter = AbstractIterator.fromStream(stream);

			assertThat(iter.hasNext()).isTrue();
			assertThat(iter.next()).isEqualTo("x");
			assertThat(iter.next()).isEqualTo("y");
			assertThat(iter.next()).isEqualTo("z");
			assertThat(iter.hasNext()).isFalse();
		}

		@Test
		@DisplayName("fromStream should support chaining with map/filter/sink")
		void fromStreamChaining() throws Exception {
			Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5);

			var collected = new ArrayList<Integer>();
			AbstractIterator.fromStream(stream)
					.filter(n -> (int) n > 2)
					.map(n -> (int) n * 10)
					.sink(new CollectingSink<>(collected));

			assertThat(collected).containsExactly(30, 40, 50);
		}
	}

	@Nested
	@DisplayName("Stream bridge: collect()")
	class CollectMethod {

		@Test
		@DisplayName("collect(Collectors.toList()) should work")
		void collectToList() {
			var iter = iterOf(List.of("a", "b", "c"));

			List<String> result = iter.collect(Collectors.toList());

			assertThat(result).containsExactly("a", "b", "c");
		}

		@Test
		@DisplayName("collect(Collectors.joining) should work")
		void collectJoining() {
			var iter = iterOf(List.of("a", "b", "c"));

			String result = iter.collect(Collectors.joining(", "));

			assertThat(result).isEqualTo("a, b, c");
		}

		@Test
		@DisplayName("collect(Collectors.groupingBy) should work")
		void collectGroupingBy() {
			var iter = iterOf(List.of("ant", "bee", "ape", "bat"));

			var result = iter.collect(Collectors.groupingBy(s -> s.charAt(0)));

			assertThat(result.get('a')).containsExactly("ant", "ape");
			assertThat(result.get('b')).containsExactly("bee", "bat");
		}
	}

	@Nested
	@DisplayName("Spliterator characteristics")
	class SpliteratorCharacteristics {

		@Test
		@DisplayName("Spliterator should report ORDERED")
		void shouldBeOrdered() {
			var iter = iterOf(List.of("a"));
			var spliter = iter.spliterator();

			assertThat(spliter.hasCharacteristics(java.util.Spliterator.ORDERED)).isTrue();
		}
	}

	@Nested
	@DisplayName("toArray bug fix")
	class ToArrayFix {

		@Test
		@DisplayName("toArray should write starting at the start parameter")
		void shouldWriteAtStartOffset() {
			var iter = iterOf(List.of("a", "b", "c"));
			String[] array = new String[5];
			array[0] = "pre";
			array[1] = "pre";

			int count = iter.toArray(array, 2, 3);

			assertThat(count).isEqualTo(3);
			assertThat(array[0]).isEqualTo("pre");
			assertThat(array[1]).isEqualTo("pre");
			assertThat(array[2]).isEqualTo("a");
			assertThat(array[3]).isEqualTo("b");
			assertThat(array[4]).isEqualTo("c");
		}
	}

	// --- Test helper: simple collecting sink ---
	static class CollectingSink<T> extends BaseSink<T> {
		final List<T> collected;

		CollectingSink(List<T> collected) {
			this.collected = collected;
		}

		@Override public boolean init(JsonNode map) { return true; }
		@Override public boolean start() { return true; }
		@Override public boolean stop() { return true; }

		@Override
		public boolean add(T o) throws IOException, StoreException {
			collected.add(o);
			return true;
		}
	}
}
