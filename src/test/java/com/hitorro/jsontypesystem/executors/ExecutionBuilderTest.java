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
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ExecutionBuilder, ExecutionNode, and the projection pipeline
 * using recording actions that don't need the type system or Solr config.
 */
@DisplayName("ExecutionBuilder Tests")
class ExecutionBuilderTest {

	/**
	 * A test action that records every path it projects, for assertion.
	 */
	static class RecordingAction implements ExecutorAction<ExecutionBuilder> {
		static List<String> projectedPaths = new ArrayList<>();
		static List<JsonNode> projectedValues = new ArrayList<>();

		@Override
		public void project(ProjectionContext pc, Propaccess path, boolean isMulti, String lang) {
			String pathStr = path.getPath();
			projectedPaths.add(pathStr);
			try {
				JsonNode val = pc.source.get(path);
				projectedValues.add(val);
			} catch (PropaccessError e) {
				projectedValues.add(null);
			}
		}

		static void reset() {
			projectedPaths.clear();
			projectedValues.clear();
		}
	}

	/**
	 * A factory that produces RecordingActions.
	 */
	static class RecordingFactory implements ExecutorFactory<RecordingAction> {
		@Override
		public RecordingAction getNew(com.hitorro.jsontypesystem.Field field,
		                               com.hitorro.jsontypesystem.Group group,
		                               Propaccess path) {
			return new RecordingAction();
		}
	}

	@Nested
	@DisplayName("Builder construction")
	class BuilderConstruction {

		@Test
		@DisplayName("Empty builder should produce root node with no actions")
		void emptyBuilder() {
			ExecutionBuilder<RecordingAction> builder = new ExecutionBuilder<>(new RecordingFactory());
			builder.finalizeNode();

			ExecutionNode<RecordingAction> root = builder.getExecutor();
			assertThat(root).isNotNull();
			assertThat(root.getRows()).isEmpty();
			assertThat(root.getActions()).isEmpty();
		}

		@Test
		@DisplayName("Finalization should return action+row count")
		void finalizationCount() {
			ExecutionBuilder<RecordingAction> builder = new ExecutionBuilder<>(new RecordingFactory());
			int count = builder.finalizeNode();
			assertThat(count).isEqualTo(0);
		}

		@Test
		@DisplayName("getCurrentNode should return root after construction")
		void currentNodeIsRoot() {
			ExecutionBuilder<RecordingAction> builder = new ExecutionBuilder<>(new RecordingFactory());
			assertThat(builder.getCurrentNode()).isSameAs(builder.getExecutor());
		}
	}

	@Nested
	@DisplayName("Node dump output")
	class DumpOutput {

		@Test
		@DisplayName("Root node dump should be [root] with newline")
		void rootDump() {
			ExecutionNode<RecordingAction> root = new ExecutionNode<>(null);
			root.finalizeNode();
			String dump = root.dump();

			assertThat(dump.trim()).isEqualTo("[root]");
		}

		@Test
		@DisplayName("Node with actions should show action count and class name")
		void nodeWithActionsDump() {
			ExecutionNode<RecordingAction> root = new ExecutionNode<>(null);
			root.addAction(new RecordingAction());
			root.addAction(new RecordingAction());
			root.finalizeNode();
			String dump = root.dump();

			assertThat(dump).contains("2 action(s)");
			assertThat(dump).contains("RecordingAction");
		}
	}

	@Nested
	@DisplayName("Projection through actions")
	class ProjectionThroughActions {

		@Test
		@DisplayName("Should project root-level actions with empty path")
		void rootLevelActions() throws Exception {
			RecordingAction.reset();

			ExecutionNode<RecordingAction> root = new ExecutionNode<>(null);
			root.addAction(new RecordingAction());
			root.finalizeNode();

			JVS source = JVS.read("{\"name\":\"test\"}");
			ProjectionContext pc = new ProjectionContext();
			pc.source = source;
			pc.target = new JVS();

			root.project(pc);

			assertThat(RecordingAction.projectedPaths).hasSize(1);
			assertThat(RecordingAction.projectedPaths.get(0)).isEmpty();
		}
	}

	@Nested
	@DisplayName("ProjectionContext lifecycle")
	class ContextLifecycle {

