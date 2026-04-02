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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static com.hitorro.jsontypesystem.schema.HiTorroSchemaConstants.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("SchemaFileLoader — loading types from .schema.json files")
class SchemaFileLoaderTest {

	private final SchemaFileLoader loader = new SchemaFileLoader();

	private File schemasDir() {
		File dir = new File("config/schemas");
		if (!dir.exists()) {
			dir = new File("hitorro-util/config/schemas");
		}
		return dir;
	}

	// ------------------------------------------------------------------
	// Loading individual schema files
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Load single schema file")
	class LoadSingle {

		@Test
		@DisplayName("Should load primitive type (core_string) from schema file")
		void shouldLoadPrimitiveFromFile() throws Exception {
			File f = new File(schemasDir(), "core_string.schema.json");
			assertThat(f).exists();

			ObjectNode typeJson = loader.loadOne(f);

			assertThat(typeJson.get("name").asText()).isEqualTo("string");
			assertThat(typeJson.get("primitivetype").asText()).isEqualTo("string");
			assertThat(typeJson.has("fields")).isFalse();
		}

		@Test
		@DisplayName("Should load composite type (core_id) from schema file")
		void shouldLoadCompositeFromFile() throws Exception {
			File f = new File(schemasDir(), "core_id.schema.json");
			assertThat(f).exists();

			ObjectNode typeJson = loader.loadOne(f);

			assertThat(typeJson.get("name").asText()).isEqualTo("id");
			assertThat(typeJson.has("fields")).isTrue();

			JsonNode fields = typeJson.get("fields");
			assertThat(fields.size()).isEqualTo(4); // domain, did, id, id_hash

			// Verify field types resolve from file-based $ref
			assertThat(fields.get(0).get("name").asText()).isEqualTo("domain");
			assertThat(fields.get(0).get("type").asText()).isEqualTo("core_string");

			// Verify vector field
			JsonNode idHash = fields.get(3);
			assertThat(idHash.get("name").asText()).isEqualTo("id_hash");
			assertThat(idHash.get("vector").asBoolean()).isTrue();
			assertThat(idHash.get("type").asText()).isEqualTo("core_long");

			// Verify dynamic config preserved
			JsonNode idField = fields.get(2);
			assertThat(idField.has("dynamic")).isTrue();
			assertThat(idField.get("dynamic").get("class").asText())
					.isEqualTo("multivalue-merger");
		}

		@Test
		@DisplayName("Should load MLS elem with all derivative fields from schema file")
		void shouldLoadMlsElemFromFile() throws Exception {
			File f = new File(schemasDir(), "core_mlselem.schema.json");

			ObjectNode typeJson = loader.loadOne(f);

			// Root-level metadata
			assertThat(typeJson.get("name").asText()).isEqualTo("mlselem");
			assertThat(typeJson.has("indexseeker")).isTrue();
			assertThat(typeJson.get("indexseeker").get("class").asText())
					.isEqualTo("iso-language-seeker");
			assertThat(typeJson.get("fetchlang").asBoolean()).isTrue();

			// Should have all 11 fields: lang, text, clean, dependency,
			// clean_normhash, pos, segmented_span, segmented,
			// segmented_parsed, segmented_answers, segmented_ner, segmented_normhash
			JsonNode fields = typeJson.get("fields");
			assertThat(fields.size()).isEqualTo(12);

			// text: i18n, no dynamic
			JsonNode textField = fieldByName(fields, "text");
			assertThat(textField.get("i18n").asBoolean()).isTrue();
			assertThat(textField.has("dynamic")).isFalse();

			// clean: i18n + dynamic (Html scrubber, reads .text) + index group
			JsonNode cleanField = fieldByName(fields, "clean");
			assertThat(cleanField.get("i18n").asBoolean()).isTrue();
			assertThat(cleanField.get("dynamic").get("class").asText())
					.isEqualTo("dynamic-mapper");
			assertThat(cleanField.get("dynamic").get("mapper").get("class").asText())
					.isEqualTo("html-scrubber");
			assertThat(cleanField.get("dynamic").get("fields").get(0).asText())
					.isEqualTo(".text");
			assertThat(cleanField.get("groups").get(0).get("name").asText())
					.isEqualTo("index");

			// segmented_ner: vector + i18n + NERMarkupMapper dynamic + two groups
			JsonNode nerField = fieldByName(fields, "segmented_ner");
			assertThat(nerField.get("vector").asBoolean()).isTrue();
			assertThat(nerField.get("i18n").asBoolean()).isTrue();
			assertThat(nerField.get("dynamic").get("class").asText())
					.isEqualTo("ner-markup");
			assertThat(nerField.get("dynamic").get("fields").get(0).asText())
					.isEqualTo(".lang");
			assertThat(nerField.get("dynamic").get("fields").get(1).asText())
					.isEqualTo(".segmented");
			// Two groups: index(textmarkup) + enrich(ner)
			assertThat(nerField.get("groups").size()).isEqualTo(2);
			assertThat(nerField.get("groups").get(0).get("method").asText())
					.isEqualTo("textmarkup");
			assertThat(nerField.get("groups").get(1).get("tags").get(0).asText())
					.isEqualTo("ner");

			// clean_normhash: vector, no i18n, DynamicMapper wrapping NormalizedTextHashMapper
			JsonNode hashField = fieldByName(fields, "clean_normhash");
			assertThat(hashField.get("vector").asBoolean()).isTrue();
			assertThat(hashField.has("i18n")).isFalse();
			assertThat(hashField.get("dynamic").get("mapper").get("class").asText())
					.isEqualTo("normalized-text-hash");
		}

