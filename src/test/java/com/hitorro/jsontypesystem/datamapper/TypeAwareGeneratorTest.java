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
package com.hitorro.jsontypesystem.datamapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.json.String2JsonMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for TypeAwareGenerator — generates JVS documents from type definitions.
 * Uses raw JsonNode type definitions to avoid triggering JsonTypeSystem static init.
 */
@DisplayName("TypeAwareGenerator Tests")
class TypeAwareGeneratorTest {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();
	private static DataGenerators generators;

	@BeforeAll
	static void setup() {
		File genDir = new File("config/generators");
		if (!genDir.exists()) genDir = new File("hitorro-util/config/generators");
		generators = new DataGenerators(genDir);
	}

	private static JsonNode typeDef(String json) {
		return jsonMapper.apply(json);
	}

	@Nested
	@DisplayName("Primitive field generation")
	class PrimitiveFields {

		@Test
		@DisplayName("Should generate string value for string field")
		void stringField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "title", "type": "core_string"}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.get("title")).isNotNull();
			assertThat(doc.get("title").isTextual()).isTrue();
		}

		@Test
		@DisplayName("Should generate number for long field")
		void longField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "count", "type": "core_long"}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.get("count")).isNotNull();
			assertThat(doc.get("count").isNumber()).isTrue();
		}

		@Test
		@DisplayName("Should generate boolean for boolean field")
		void booleanField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "active", "type": "core_boolean"}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.get("active")).isNotNull();
			assertThat(doc.get("active").isBoolean()).isTrue();
		}

		@Test
		@DisplayName("Should generate date string for date field")
		void dateField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "created", "type": "core_date"}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.get("created")).isNotNull();
			assertThat(doc.getString("created")).matches("\\d{4}-\\d{2}-\\d{2}T.*");
		}
	}

	@Nested
	@DisplayName("Vector field generation")
	class VectorFields {

		@Test
		@DisplayName("Should generate array for vector field")
		void vectorField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "tags", "type": "core_string", "vector": true}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			JsonNode tags = doc.get("tags");
			assertThat(tags).isNotNull();
			assertThat(tags.isArray()).isTrue();
			assertThat(tags.size()).isGreaterThan(0);
		}
	}

	@Nested
	@DisplayName("Composite type generation")
	class CompositeTypes {

		@Test
		@DisplayName("Should generate all defined fields")
		void allFields() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "name", "type": "core_string"},
						{"name": "age", "type": "core_long"},
						{"name": "active", "type": "core_boolean"}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.get("name")).isNotNull();
			assertThat(doc.get("age")).isNotNull();
			assertThat(doc.get("active")).isNotNull();
		}

		@Test
		@DisplayName("Should skip dynamic fields (they are computed)")
		void skipDynamic() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "source", "type": "core_string"},
						{"name": "computed", "type": "core_string",
						 "dynamic": {"class": "dynamic-mapper", "mapper": {"class": "fp-hash"}, "fields": [".source"]}}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.get("source")).isNotNull();
			assertThat(doc.get("computed")).isNull();
		}

		@Test
		@DisplayName("Should set type name on generated document")
		void setsTypeName() {
			JsonNode def = typeDef("""
					{"name": "mytype", "fields": [
						{"name": "x", "type": "core_string"}
					]}""");

			JVS doc = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc.getString("type")).isNotNull();
		}
	}

	@Nested
	@DisplayName("Multiple generation")
	class MultipleGeneration {

		@Test
		@DisplayName("Each generated document should be unique")
		void uniqueDocs() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "name", "type": "core_string"}
					]}""");

			JVS doc1 = TypeAwareGenerator.generateFromDefinition(def, generators);
			JVS doc2 = TypeAwareGenerator.generateFromDefinition(def, generators);

			assertThat(doc1.getStringRepresentation())
					.isNotEqualTo(doc2.getStringRepresentation());
		}
	}
}
