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
import com.hitorro.util.json.String2JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Loads each type from both the native config (.json) and the JSON Schema (.schema.json),
 * then uses TypeConfigIntegrityChecker to verify structural equivalence.
 *
 * This test does NOT require the type system to be initialized — it compares the
 * raw JSON configs directly, which is exactly what Type.init() would consume.
 */
@DisplayName("Type Config Integrity — native vs schema-loaded")
class TypeConfigIntegrityTest {

	private final TypeConfigIntegrityChecker checker = new TypeConfigIntegrityChecker();
	private final SchemaFileLoader schemaLoader = new SchemaFileLoader();
	private final String2JsonMapper jsonMapper = new String2JsonMapper();

	private File nativeTypesDir() {
		// hitorro-util/config/types/ has the core types
		File dir = new File("config/types");
		if (!dir.exists()) {
			dir = new File("hitorro-util/config/types");
		}
		return dir;
	}

	private File schemasDir() {
		File dir = new File("config/schemas");
		if (!dir.exists()) {
			dir = new File("hitorro-util/config/schemas");
		}
		return dir;
	}

	private JsonNode loadNative(String configName) throws IOException {
		File f = new File(nativeTypesDir(), configName + ".json");
		if (!f.exists()) {
			// Try the project root config/types for demo types
			f = new File("../config/types/" + configName + ".json");
		}
		assertThat(f).as("Native config %s", configName).exists();
		String json = new String(Files.readAllBytes(f.toPath()));
		return jsonMapper.apply(json);
	}

	private ObjectNode loadFromSchema(String configName) throws IOException {
		File f = new File(schemasDir(), configName + ".schema.json");
		assertThat(f).as("Schema file %s", configName).exists();
		return schemaLoader.loadOne(f);
	}

	/**
	 * Verify that schema-converted config matches native config for a given type.
	 * Returns the diff list for further inspection.
	 */
	private List<String> verifyType(String configName) throws IOException {
		JsonNode nativeConfig = loadNative(configName);
		ObjectNode fromSchema = loadFromSchema(configName);

		List<String> diffs = checker.compare(nativeConfig, fromSchema);
		if (!diffs.isEmpty()) {
			System.err.println("Integrity check for " + configName + ":");
			System.err.println(checker.report(nativeConfig, fromSchema));
		}
		return diffs;
	}

	// ------------------------------------------------------------------
	// Primitive types
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Primitive types")
	class Primitives {

		@Test
		@DisplayName("core_string: native vs schema should match")
		void coreString() throws Exception {
			assertThat(verifyType("core_string")).isEmpty();
		}

		@Test
		@DisplayName("core_long: native vs schema should match")
		void coreLong() throws Exception {
			assertThat(verifyType("core_long")).isEmpty();
		}

		@Test
		@DisplayName("core_boolean: native vs schema should match")
		void coreBoolean() throws Exception {
			assertThat(verifyType("core_boolean")).isEmpty();
		}

		@Test
		@DisplayName("core_date: native vs schema should match")
		void coreDate() throws Exception {
			assertThat(verifyType("core_date")).isEmpty();
		}
	}

	// ------------------------------------------------------------------
	// Composite types
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Composite types")
	class Composites {

		@Test
		@DisplayName("core_id: native vs schema should match (dynamic fields, groups)")
		void coreId() throws Exception {
			assertThat(verifyType("core_id")).isEmpty();
		}

		@Test
		@DisplayName("core_dates: native vs schema should match (groups)")
		void coreDates() throws Exception {
			assertThat(verifyType("core_dates")).isEmpty();
		}

		@Test
		@DisplayName("core_mls: native vs schema should match")
		void coreMls() throws Exception {
			assertThat(verifyType("core_mls")).isEmpty();
		}

		@Test
		@DisplayName("core_mlselem: native vs schema should match (all derivative fields)")
		void coreMlselem() throws Exception {
			List<String> diffs = verifyType("core_mlselem");
			assertThat(diffs).as("mlselem diffs: %s", diffs).isEmpty();
		}

