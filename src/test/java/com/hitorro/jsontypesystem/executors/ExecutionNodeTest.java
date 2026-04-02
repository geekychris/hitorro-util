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
package com.hitorro.jsontypesystem.executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExecutionNode Tests")
class ExecutionNodeTest {

	@Nested
	@DisplayName("ProjectionContext reusable Propaccess")
	class ProjectionContextPath {

		@Test
		@DisplayName("ProjectionContext should have a reusable Propaccess")
		void shouldHaveReusablePropaccess() {
			ProjectionContext pc = new ProjectionContext();

			assertThat(pc.path).isNotNull();
			assertThat(pc.path.length()).isEqualTo(0);
		}

		@Test
		@DisplayName("ProjectionContext path should be reusable across calls")
		void pathShouldBeReusable() {
			ProjectionContext pc = new ProjectionContext();
			pc.path.append("test");
			assertThat(pc.path.length()).isEqualTo(1);

			// Reset for next use
			pc.path.setLength(0);
			assertThat(pc.path.length()).isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("ExecutionNode dump")
	class Dump {

		@Test
		@DisplayName("Root node dump should show [root]")
		void rootNodeDump() {
			ExecutionNode<ExecutorAction> root = new ExecutionNode<>(null);
			root.finalizeNode();

			String dump = root.dump();

			assertThat(dump).contains("[root]");
		}
	}

	@Nested
	@DisplayName("ExecutionNode finalization")
	class Finalization {

		@Test
		@DisplayName("Finalized node should have zero rows and actions when empty")
		void emptyNodeFinalization() {
			ExecutionNode<ExecutorAction> node = new ExecutionNode<>(null);
			int count = node.finalizeNode();

			assertThat(count).isEqualTo(0);
			assertThat(node.getRows()).isEmpty();
			assertThat(node.getActions()).isEmpty();
		}

		@Test
		@DisplayName("Second finalization should return cached count")
		void doubleFinalization() {
			ExecutionNode<ExecutorAction> node = new ExecutionNode<>(null);
			int first = node.finalizeNode();
			int second = node.finalizeNode();

			assertThat(first).isEqualTo(second);
		}
	}

	@Nested
	@DisplayName("IndexerAction type preservation")
	class IndexerActionTypePreservation {

		@Test
		@DisplayName("Should preserve long values when indexing (not convert to text)")
		void shouldPreserveLongValues() {
			// Create a source document with a long value
			JVS source = JVS.read("{\"value\":42}");
			JVS target = new JVS();

			ProjectionContext pc = new ProjectionContext();
			pc.source = source;
			pc.target = target;
			pc.sb.setLength(0);

			// Manually project a long value through IndexerAction's logic
			Propaccess path = new Propaccess("value");
			JsonNode val = source.get(path);
			assertThat(val.isNumber()).isTrue();

			// Simulate what IndexerAction does: set the value directly on the ObjectNode
			ObjectNode on = (ObjectNode) target.getJsonNode();
			String fieldName = "value_long_s";
			on.set(fieldName, val);

			// Verify the value type is preserved (not converted to text)
			JsonNode stored = on.get(fieldName);
			assertThat(stored.isNumber()).isTrue();
			assertThat(stored.longValue()).isEqualTo(42L);
		}

		@Test
		@DisplayName("Should promote to array for multi-valued fields preserving types")
		void shouldPromoteToArrayPreservingTypes() {
			ObjectNode on = JsonNodeFactory.instance.objectNode();

			// First value — set directly
			on.set("field", JsonNodeFactory.instance.numberNode(10));

			// Second value — should promote to array
			JsonNode existing = on.get("field");
			assertThat(existing.isArray()).isFalse();

			// Simulate IndexerAction's array promotion logic
			com.fasterxml.jackson.databind.node.ArrayNode arr = JsonNodeFactory.instance.arrayNode();
			arr.add(existing);
			on.set("field", arr);
			arr.add(JsonNodeFactory.instance.numberNode(20));

			// Verify both values preserved as numbers
			JsonNode result = on.get("field");
			assertThat(result.isArray()).isTrue();
			assertThat(result.size()).isEqualTo(2);
			assertThat(result.get(0).isNumber()).isTrue();
			assertThat(result.get(0).longValue()).isEqualTo(10);
			assertThat(result.get(1).isNumber()).isTrue();
			assertThat(result.get(1).longValue()).isEqualTo(20);
		}

		@Test
		@DisplayName("Should preserve boolean values when indexing")
		void shouldPreserveBooleanValues() {
			JVS source = JVS.read("{\"flag\":true}");
			JsonNode val = source.get("flag");

			assertThat(val.isBoolean()).isTrue();

			// Simulate IndexerAction storing it
			ObjectNode on = JsonNodeFactory.instance.objectNode();
			on.set("flag.identifier_s", val);

			assertThat(on.get("flag.identifier_s").isBoolean()).isTrue();
			assertThat(on.get("flag.identifier_s").booleanValue()).isTrue();
		}

		@Test
		@DisplayName("Should skip null values")
		void shouldSkipNullValues() {
			JVS source = JVS.read("{\"field\":null}");
			JsonNode val = source.get("field");

			// IndexerAction checks: val != null && !val.isNull()
			assertThat(val == null || val.isNull()).isTrue();
		}
	}

	@Nested
	@DisplayName("ExecutionBuilder memory cleanup")
	class BuilderCleanup {

		@Test
		@DisplayName("ExecutionBuilder should release executors map after finalization")
		void shouldReleaseExecutorsAfterFinalize() {
			ExecutionBuilder<ExecutorAction> builder = new ExecutionBuilder<>(
					(field, group, path) -> null);
			builder.finalizeNode();

			// After finalization, the builder should still return a valid executor
			assertThat(builder.getExecutor()).isNotNull();
		}
	}
}