		@Test
		@DisplayName("Should load demo_article extending sysobject from schema file")
		void shouldLoadArticleFromFile() throws Exception {
			File f = new File(schemasDir(), "demo_article.schema.json");

			ObjectNode typeJson = loader.loadOne(f);

			// Name and super
			assertThat(typeJson.get("name").asText()).isEqualTo("article");
			assertThat(typeJson.get("super").asText()).isEqualTo("sysobject");

			// Own fields (not inherited)
			JsonNode fields = typeJson.get("fields");
			assertThat(fields).isNotNull();
			assertThat(fields.size()).isEqualTo(8); // author, publication, published_date, category, tags, content, excerpt, source_url

			// Verify specific fields
			assertThat(fieldByName(fields, "author").get("type").asText()).isEqualTo("core_string");
			assertThat(fieldByName(fields, "published_date").get("type").asText()).isEqualTo("core_date");
			assertThat(fieldByName(fields, "source_url").get("type").asText()).isEqualTo("core_url");

			// Vector fields
			JsonNode categoryField = fieldByName(fields, "category");
			assertThat(categoryField.get("vector").asBoolean()).isTrue();
			assertThat(categoryField.get("type").asText()).isEqualTo("core_string");

			JsonNode tagsField = fieldByName(fields, "tags");
			assertThat(tagsField.get("vector").asBoolean()).isTrue();

			// MLS field
			assertThat(fieldByName(fields, "content").get("type").asText()).isEqualTo("core_mls");
		}
	}

	// ------------------------------------------------------------------
	// Loading from string
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Load schema from JSON string")
	class LoadFromString {

		@Test
		@DisplayName("Should load a primitive schema from string")
		void shouldLoadPrimitiveFromString() {
			String schema = """
					{
						"$schema": "https://json-schema.org/draft/2020-12/schema",
						"$id": "core_string.schema.json",
						"title": "string",
						"type": "string",
						"x-hitorro-name": "string",
						"x-hitorro-primitivetype": "string"
					}
					""";

			ObjectNode typeJson = loader.loadFromString(schema);

			assertThat(typeJson.get("name").asText()).isEqualTo("string");
			assertThat(typeJson.get("primitivetype").asText()).isEqualTo("string");
		}

		@Test
		@DisplayName("Should load a composite schema with file-based $ref from string")
		void shouldLoadCompositeFromString() {
			String schema = """
					{
						"title": "widget",
						"type": "object",
						"x-hitorro-name": "widget",
						"properties": {
							"label": {"$ref": "core_string.schema.json"},
							"count": {"$ref": "core_long.schema.json"},
							"values": {
								"type": "array",
								"items": {"$ref": "core_string.schema.json"}
							}
						}
					}
					""";

			ObjectNode typeJson = loader.loadFromString(schema);

			assertThat(typeJson.get("name").asText()).isEqualTo("widget");

			JsonNode fields = typeJson.get("fields");
			assertThat(fields.size()).isEqualTo(3);

			assertThat(fieldByName(fields, "label").get("type").asText()).isEqualTo("core_string");
			assertThat(fieldByName(fields, "count").get("type").asText()).isEqualTo("core_long");

			JsonNode valuesField = fieldByName(fields, "values");
			assertThat(valuesField.get("type").asText()).isEqualTo("core_string");
			assertThat(valuesField.get("vector").asBoolean()).isTrue();
		}
	}

