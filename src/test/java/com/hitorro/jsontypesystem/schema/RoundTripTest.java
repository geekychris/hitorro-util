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
import com.hitorro.jsontypesystem.JsonTypeSystem;
import com.hitorro.jsontypesystem.Type;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Round-trip conversion tests (Type -> Schema -> Native JSON)")
@Disabled("Requires type system initialization with config files")
class RoundTripTest {

	private final Type2JsonSchemaConverter toSchema = new Type2JsonSchemaConverter();
	private final JsonSchema2TypeConverter fromSchema = new JsonSchema2TypeConverter();

	@Test
	@DisplayName("Should round-trip core types")
	void shouldRoundTripCoreTypes() {
		String[] typeNames = {
				"core_string", "core_long", "core_boolean", "core_date",
				"core_id", "core_mls", "core_mlselem", "core_dates"
		};

		for (String typeName : typeNames) {
			verifyRoundTrip(typeName);
		}
	}

	private void verifyRoundTrip(String typeName) {
		Type type = JsonTypeSystem.getMe().getType(typeName);
		assertThat(type).as("Type %s should exist", typeName).isNotNull();

		// Type -> Schema
		ObjectNode schema = toSchema.convert(type);
		assertThat(schema).isNotNull();
		assertThat(schema.has("x-hitorro-name")).isTrue();

		// Schema -> Native JSON
		ObjectNode nativeJson = fromSchema.convert(schema);
		assertThat(nativeJson).isNotNull();

		// Verify structural equality of key properties
		JsonNode metaNode = type.getMetaNode();
		assertThat(nativeJson.get("name").asText())
				.as("Round-trip name for %s", typeName)
				.isEqualTo(metaNode.get("name").asText());

		// Primitive types should round-trip the primitivetype field
		if (type.isPrimitiveType()) {
			assertThat(nativeJson.has("primitivetype")).isTrue();
			assertThat(nativeJson.get("primitivetype").asText())
					.isEqualTo(metaNode.get("primitivetype").asText());
		}

		// Composite types should preserve field count
		if (metaNode.has("fields") && metaNode.get("fields").isArray()) {
			assertThat(nativeJson.has("fields")).isTrue();
			assertThat(nativeJson.get("fields").size())
					.as("Field count for %s", typeName)
					.isEqualTo(metaNode.get("fields").size());
		}
	}
}
