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

@DisplayName("TypeDiff Tests")
class TypeDiffTest {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();

	private static JsonNode def(String json) { return jsonMapper.apply(json); }

	@Nested
	@DisplayName("Field changes")
	class FieldChanges {

		@Test
		@DisplayName("Identical types should have no changes")
		void identical() {
			JsonNode a = def("""
					{"name": "t", "fields": [{"name": "x", "type": "core_string"}]}""");

			List<TypeDiff.Change> changes = TypeDiff.diff(a, a);
			assertThat(changes).isEmpty();
		}

		@Test
		@DisplayName("Added field should be detected")
		void addedField() {
			JsonNode oldDef = def("""
					{"name": "t", "fields": [{"name": "a", "type": "core_string"}]}""");
			JsonNode newDef = def("""
					{"name": "t", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_long"}
					]}""");

			List<TypeDiff.Change> changes = TypeDiff.diff(oldDef, newDef);
			assertThat(changes).anyMatch(c ->
					c.kind() == TypeDiff.ChangeKind.ADDED && c.fieldName().equals("b"));
		}

		@Test
		@DisplayName("Removed field should be detected")
		void removedField() {
			JsonNode oldDef = def("""
					{"name": "t", "fields": [
						{"name": "a", "type": "core_string"},
						{"name": "b", "type": "core_long"}
					]}""");
			JsonNode newDef = def("""
					{"name": "t", "fields": [{"name": "a", "type": "core_string"}]}""");

			List<TypeDiff.Change> changes = TypeDiff.diff(oldDef, newDef);
			assertThat(changes).anyMatch(c ->
					c.kind() == TypeDiff.ChangeKind.REMOVED && c.fieldName().equals("b"));
		}

		@Test
		@DisplayName("Changed field type should be detected")
		void changedType() {
			JsonNode oldDef = def("""
					{"name": "t", "fields": [{"name": "x", "type": "core_string"}]}""");
			JsonNode newDef = def("""
					{"name": "t", "fields": [{"name": "x", "type": "core_long"}]}""");

			List<TypeDiff.Change> changes = TypeDiff.diff(oldDef, newDef);
			assertThat(changes).anyMatch(c ->
					c.kind() == TypeDiff.ChangeKind.MODIFIED && c.fieldName().equals("x"));
		}

		@Test
		@DisplayName("Vector flag change should be detected")
		void vectorChange() {
			JsonNode oldDef = def("""
					{"name": "t", "fields": [{"name": "x", "type": "core_string"}]}""");
			JsonNode newDef = def("""
					{"name": "t", "fields": [{"name": "x", "type": "core_string", "vector": true}]}""");

			List<TypeDiff.Change> changes = TypeDiff.diff(oldDef, newDef);
			assertThat(changes).anyMatch(c ->
					c.kind() == TypeDiff.ChangeKind.MODIFIED && c.fieldName().equals("x"));
		}
	}

	@Nested
	@DisplayName("Report format")
	class Report {

		@Test
		@DisplayName("Should produce readable diff report")
		void readableReport() {
			JsonNode oldDef = def("""
					{"name": "test", "fields": [
						{"name": "kept", "type": "core_string"},
						{"name": "removed", "type": "core_long"}
					]}""");
			JsonNode newDef = def("""
					{"name": "test", "fields": [
						{"name": "kept", "type": "core_string"},
						{"name": "added", "type": "core_boolean"}
					]}""");

			String report = TypeDiff.report(oldDef, newDef);
			assertThat(report).contains("ADDED");
			assertThat(report).contains("REMOVED");
			assertThat(report).contains("added");
			assertThat(report).contains("removed");
		}

		@Test
		@DisplayName("No changes should report clean")
		void noChanges() {
			JsonNode a = def("""
					{"name": "t", "fields": [{"name": "x", "type": "core_string"}]}""");

			String report = TypeDiff.report(a, a);
			assertThat(report).contains("No changes");
		}
	}

	@Nested
	@DisplayName("Migration script generation")
	class MigrationScript {

		@Test
		@DisplayName("Should generate Groovy migration script")
		void generateScript() {
			JsonNode oldDef = def("""
					{"name": "test", "fields": [
						{"name": "old_field", "type": "core_string"},
						{"name": "kept", "type": "core_string"}
					]}""");
			JsonNode newDef = def("""
					{"name": "test", "fields": [
						{"name": "kept", "type": "core_string"},
						{"name": "new_field", "type": "core_long"}
					]}""");

			String script = TypeDiff.generateMigrationScript(oldDef, newDef);
			assertThat(script).contains("copyAll()");
			assertThat(script).contains("delete");
			assertThat(script).contains("old_field");
			assertThat(script).contains("new_field");
		}
	}
}