	// ------------------------------------------------------------------
	// Loading a full directory
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Load directory of schema files")
	class LoadDirectory {

		@Test
		@DisplayName("Should load all schema files from directory")
		void shouldLoadAllFromDirectory() throws Exception {
			File dir = schemasDir();
			assertThat(dir).exists();

			Map<String, ObjectNode> types = loader.loadDirectory(dir);

			// Should have loaded all schema files
			assertThat(types).containsKey("core_string");
			assertThat(types).containsKey("core_long");
			assertThat(types).containsKey("core_id");
			assertThat(types).containsKey("core_sysobject");
			assertThat(types).containsKey("demo_article");

			// Verify a primitive
			ObjectNode stringType = types.get("core_string");
			assertThat(stringType.get("name").asText()).isEqualTo("string");
			assertThat(stringType.get("primitivetype").asText()).isEqualTo("string");

			// Verify article has super
			ObjectNode articleType = types.get("demo_article");
			assertThat(articleType.get("super").asText()).isEqualTo("sysobject");
		}

		@Test
		@DisplayName("Each loaded type should be independently valid")
		void eachTypeShouldBeIndependentlyValid() throws Exception {
			Map<String, ObjectNode> types = loader.loadDirectory(schemasDir());

			for (Map.Entry<String, ObjectNode> entry : types.entrySet()) {
				ObjectNode typeJson = entry.getValue();
				assertThat(typeJson.has("name"))
						.as("Type %s should have a name", entry.getKey())
						.isTrue();

				// Either primitive or composite
				boolean isPrimitive = typeJson.has("primitivetype");
				boolean hasFields = typeJson.has("fields");
				assertThat(isPrimitive || hasFields || typeJson.has("super"))
						.as("Type %s should be primitive, have fields, or extend a super",
								entry.getKey())
						.isTrue();
			}
		}
	}

	// ------------------------------------------------------------------
	// Structural comparison: schema-loaded vs original native config
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Schema vs native config comparison")
	class SchemaVsNative {

		@Test
		@DisplayName("demo_article from schema should match original native config structure")
		void articleSchemaShouldMatchNativeConfig() throws Exception {
			// Load from schema
			ObjectNode fromSchema = loader.loadOne(
					new File(schemasDir(), "demo_article.schema.json"));

			// The original native config
			String originalJson = """
					{
						"name": "article",
						"super": "sysobject",
						"fields": [
							{"name": "author", "type": "core_string"},
							{"name": "publication", "type": "core_string"},
							{"name": "published_date", "type": "core_date"},
							{"name": "category", "type": "core_string", "vector": true},
							{"name": "tags", "type": "core_string", "vector": true},
							{"name": "content", "type": "core_mls"},
							{"name": "excerpt", "type": "core_mls"},
							{"name": "source_url", "type": "core_url"}
						]
					}
					""";

			// Verify key structural properties match
			assertThat(fromSchema.get("name").asText()).isEqualTo("article");
			assertThat(fromSchema.get("super").asText()).isEqualTo("sysobject");

			JsonNode fields = fromSchema.get("fields");
			assertThat(fields.size()).isEqualTo(8);

			// Verify each field name, type, and vector flag
			String[][] expected = {
					{"author", "core_string", "false"},
					{"publication", "core_string", "false"},
					{"published_date", "core_date", "false"},
					{"category", "core_string", "true"},
					{"tags", "core_string", "true"},
					{"content", "core_mls", "false"},
					{"excerpt", "core_mls", "false"},
					{"source_url", "core_url", "false"},
			};

			for (int i = 0; i < expected.length; i++) {
				JsonNode field = fields.get(i);
				assertThat(field.get("name").asText())
						.as("Field %d name", i)
						.isEqualTo(expected[i][0]);
				assertThat(field.get("type").asText())
						.as("Field %d type", i)
						.isEqualTo(expected[i][1]);
				boolean expectVector = "true".equals(expected[i][2]);
				if (expectVector) {
					assertThat(field.get("vector").asBoolean())
							.as("Field %d vector", i)
							.isTrue();
				}
			}
		}
	}

	// ------------------------------------------------------------------

	private JsonNode fieldByName(JsonNode fields, String name) {
		for (JsonNode f : fields) {
			if (name.equals(f.get("name").asText())) {
				return f;
			}
		}
		fail("Field not found: " + name);
		return null;
	}
}
