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
import com.hitorro.jsontypesystem.BaseT;
import com.hitorro.jsontypesystem.Field;
import com.hitorro.jsontypesystem.Group;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.grouppredicates.GroupNameFilter;
import com.hitorro.util.json.String2JsonMapper;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.testframework.TestPlus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the projection pipeline.
 * Builds real Type objects from JSON configs, constructs ExecutionBuilders,
 * and projects JVS documents through them.
 *
 * Requires the type system and solr field config to be accessible via Env.
 */
@DisplayName("Projection Integration Tests")
@Disabled("Requires full runtime config (types + jsonconfigs/solr_fields.json)")
class ProjectionIntegrationTest implements TestPlus {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();

	private static Type buildType(String json) {
		Type t = new Type();
		t.init(jsonMapper.apply(json));
		return t;
	}

	private static ExecutionBuilder<IndexerAction> buildIndexProjection(Type type) {
		Propaccess path = new Propaccess("");
		ExecutionBuilder<IndexerAction> builder = new ExecutionBuilder<>(new IndexerFactory());
		Predicate<BaseT> predicate = (Predicate<BaseT>) GroupNameFilter.indexFilter;
		type.visit(builder, predicate, path);
		builder.finalizeNode();
		return builder;
	}

	private static ExecutionBuilder<EnrichAction> buildEnrichProjection(Type type) {
		Propaccess path = new Propaccess("");
		ExecutionBuilder<EnrichAction> builder = new ExecutionBuilder<>(new EnrichFactory());
		Predicate<BaseT> predicate = (Predicate<BaseT>) GroupNameFilter.enrichFilter;
		type.visit(builder, predicate, path);
		builder.finalizeNode();
		return builder;
	}

	@Nested
	@DisplayName("Index projection")
	class IndexProjection {

		@Test
		@DisplayName("Should project core_id fields to index document")
		void shouldProjectCoreIdFields() throws Exception {
			Type idType = buildType("""
					{
						"name": "test_id_wrapper",
						"fields": [
							{
								"name": "domain", "type": "core_string",
								"groups": [{"name": "index", "method": "identifier"}]
							},
							{
								"name": "did", "type": "core_string",
								"groups": [{"name": "index", "method": "identifier"}]
							}
						]
					}
					""");

			ExecutionBuilder<IndexerAction> builder = buildIndexProjection(idType);
			assertThat(builder.finalizeNode()).isGreaterThan(0);

			// Dump the execution plan
			String dump = builder.getExecutor().dump();
			assertThat(dump).contains("domain");
			assertThat(dump).contains("did");
			assertThat(dump).contains("IndexerAction");

			// Project a document
			JVS source = JVS.read("{\"domain\":\"users\",\"did\":\"user123\"}");
			JVS target = new JVS();

			ProjectionContext pc = new ProjectionContext();
			pc.source = source;
			pc.target = target;

			builder.getExecutor().project(pc);

			// Verify index fields were created
			JsonNode targetNode = target.getJsonNode();
			assertThat(targetNode.size()).isGreaterThan(0);
		}

		@Test
		@DisplayName("Should preserve long values in index projection")
		void shouldPreserveLongValuesInIndex() throws Exception {
			Type type = buildType("""
					{
						"name": "test_long",
						"fields": [
							{
								"name": "count", "type": "core_long",
								"groups": [{"name": "index", "method": "long"}]
							}
						]
					}
					""");

			ExecutionBuilder<IndexerAction> builder = buildIndexProjection(type);
			JVS source = JVS.read("{\"count\":42}");
			JVS target = new JVS();

			ProjectionContext pc = new ProjectionContext();
			pc.source = source;
			pc.target = target;
			builder.getExecutor().project(pc);

			// The projected value should be a number, not a string
			JsonNode targetNode = target.getJsonNode();
			// Find the index field (will be named count.long_s or similar)
			boolean foundNumber = false;
			var iter = targetNode.fields();
			while (iter.hasNext()) {
				var entry = iter.next();
				if (entry.getKey().startsWith("count")) {
					assertThat(entry.getValue().isNumber())
							.as("Index field %s should be a number, was: %s",
									entry.getKey(), entry.getValue())
							.isTrue();
					assertThat(entry.getValue().longValue()).isEqualTo(42L);
					foundNumber = true;
				}
			}
			assertThat(foundNumber).as("Should have found a count index field").isTrue();
		}

