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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TypeVersion Tests")
class TypeVersionTest {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();

	@Test
	@DisplayName("Should extract version from type definition")
	void extractVersion() {
		JsonNode def = jsonMapper.apply("""
				{"name": "test", "version": "2.1.0", "fields": []}""");

		TypeVersion v = TypeVersion.fromDefinition(def);
		assertThat(v.version()).isEqualTo("2.1.0");
		assertThat(v.name()).isEqualTo("test");
	}

	@Test
	@DisplayName("Missing version should default to 1.0.0")
	void missingVersion() {
		JsonNode def = jsonMapper.apply("""
				{"name": "test", "fields": []}""");

		TypeVersion v = TypeVersion.fromDefinition(def);
		assertThat(v.version()).isEqualTo("1.0.0");
	}

	@Test
	@DisplayName("Should compare versions")
	void compareVersions() {
		TypeVersion v1 = new TypeVersion("test", "1.0.0");
		TypeVersion v2 = new TypeVersion("test", "2.0.0");
		TypeVersion v1b = new TypeVersion("test", "1.0.0");

		assertThat(v1.compareTo(v2)).isLessThan(0);
		assertThat(v2.compareTo(v1)).isGreaterThan(0);
		assertThat(v1.compareTo(v1b)).isEqualTo(0);
	}

	@Test
	@DisplayName("Should detect version bump type")
	void bumpDetection() {
		TypeVersion v1 = new TypeVersion("t", "1.2.3");

		assertThat(v1.bumpMajor().version()).isEqualTo("2.0.0");
		assertThat(v1.bumpMinor().version()).isEqualTo("1.3.0");
		assertThat(v1.bumpPatch().version()).isEqualTo("1.2.4");
	}

	@Test
	@DisplayName("Should determine required bump from diff")
	void suggestedBump() {
		JsonNode def1 = jsonMapper.apply("""
				{"name": "t", "version": "1.0.0", "fields": [
					{"name": "a", "type": "core_string"}
				]}""");

		// Removing a field is a major change
		JsonNode defRemoved = jsonMapper.apply("""
				{"name": "t", "version": "1.0.0", "fields": []}""");

		assertThat(TypeVersion.suggestedBump(def1, defRemoved)).isEqualTo("major");

		// Adding a field is a minor change
		JsonNode defAdded = jsonMapper.apply("""
				{"name": "t", "version": "1.0.0", "fields": [
					{"name": "a", "type": "core_string"},
					{"name": "b", "type": "core_long"}
				]}""");

		assertThat(TypeVersion.suggestedBump(def1, defAdded)).isEqualTo("minor");

		// No change = no bump
		assertThat(TypeVersion.suggestedBump(def1, def1)).isEqualTo("none");
	}
}
