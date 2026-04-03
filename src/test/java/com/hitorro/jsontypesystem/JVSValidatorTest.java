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

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVSValidator Tests")
class JVSValidatorTest {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();

	private static JsonNode typeDef(String json) {
		return jsonMapper.apply(json);
	}

	@Nested
	@DisplayName("Structural validation")
	class StructuralValidation {

		@Test
		@DisplayName("Valid document should produce no violations")
		void validDocument() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "title", "type": "core_string"},
						{"name": "count", "type": "core_long"}
					]}""");

			JVS doc = JVS.read("{\"title\":\"hello\",\"count\":42}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).isEmpty();
		}

		@Test
		@DisplayName("Missing field should report violation")
		void missingField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "title", "type": "core_string"},
						{"name": "count", "type": "core_long"}
					]}""");

			JVS doc = JVS.read("{\"title\":\"hello\"}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("count") && v.level() == JVSValidator.Level.WARNING);
		}

		@Test
		@DisplayName("Extra field not in type should report info violation")
		void extraField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "title", "type": "core_string"}
					]}""");

			JVS doc = JVS.read("{\"title\":\"hello\",\"extra\":\"unexpected\"}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("extra") && v.level() == JVSValidator.Level.INFO);
		}

		@Test
		@DisplayName("Empty document should report all fields as missing")
		void emptyDocument() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_string"},
						{"name": "c", "type": "core_string"}
					]}""");

			JVS doc = JVS.read("{}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).hasSize(3);
		}
	}

	@Nested
	@DisplayName("Type mismatch detection")
	class TypeMismatch {

		@Test
		@DisplayName("String where number expected should report error")
		void stringWhereNumberExpected() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "count", "type": "core_long"}
					]}""");

			JVS doc = JVS.read("{\"count\":\"not a number\"}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("count") && v.level() == JVSValidator.Level.ERROR);
		}

		@Test
		@DisplayName("Number where string expected should report error")
		void numberWhereStringExpected() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "name", "type": "core_string"}
					]}""");

			JVS doc = JVS.read("{\"name\":42}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("name") && v.level() == JVSValidator.Level.ERROR);
		}

		@Test
		@DisplayName("Scalar where vector expected should report error")
		void scalarWhereVectorExpected() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "tags", "type": "core_string", "vector": true}
					]}""");

			JVS doc = JVS.read("{\"tags\":\"single\"}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("tags") && v.level() == JVSValidator.Level.ERROR);
		}

		@Test
		@DisplayName("Array where scalar expected should report error")
		void arrayWhereScalarExpected() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "name", "type": "core_string"}
					]}""");

			JVS doc = JVS.read("{\"name\":[\"a\",\"b\"]}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("name") && v.level() == JVSValidator.Level.ERROR);
		}
	}

	@Nested
	@DisplayName("Null handling")
	class NullHandling {

		@Test
		@DisplayName("Null value should report warning")
		void nullField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "data", "type": "core_string"}
					]}""");

			JVS doc = JVS.read("{\"data\":null}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).anyMatch(v ->
					v.path().equals("data") && v.level() == JVSValidator.Level.WARNING);
		}
	}

	@Nested
	@DisplayName("Dynamic field handling")
	class DynamicFields {

		@Test
		@DisplayName("Missing dynamic field should not be a violation")
		void missingDynamicField() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "source", "type": "core_string"},
						{"name": "computed", "type": "core_string",
						 "dynamic": {"class": "dynamic-mapper", "mapper": {"class": "fp-hash"}, "fields": [".source"]}}
					]}""");

			JVS doc = JVS.read("{\"source\":\"hello\"}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).noneMatch(v -> v.path().equals("computed"));
		}
	}

	@Nested
	@DisplayName("Violation model")
	class ViolationModel {

		@Test
		@DisplayName("Violation should have path, message, and level")
		void violationStructure() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "required", "type": "core_string"}
					]}""");

			JVS doc = JVS.read("{}");

			List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, def);
			assertThat(violations).hasSize(1);

			JVSValidator.Violation v = violations.get(0);
			assertThat(v.path()).isEqualTo("required");
			assertThat(v.message()).isNotEmpty();
			assertThat(v.level()).isEqualTo(JVSValidator.Level.WARNING);
		}

		@Test
		@DisplayName("Report should produce readable summary")
		void reportFormat() {
			JsonNode def = typeDef("""
					{"name": "test", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_long"}
					]}""");

			JVS doc = JVS.read("{\"b\":\"wrong type\"}");

			String report = JVSValidator.reportAgainstDefinition(doc, def);
			assertThat(report).contains("a");
			assertThat(report).contains("b");
		}
	}
}
