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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

import static com.hitorro.jsontypesystem.schema.HiTorroSchemaConstants.*;

/**
 * Converts a JSON Schema (with HiTorro extensions) back to the native type JSON format
 * that Type.init() expects.
 */
public class JsonSchema2TypeConverter {

	public ObjectNode convert(JsonNode schema) {
		ObjectNode typeJson = JsonNodeFactory.instance.objectNode();

		// Name
		if (schema.has(X_HITORRO_NAME)) {
			typeJson.put("name", schema.get(X_HITORRO_NAME).asText());
		} else if (schema.has("title")) {
			typeJson.put("name", schema.get("title").asText());
		}

		// Primitive type
		if (schema.has(X_HITORRO_PRIMITIVETYPE)) {
			typeJson.put("primitivetype", schema.get(X_HITORRO_PRIMITIVETYPE).asText());
			return typeJson;
		}

		// Super type
		if (schema.has(X_HITORRO_SUPER)) {
			typeJson.put("super", schema.get(X_HITORRO_SUPER).asText());
		}

		// Index seeker
		if (schema.has(X_HITORRO_INDEXSEEKER)) {
			typeJson.set("indexseeker", schema.get(X_HITORRO_INDEXSEEKER).deepCopy());
		}

		// Fetch lang
		if (schema.has(X_HITORRO_FETCHLANG) && schema.get(X_HITORRO_FETCHLANG).asBoolean()) {
			typeJson.put("fetchlang", true);
		}

		// Properties -> fields
		ObjectNode properties = findProperties(schema);
		if (properties != null && properties.size() > 0) {
			ArrayNode fields = JsonNodeFactory.instance.arrayNode();
			Iterator<Map.Entry<String, JsonNode>> iter = properties.fields();
			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				ObjectNode field = convertProperty(entry.getKey(), entry.getValue());
				if (field != null) {
					fields.add(field);
				}
			}
			typeJson.set("fields", fields);
		}

		return typeJson;
	}

	private ObjectNode findProperties(JsonNode schema) {
		// Direct properties
		if (schema.has("properties")) {
			return (ObjectNode) schema.get("properties");
		}

		// allOf inheritance — find the object with own properties
		if (schema.has("allOf")) {
			ArrayNode allOf = (ArrayNode) schema.get("allOf");
			for (JsonNode element : allOf) {
				if (element.has("properties")) {
					return (ObjectNode) element.get("properties");
				}
			}
		}

		return null;
	}

	private ObjectNode convertProperty(String name, JsonNode propSchema) {
		ObjectNode field = JsonNodeFactory.instance.objectNode();
		field.put("name", name);

		boolean isVector = false;
		String typeRef = null;

		if (propSchema.has("type") && "array".equals(propSchema.get("type").asText())) {
			isVector = true;
			if (propSchema.has("items") && propSchema.get("items").has("$ref")) {
				typeRef = propSchema.get("items").get("$ref").asText();
			}
		} else if (propSchema.has("$ref")) {
			typeRef = propSchema.get("$ref").asText();
		}

		if (typeRef != null) {
			String typeName = refToTypeName(typeRef);
			field.put("type", typeName);
		}

		if (isVector) {
			field.put("vector", true);
		}

		// Dynamic
		if (propSchema.has(X_HITORRO_DYNAMIC)) {
			field.set("dynamic", propSchema.get(X_HITORRO_DYNAMIC).deepCopy());
		}

		// Groups
		if (propSchema.has(X_HITORRO_GROUPS)) {
			field.set("groups", propSchema.get(X_HITORRO_GROUPS).deepCopy());
		}

		// i18n
		if (propSchema.has(X_HITORRO_I18N) && propSchema.get(X_HITORRO_I18N).asBoolean()) {
			field.put("i18n", true);
		}

		return field;
	}

	/**
	 * Convert a $ref value to a native type name.
	 * Handles both bundled refs (#/$defs/core_string) and file refs (core_string.schema.json).
	 */
	private String refToTypeName(String ref) {
		// File-based ref: "core_string.schema.json" -> "core_string"
		if (ref.endsWith(".schema.json")) {
			return ref.substring(0, ref.length() - ".schema.json".length());
		}

		// Bundled ref: "#/$defs/core_string" -> "core_string"
		int lastSlash = ref.lastIndexOf('/');
		if (lastSlash != -1) {
			return ref.substring(lastSlash + 1);
		}
		return ref;
	}
}
