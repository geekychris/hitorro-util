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
package com.hitorro.jsontypesystem.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.json.String2JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.hitorro.jsontypesystem.schema.HiTorroSchemaConstants.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("JsonSchema2TypeConverter Tests")
class JsonSchema2TypeConverterTest {

	private final JsonSchema2TypeConverter converter = new JsonSchema2TypeConverter();
	private final String2JsonMapper jsonMapper = new String2JsonMapper();

	@Test
	@DisplayName("Should convert primitive string schema back to native type JSON")
	void shouldConvertPrimitiveStringSchema() {
		String schemaJson = """
				{
					"$schema": "https://json-schema.org/draft/2020-12/schema",
					"title": "string",
					"type": "string",
					"x-hitorro-name": "string",
					"x-hitorro-primitivetype": "string"
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		assertThat(result.get("name").asText()).isEqualTo("string");
		assertThat(result.get("primitivetype").asText()).isEqualTo("string");
		assertThat(result.has("fields")).isFalse();
	}

	@Test
	@DisplayName("Should convert primitive long schema")
	void shouldConvertPrimitiveLongSchema() {
		String schemaJson = """
				{
					"$schema": "https://json-schema.org/draft/2020-12/schema",
					"title": "long",
					"type": "integer",
					"x-hitorro-name": "long",
					"x-hitorro-primitivetype": "long"
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		assertThat(result.get("name").asText()).isEqualTo("long");
		assertThat(result.get("primitivetype").asText()).isEqualTo("long");
	}

	@Test
	@DisplayName("Should convert composite type schema with properties")
	void shouldConvertCompositeTypeSchema() {
		String schemaJson = """
				{
					"$schema": "https://json-schema.org/draft/2020-12/schema",
					"title": "id",
					"type": "object",
					"x-hitorro-name": "id",
					"properties": {
						"domain": {"$ref": "#/$defs/core_string"},
						"did": {"$ref": "#/$defs/core_string"}
					}
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		assertThat(result.get("name").asText()).isEqualTo("id");
		assertThat(result.has("primitivetype")).isFalse();
		assertThat(result.has("fields")).isTrue();

		JsonNode fields = result.get("fields");
		assertThat(fields.isArray()).isTrue();
		assertThat(fields.size()).isEqualTo(2);

		// Check first field
		JsonNode domainField = fields.get(0);
		assertThat(domainField.get("name").asText()).isEqualTo("domain");
		assertThat(domainField.get("type").asText()).isEqualTo("core_string");
	}

	@Test
	@DisplayName("Should convert vector field schema (array type)")
	void shouldConvertVectorFieldSchema() {
		String schemaJson = """
				{
					"title": "test",
					"type": "object",
					"x-hitorro-name": "test",
					"properties": {
						"items": {
							"type": "array",
							"items": {"$ref": "#/$defs/core_long"}
						}
					}
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		JsonNode field = result.get("fields").get(0);
		assertThat(field.get("name").asText()).isEqualTo("items");
		assertThat(field.get("type").asText()).isEqualTo("core_long");
		assertThat(field.get("vector").asBoolean()).isTrue();
	}

	@Test
	@DisplayName("Should convert schema with dynamic field config")
	void shouldConvertDynamicFieldSchema() {
		String schemaJson = """
				{
					"title": "test",
					"type": "object",
					"x-hitorro-name": "test",
					"properties": {
						"id": {
							"$ref": "#/$defs/core_string",
							"x-hitorro-dynamic": {
								"class": "com.hitorro.jsontypesystem.dynamic.MultiValueMergerDM",
								"fields": [".domain", ".did"]
							}
						}
					}
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		JsonNode field = result.get("fields").get(0);
		assertThat(field.has("dynamic")).isTrue();
		assertThat(field.get("dynamic").get("class").asText())
				.isEqualTo("com.hitorro.jsontypesystem.dynamic.MultiValueMergerDM");
	}

	@Test
	@DisplayName("Should convert schema with groups")
	void shouldConvertGroupsSchema() {
		String schemaJson = """
				{
					"title": "test",
					"type": "object",
					"x-hitorro-name": "test",
					"properties": {
						"field": {
							"$ref": "#/$defs/core_string",
							"x-hitorro-groups": [
								{"name": "index", "method": "identifier", "tags": ["basic"]}
							]
						}
					}
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		JsonNode field = result.get("fields").get(0);
		assertThat(field.has("groups")).isTrue();
		assertThat(field.get("groups").isArray()).isTrue();
		assertThat(field.get("groups").get(0).get("name").asText()).isEqualTo("index");
	}

	@Test
	@DisplayName("Should convert schema with i18n marker")
	void shouldConvertI18nSchema() {
		String schemaJson = """
				{
					"title": "test",
					"type": "object",
					"x-hitorro-name": "test",
					"properties": {
						"text": {
							"$ref": "#/$defs/core_string",
							"x-hitorro-i18n": true
						}
					}
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		JsonNode field = result.get("fields").get(0);
		assertThat(field.get("i18n").asBoolean()).isTrue();
	}

	@Test
	@DisplayName("Should convert schema with indexseeker and fetchlang")
	void shouldConvertIndexSeekerSchema() {
		String schemaJson = """
				{
					"title": "mlselem",
					"type": "object",
					"x-hitorro-name": "mlselem",
					"x-hitorro-indexseeker": {"class": "com.hitorro.jsontypesystem.IsoLanguageSeeker"},
					"x-hitorro-fetchlang": true,
					"properties": {
						"lang": {"$ref": "#/$defs/core_string"},
						"text": {"$ref": "#/$defs/core_string", "x-hitorro-i18n": true}
					}
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		assertThat(result.get("name").asText()).isEqualTo("mlselem");
		assertThat(result.has("indexseeker")).isTrue();
		assertThat(result.get("indexseeker").get("class").asText())
				.isEqualTo("com.hitorro.jsontypesystem.IsoLanguageSeeker");
		assertThat(result.get("fetchlang").asBoolean()).isTrue();
	}

	@Test
	@DisplayName("Should convert schema with inheritance (allOf)")
	void shouldConvertInheritanceSchema() {
		String schemaJson = """
				{
					"title": "child",
					"x-hitorro-name": "child",
					"x-hitorro-super": "parent",
					"allOf": [
						{"$ref": "#/$defs/core_parent"},
						{
							"type": "object",
							"properties": {
								"extra": {"$ref": "#/$defs/core_string"}
							}
						}
					]
				}
				""";
		JsonNode schema = jsonMapper.apply(schemaJson);

		ObjectNode result = converter.convert(schema);

		assertThat(result.get("name").asText()).isEqualTo("child");
		assertThat(result.get("super").asText()).isEqualTo("parent");
		assertThat(result.has("fields")).isTrue();
		assertThat(result.get("fields").get(0).get("name").asText()).isEqualTo("extra");
	}
}
