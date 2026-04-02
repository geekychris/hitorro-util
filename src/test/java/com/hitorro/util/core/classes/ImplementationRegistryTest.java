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
package com.hitorro.util.core.classes;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ImplementationRegistry Tests")
class ImplementationRegistryTest {

	private ImplementationRegistry registry;

	@BeforeEach
	void setUp() {
		registry = ImplementationRegistry.createStandalone();
	}

	@AfterEach
	void tearDown() {
		registry.clear();
	}

	@Nested
	@DisplayName("Programmatic registration")
	class ProgrammaticRegistration {

		@Test
		@DisplayName("Should resolve registered symbolic name")
		void shouldResolveRegisteredName() {
			registry.register("html-scrubber", "com.example.HtmlScrubber");

			assertThat(registry.resolve("html-scrubber")).isEqualTo("com.example.HtmlScrubber");
		}

		@Test
		@DisplayName("Should return FQN as-is when not a symbolic name")
		void shouldReturnFqnAsIs() {
			assertThat(registry.resolve("com.example.SomeClass")).isEqualTo("com.example.SomeClass");
		}

		@Test
		@DisplayName("Should return null for null input")
		void shouldReturnNullForNull() {
			assertThat(registry.resolve(null)).isNull();
		}

		@Test
		@DisplayName("Should return empty for empty input")
		void shouldReturnEmptyForEmpty() {
			assertThat(registry.resolve("")).isEqualTo("");
		}

		@Test
		@DisplayName("Should allow override of existing mapping")
		void shouldAllowOverride() {
			registry.register("scrubber", "com.example.V1");
			registry.register("scrubber", "com.example.V2");

			assertThat(registry.resolve("scrubber")).isEqualTo("com.example.V2");
		}

		@Test
		@DisplayName("Should detect symbolic names")
		void shouldDetectSymbolicNames() {
			registry.register("my-impl", "com.example.Impl");

			assertThat(registry.isSymbolic("my-impl")).isTrue();
			assertThat(registry.isSymbolic("com.example.Impl")).isFalse();
		}
	}

	@Nested
	@DisplayName("Loading from JSON")
	class LoadFromJson {

		@Test
		@DisplayName("Should load mappings from JSON object")
		void shouldLoadFromJson() {
			ObjectNode json = JsonNodeFactory.instance.objectNode();
			json.put("scrubber", "com.example.Scrubber");
			json.put("tokenizer", "com.example.Tokenizer");

			registry.loadFromJson(json);

			assertThat(registry.resolve("scrubber")).isEqualTo("com.example.Scrubber");
			assertThat(registry.resolve("tokenizer")).isEqualTo("com.example.Tokenizer");
		}

		@Test
		@DisplayName("Should not overwrite programmatic registrations with config file entries")
		void programmaticOverridesShouldWin() {
			registry.register("scrubber", "com.example.MyCustom");

			ObjectNode json = JsonNodeFactory.instance.objectNode();
			json.put("scrubber", "com.example.Default");

			registry.loadFromJson(json);

			assertThat(registry.resolve("scrubber")).isEqualTo("com.example.MyCustom");
		}

		@Test
		@DisplayName("Should handle null JSON gracefully")
		void shouldHandleNullJson() {
			registry.loadFromJson(null);
			assertThat(registry.size()).isGreaterThanOrEqualTo(0);
		}

		@Test
		@DisplayName("Should skip non-textual values")
		void shouldSkipNonTextual() {
			ObjectNode json = JsonNodeFactory.instance.objectNode();
			json.put("good", "com.example.Good");
			json.put("bad", 42);

			registry.loadFromJson(json);

			assertThat(registry.resolve("good")).isEqualTo("com.example.Good");
			assertThat(registry.isSymbolic("bad")).isFalse();
		}
	}

	@Nested
	@DisplayName("Passthrough for FQN class names")
	class Passthrough {

		@Test
		@DisplayName("FQN class names pass through unchanged")
		void fqnPassesThrough() {
			registry.register("scrubber", "com.example.Scrubber");

			// FQN that's not a symbolic name — passes through
			assertThat(registry.resolve("com.hitorro.jsontypesystem.dynamic.DynamicMapper"))
					.isEqualTo("com.hitorro.jsontypesystem.dynamic.DynamicMapper");
		}

		@Test
		@DisplayName("Static field references pass through unchanged")
		void staticFieldRefPassesThrough() {
			assertThat(registry.resolve("com.example.Foo#INSTANCE"))
					.isEqualTo("com.example.Foo#INSTANCE");
		}
	}
}
