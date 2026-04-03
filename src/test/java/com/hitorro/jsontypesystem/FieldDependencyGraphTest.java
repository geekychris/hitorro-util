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
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.json.String2JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FieldDependencyGraph Tests")
class FieldDependencyGraphTest {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();

	private static JsonNode typeDef(String json) {
		return jsonMapper.apply(json);
	}

	@Nested
	@DisplayName("Graph construction")
	class Construction {

		@Test
		@DisplayName("Simple linear chain: text -> clean -> hash")
		void linearChain() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "text", "type": "core_string"},
						{"name": "clean", "type": "core_string",
						 "dynamic": {"class": "dynamic-mapper", "fields": [".text"]}},
						{"name": "hash", "type": "core_long",
						 "dynamic": {"class": "dynamic-mapper", "fields": [".clean"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			assertThat(graph.getDependencies("clean")).containsExactly("text");
			assertThat(graph.getDependencies("hash")).containsExactly("clean");
			assertThat(graph.getDependencies("text")).isEmpty();
		}

		@Test
		@DisplayName("Multiple inputs: field depends on two sources")
		void multipleInputs() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "first", "type": "core_string"},
						{"name": "last", "type": "core_string"},
						{"name": "full", "type": "core_string",
						 "dynamic": {"class": "dynamic-mapper", "fields": [".first", ".last"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			assertThat(graph.getDependencies("full")).containsExactlyInAnyOrder("first", "last");
		}

		@Test
		@DisplayName("No dynamic fields produces empty graph")
		void noDynamicFields() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_long"}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			assertThat(graph.getDependencies("a")).isEmpty();
			assertThat(graph.getDependencies("b")).isEmpty();
		}
	}

	@Nested
	@DisplayName("Topological sort")
	class TopologicalSort {

		@Test
		@DisplayName("Linear chain should sort in dependency order")
		void linearSort() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "text", "type": "core_string"},
						{"name": "clean", "type": "core_string",
						 "dynamic": {"class": "dynamic-mapper", "fields": [".text"]}},
						{"name": "hash", "type": "core_long",
						 "dynamic": {"class": "dynamic-mapper", "fields": [".clean"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);
			List<String> order = graph.topologicalSort();

			assertThat(order.indexOf("text")).isLessThan(order.indexOf("clean"));
			assertThat(order.indexOf("clean")).isLessThan(order.indexOf("hash"));
		}

		@Test
		@DisplayName("No dependencies should still return all fields")
		void noDepsSort() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_string"}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);
			List<String> order = graph.topologicalSort();

			assertThat(order).containsExactlyInAnyOrder("a", "b");
		}
	}

	@Nested
	@DisplayName("Impact analysis")
	class ImpactAnalysis {

		@Test
		@DisplayName("Should find all downstream dependents")
		void downstreamDependents() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "text", "type": "core_string"},
						{"name": "clean", "type": "core_string",
						 "dynamic": {"class": "dm", "fields": [".text"]}},
						{"name": "hash", "type": "core_long",
						 "dynamic": {"class": "dm", "fields": [".clean"]}},
						{"name": "ner", "type": "core_string",
						 "dynamic": {"class": "dm", "fields": [".clean"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			// "text" affects clean, which affects hash and ner
			Set<String> impacted = graph.getImpacted("text");
			assertThat(impacted).containsExactlyInAnyOrder("clean", "hash", "ner");
		}

		@Test
		@DisplayName("Leaf field should have no downstream impact")
		void leafHasNoImpact() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "text", "type": "core_string"},
						{"name": "hash", "type": "core_long",
						 "dynamic": {"class": "dm", "fields": [".text"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			assertThat(graph.getImpacted("hash")).isEmpty();
		}
	}

	@Nested
	@DisplayName("Cycle detection")
	class CycleDetection {

		@Test
		@DisplayName("Acyclic graph should report no cycles")
		void noCycle() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_string",
						 "dynamic": {"class": "dm", "fields": [".a"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			assertThat(graph.hasCycle()).isFalse();
		}
	}

	@Nested
	@DisplayName("Visualization")
	class Visualization {

		@Test
		@DisplayName("Should produce readable text dump")
		void textDump() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "text", "type": "core_string"},
						{"name": "clean", "type": "core_string",
						 "dynamic": {"class": "dm", "fields": [".text"]}},
						{"name": "hash", "type": "core_long",
						 "dynamic": {"class": "dm", "fields": [".clean"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			String dump = graph.dump();
			assertThat(dump).contains("text");
			assertThat(dump).contains("clean");
			assertThat(dump).contains("hash");
			assertThat(dump).contains("→");
		}

		@Test
		@DisplayName("Should produce Mermaid diagram")
		void mermaidDiagram() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "text", "type": "core_string"},
						{"name": "clean", "type": "core_string",
						 "dynamic": {"class": "dm", "fields": [".text"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			String mermaid = graph.toMermaid();
			assertThat(mermaid).startsWith("graph TD");
			assertThat(mermaid).contains("text");
			assertThat(mermaid).contains("clean");
			assertThat(mermaid).contains("-->");
		}
	}

	@Nested
	@DisplayName("Real-world: core_mlselem structure")
	class RealWorld {

		@Test
		@DisplayName("Should model the mlselem processing pipeline")
		void mlselemPipeline() {
			// Simplified version of core_mlselem dependencies
			JsonNode def = typeDef("""
					{"name": "mlselem", "fields": [
						{"name": "lang", "type": "core_string"},
						{"name": "text", "type": "core_string"},
						{"name": "clean", "type": "core_string",
						 "dynamic": {"class": "dm", "fields": [".text"]}},
						{"name": "pos", "type": "core_string", "vector": true,
						 "dynamic": {"class": "dm", "fields": [".lang", ".clean"]}},
						{"name": "segmented_span", "type": "core_string", "vector": true,
						 "dynamic": {"class": "dm", "fields": [".lang", ".clean"]}},
						{"name": "segmented", "type": "core_string", "vector": true,
						 "dynamic": {"class": "dm", "fields": [".segmented_span", ".clean"]}},
						{"name": "segmented_ner", "type": "core_string", "vector": true,
						 "dynamic": {"class": "dm", "fields": [".lang", ".segmented"]}}
					]}""");

			FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(def);

			// Verify the chain
			assertThat(graph.getDependencies("clean")).containsExactly("text");
			assertThat(graph.getDependencies("segmented")).containsExactlyInAnyOrder("segmented_span", "clean");
			assertThat(graph.getDependencies("segmented_ner")).containsExactlyInAnyOrder("lang", "segmented");

			// Topological: text must come before clean, clean before segmented, etc.
			List<String> order = graph.topologicalSort();
			assertThat(order.indexOf("text")).isLessThan(order.indexOf("clean"));
			assertThat(order.indexOf("clean")).isLessThan(order.indexOf("segmented"));
			assertThat(order.indexOf("segmented")).isLessThan(order.indexOf("segmented_ner"));

			// Impact: changing "text" should ripple through the whole chain
			Set<String> textImpact = graph.getImpacted("text");
			assertThat(textImpact).contains("clean", "pos", "segmented_span", "segmented", "segmented_ner");

			// No cycles
			assertThat(graph.hasCycle()).isFalse();
		}
	}
}