		@Test
		@DisplayName("core_sysobject: native vs schema should match")
		void coreSysobject() throws Exception {
			assertThat(verifyType("core_sysobject")).isEmpty();
		}
	}

	// ------------------------------------------------------------------
	// Inherited types (extends sysobject)
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("Inherited types")
	class Inherited {

		@Test
		@DisplayName("demo_article: native vs schema should match (extends sysobject)")
		void demoArticle() throws Exception {
			assertThat(verifyType("demo_article")).isEmpty();
		}
	}

	// ------------------------------------------------------------------
	// Checker utility tests
	// ------------------------------------------------------------------

	@Nested
	@DisplayName("TypeConfigIntegrityChecker behavior")
	class CheckerBehavior {

		@Test
		@DisplayName("Should report no diffs for identical configs")
		void noDiffsForIdentical() {
			JsonNode a = jsonMapper.apply("{\"name\":\"test\",\"primitivetype\":\"string\"}");
			JsonNode b = jsonMapper.apply("{\"name\":\"test\",\"primitivetype\":\"string\"}");

			assertThat(checker.compare(a, b)).isEmpty();
		}

		@Test
		@DisplayName("Should detect name difference")
		void detectNameDiff() {
			JsonNode a = jsonMapper.apply("{\"name\":\"foo\"}");
			JsonNode b = jsonMapper.apply("{\"name\":\"bar\"}");

			List<String> diffs = checker.compare(a, b);
			assertThat(diffs).anyMatch(d -> d.contains("name"));
		}

		@Test
		@DisplayName("Should detect missing field")
		void detectMissingField() {
			JsonNode a = jsonMapper.apply("{\"name\":\"t\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":\"long\"}]}");
			JsonNode b = jsonMapper.apply("{\"name\":\"t\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"}]}");

			List<String> diffs = checker.compare(a, b);
			assertThat(diffs).anyMatch(d -> d.contains("y") && d.contains("missing"));
		}

		@Test
		@DisplayName("Should detect field type mismatch")
		void detectFieldTypeMismatch() {
			JsonNode a = jsonMapper.apply("{\"name\":\"t\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"}]}");
			JsonNode b = jsonMapper.apply("{\"name\":\"t\",\"fields\":[{\"name\":\"x\",\"type\":\"long\"}]}");

			List<String> diffs = checker.compare(a, b);
			assertThat(diffs).anyMatch(d -> d.contains("type") && d.contains("string") && d.contains("long"));
		}

		@Test
		@DisplayName("Should detect dynamic config mismatch")
		void detectDynamicMismatch() {
			String base = "{\"name\":\"t\",\"fields\":[{\"name\":\"x\",\"type\":\"s\",\"dynamic\":{\"class\":\"A\",\"fields\":[\".a\"]}}]}";
			String diff = "{\"name\":\"t\",\"fields\":[{\"name\":\"x\",\"type\":\"s\",\"dynamic\":{\"class\":\"B\",\"fields\":[\".a\"]}}]}";

			List<String> diffs = checker.compare(jsonMapper.apply(base), jsonMapper.apply(diff));
			assertThat(diffs).anyMatch(d -> d.contains("dynamic") && d.contains("class"));
		}

		@Test
		@DisplayName("Should report a readable report")
		void readableReport() {
			JsonNode a = jsonMapper.apply("{\"name\":\"foo\"}");
			JsonNode b = jsonMapper.apply("{\"name\":\"bar\"}");

			String report = checker.report(a, b);
			assertThat(report).contains("difference");
			assertThat(report).contains("foo");
			assertThat(report).contains("bar");
		}

		@Test
		@DisplayName("Should report OK for matching configs")
		void reportOkForMatch() {
			JsonNode a = jsonMapper.apply("{\"name\":\"test\"}");
			String report = checker.report(a, a);
			assertThat(report).contains("OK");
		}
	}
}