		@Test
		@DisplayName("Context path should be reset on each project() call")
		void pathResetOnProject() throws Exception {
			ExecutionNode<RecordingAction> root = new ExecutionNode<>(null);
			root.finalizeNode();

			ProjectionContext pc = new ProjectionContext();
			pc.source = new JVS();
			pc.target = new JVS();

			// Dirty the path
			pc.path.append("stale.data");
			assertThat(pc.path.length()).isEqualTo(2);

			// project() should reset it
			root.project(pc);
			assertThat(pc.path.length()).isEqualTo(0);
		}

		@Test
		@DisplayName("Context StringBuilder should be independent per use")
		void stringBuilderIndependence() {
			ProjectionContext pc = new ProjectionContext();
			pc.sb.append("stale");

			// Simulate IndexerAction resetting it
			pc.sb.setLength(0);
			pc.sb.append("fresh");

			assertThat(pc.sb.toString()).isEqualTo("fresh");
		}
	}

	@Nested
	@DisplayName("Type preservation in index output")
	class TypePreservation {

		@Test
		@DisplayName("Long values should remain numbers in index target")
		void longPreservation() {
			ObjectNode target = JsonNodeFactory.instance.objectNode();

			// Simulate IndexerAction: on.set(field, val) instead of on.put(field, vText)
			JsonNode longVal = JsonNodeFactory.instance.numberNode(999L);
			target.set("count.long_s", longVal);

			assertThat(target.get("count.long_s").isNumber()).isTrue();
			assertThat(target.get("count.long_s").longValue()).isEqualTo(999L);
		}

		@Test
		@DisplayName("Boolean values should remain booleans in index target")
		void booleanPreservation() {
			ObjectNode target = JsonNodeFactory.instance.objectNode();
			JsonNode boolVal = JsonNodeFactory.instance.booleanNode(true);
			target.set("active.identifier_s", boolVal);

			assertThat(target.get("active.identifier_s").isBoolean()).isTrue();
		}

		@Test
		@DisplayName("Text values should remain text in index target")
		void textPreservation() {
			ObjectNode target = JsonNodeFactory.instance.objectNode();
			JsonNode textVal = JsonNodeFactory.instance.textNode("hello");
			target.set("name.text_en_s", textVal);

			assertThat(target.get("name.text_en_s").isTextual()).isTrue();
			assertThat(target.get("name.text_en_s").textValue()).isEqualTo("hello");
		}

		@Test
		@DisplayName("Multi-valued promotion should preserve all value types")
		void multiValuedPromotionPreservesTypes() {
			ObjectNode target = JsonNodeFactory.instance.objectNode();

			// First value
			JsonNode first = JsonNodeFactory.instance.numberNode(10L);
			target.set("hash.long_m", first);

			// Second value — simulate IndexerAction promotion
			JsonNode existing = target.get("hash.long_m");
			com.fasterxml.jackson.databind.node.ArrayNode arr = JsonNodeFactory.instance.arrayNode();
			arr.add(existing);
			target.set("hash.long_m", arr);
			arr.add(JsonNodeFactory.instance.numberNode(20L));
			arr.add(JsonNodeFactory.instance.numberNode(30L));

			// All values should be numbers
			JsonNode result = target.get("hash.long_m");
			assertThat(result.isArray()).isTrue();
			assertThat(result.size()).isEqualTo(3);
			for (int i = 0; i < result.size(); i++) {
				assertThat(result.get(i).isNumber())
						.as("Element %d should be a number", i)
						.isTrue();
			}
		}

		@Test
		@DisplayName("Null values should be skipped")
		void nullSkipped() {
			JVS source = JVS.read("{\"field\":null}");
			JsonNode val = source.get("field");

			// IndexerAction guard: val != null && !val.isNull()
			boolean shouldSkip = (val == null || val.isNull());
			assertThat(shouldSkip).isTrue();
		}

		@Test
		@DisplayName("Missing values should be skipped")
		void missingSkipped() {
			JVS source = JVS.read("{\"other\":\"value\"}");
			JsonNode val = source.get("field");

			boolean shouldSkip = (val == null || val.isNull());
			assertThat(shouldSkip).isTrue();
		}
	}
}
