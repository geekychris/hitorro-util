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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static com.hitorro.jsontypesystem.schema.HiTorroSchemaConstants.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Type2JsonSchemaConverter Tests")
@Disabled("Requires type system initialization with config files")
class Type2JsonSchemaConverterTest {

	private final Type2JsonSchemaConverter converter = new Type2JsonSchemaConverter();

	@Test
	@DisplayName("Should convert primitive string type")
	void shouldConvertPrimitiveString() {
		Type type = JsonTypeSystem.getMe().getType("core_string");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		assertThat(schema.get("$schema").asText()).isEqualTo(SCHEMA_URI);
		assertThat(schema.get(X_HITORRO_NAME).asText()).isEqualTo("string");
		assertThat(schema.get("type").asText()).isEqualTo("string");
		assertThat(schema.get(X_HITORRO_PRIMITIVETYPE).asText()).isEqualTo("string");
	}

	@Test
	@DisplayName("Should convert primitive long type")
	void shouldConvertPrimitiveLong() {
		Type type = JsonTypeSystem.getMe().getType("core_long");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		assertThat(schema.get("type").asText()).isEqualTo("integer");
		assertThat(schema.get(X_HITORRO_PRIMITIVETYPE).asText()).isEqualTo("long");
	}

	@Test
	@DisplayName("Should convert primitive boolean type")
	void shouldConvertPrimitiveBoolean() {
		Type type = JsonTypeSystem.getMe().getType("core_boolean");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		assertThat(schema.get("type").asText()).isEqualTo("boolean");
		assertThat(schema.get(X_HITORRO_PRIMITIVETYPE).asText()).isEqualTo("boolean");
	}

	@Test
	@DisplayName("Should convert primitive date type")
	void shouldConvertPrimitiveDate() {
		Type type = JsonTypeSystem.getMe().getType("core_date");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		assertThat(schema.get("type").asText()).isEqualTo("string");
		assertThat(schema.get(X_HITORRO_PRIMITIVETYPE).asText()).isEqualTo("date");
	}

	@Test
	@DisplayName("Should convert composite id type with dynamic fields")
	void shouldConvertCompositeIdType() {
		Type type = JsonTypeSystem.getMe().getType("core_id");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		assertThat(schema.get("type").asText()).isEqualTo("object");
		assertThat(schema.has("properties")).isTrue();

		JsonNode props = schema.get("properties");
		assertThat(props.has("domain")).isTrue();
		assertThat(props.has("did")).isTrue();
		assertThat(props.has("id")).isTrue();
		assertThat(props.has("id_hash")).isTrue();

		// id field should have dynamic config
		JsonNode idProp = props.get("id");
		assertThat(idProp.has(X_HITORRO_DYNAMIC)).isTrue();
		assertThat(idProp.has(X_HITORRO_GROUPS)).isTrue();

		// id_hash should be a vector (array type)
		JsonNode idHashProp = props.get("id_hash");
		assertThat(idHashProp.get("type").asText()).isEqualTo("array");
		assertThat(idHashProp.has("items")).isTrue();
	}

	@Test
	@DisplayName("Should convert MLS elem type with indexseeker")
	void shouldConvertMlsElemWithIndexSeeker() {
		Type type = JsonTypeSystem.getMe().getType("core_mlselem");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		assertThat(schema.has(X_HITORRO_INDEXSEEKER)).isTrue();
		assertThat(schema.get(X_HITORRO_FETCHLANG).asBoolean()).isTrue();

		JsonNode props = schema.get("properties");
		assertThat(props.has("lang")).isTrue();
		assertThat(props.has("text")).isTrue();

		// text should have i18n marker
		assertThat(props.get("text").has(X_HITORRO_I18N)).isTrue();
	}

	@Test
	@DisplayName("Should convert mls type with vector field")
	void shouldConvertMlsTypeWithVectorField() {
		Type type = JsonTypeSystem.getMe().getType("core_mls");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		JsonNode props = schema.get("properties");
		assertThat(props.has("mls")).isTrue();

		JsonNode mlsProp = props.get("mls");
		assertThat(mlsProp.get("type").asText()).isEqualTo("array");
	}

	@Test
	@DisplayName("Should convert dates type with groups")
	void shouldConvertDatesTypeWithGroups() {
		Type type = JsonTypeSystem.getMe().getType("core_dates");
		assertThat(type).isNotNull();

		ObjectNode schema = converter.convert(type);

		JsonNode props = schema.get("properties");
		assertThat(props.has("created")).isTrue();
		assertThat(props.has("modified")).isTrue();

		// created should have groups
		assertThat(props.get("created").has(X_HITORRO_GROUPS)).isTrue();
	}
}