		@Test
		@DisplayName("Should handle vector fields creating multi-valued index entries")
		void shouldHandleVectorFields() throws Exception {
			Type type = buildType("""
					{
						"name": "test_vector",
						"fields": [
							{
								"name": "tags", "type": "core_string", "vector": true,
								"groups": [{"name": "index", "method": "text"}]
							}
						]
					}
					""");

			ExecutionBuilder<IndexerAction> builder = buildIndexProjection(type);
			JVS source = JVS.read("{\"tags\":[\"java\",\"kotlin\",\"scala\"]}");
			JVS target = new JVS();

			ProjectionContext pc = new ProjectionContext();
			pc.source = source;
			pc.target = target;
			builder.getExecutor().project(pc);

			// Should have an array with 3 values in the target
			JsonNode targetNode = target.getJsonNode();
			boolean foundArray = false;
			var iter = targetNode.fields();
			while (iter.hasNext()) {
				var entry = iter.next();
				if (entry.getKey().startsWith("tags")) {
					assertThat(entry.getValue().isArray())
							.as("Vector field %s should be an array", entry.getKey())
							.isTrue();
					assertThat(entry.getValue().size()).isEqualTo(3);
					foundArray = true;
				}
			}
			assertThat(foundArray).as("Should have found a tags index field").isTrue();
		}

		@Test
		@DisplayName("Should produce meaningful execution plan dump")
		void shouldDumpExecutionPlan() throws Exception {
			Type type = buildType("""
					{
						"name": "test_dump",
						"fields": [
							{
								"name": "title", "type": "core_string",
								"groups": [{"name": "index", "method": "text"}]
							},
							{
								"name": "count", "type": "core_long",
								"groups": [{"name": "index", "method": "long"}]
							}
						]
					}
					""");

			ExecutionBuilder<IndexerAction> builder = buildIndexProjection(type);
			String dump = builder.getExecutor().dump();

			// Should show the field tree with actions
			assertThat(dump).contains("[root]");
			assertThat(dump).contains("title");
			assertThat(dump).contains("count");
			assertThat(dump).contains("IndexerAction");
			assertThat(dump).contains("action(s)");
		}
	}

	@Nested
	@DisplayName("Enrich projection")
	class EnrichProjection {

		@Test
		@DisplayName("Should build enrich projection for type with enrich groups")
		void shouldBuildEnrichProjection() throws Exception {
			Type type = buildType("""
					{
						"name": "test_enrich",
						"fields": [
							{
								"name": "text", "type": "core_string",
								"groups": [{"name": "enrich", "method": "text"}]
							}
						]
					}
					""");

			ExecutionBuilder<EnrichAction> builder = buildEnrichProjection(type);
			assertThat(builder.finalizeNode()).isGreaterThan(0);

			String dump = builder.getExecutor().dump();
			assertThat(dump).contains("text");
			assertThat(dump).contains("EnrichAction");
		}

		@Test
		@DisplayName("Enrich projection should touch source fields")
		void shouldTouchSourceFields() throws Exception {
			Type type = buildType("""
					{
						"name": "test_enrich_touch",
						"fields": [
							{
								"name": "value", "type": "core_string",
								"groups": [{"name": "enrich", "method": "text"}]
							}
						]
					}
					""");

			ExecutionBuilder<EnrichAction> builder = buildEnrichProjection(type);
			JVS source = JVS.read("{\"value\":\"hello world\"}");

			ProjectionContext pc = new ProjectionContext();
			pc.source = source;
			pc.target = new JVS();
			builder.getExecutor().project(pc);

			// EnrichAction reads the value — source should still have it
			assertThat(source.getString("value")).isEqualTo("hello world");
		}
	}

	@Nested
	@DisplayName("Projection with no matching groups")
	class EmptyProjection {

		@Test
		@DisplayName("Should produce empty plan when no groups match filter")
		void shouldProduceEmptyPlan() {
			Type type = buildType("""
					{
						"name": "test_no_groups",
						"fields": [
							{"name": "plain", "type": "core_string"}
						]
					}
					""");

			// Build index projection — but the field has no index group
			ExecutionBuilder<IndexerAction> builder = buildIndexProjection(type);
			int count = builder.finalizeNode();

			assertThat(count).isEqualTo(0);
		}

		@Test
		@DisplayName("Should handle type with enrich group but index filter")
		void shouldHandleMismatchedFilter() {
			Type type = buildType("""
					{
						"name": "test_mismatch",
						"fields": [
							{
								"name": "value", "type": "core_string",
								"groups": [{"name": "enrich", "method": "text"}]
							}
						]
					}
					""");

			// Build INDEX projection for type with only ENRICH groups
			ExecutionBuilder<IndexerAction> builder = buildIndexProjection(type);
			int count = builder.finalizeNode();

			assertThat(count).isEqualTo(0);
		}
	}
}
