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
import com.hitorro.jsontypesystem.IndexSeeker;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.util.typesystem.TypeFieldDataType;

import static com.hitorro.jsontypesystem.schema.HiTorroSchemaConstants.*;

public class Type2JsonSchemaConverter {

	public enum RefStyle {
		/** Bundled: $ref uses "#/$defs/core_string" */
		BUNDLED,
		/** File-based: $ref uses "core_string.schema.json" */
		FILE
	}

	private final RefStyle refStyle;

	public Type2JsonSchemaConverter() {
		this(RefStyle.BUNDLED);
	}

	public Type2JsonSchemaConverter(RefStyle refStyle) {
		this.refStyle = refStyle;
	}

	public ObjectNode convert(Type type) {
		ObjectNode schema = JsonNodeFactory.instance.objectNode();
		schema.put("$schema", SCHEMA_URI);

		String typeName = type.getName();
		String configName = getConfigName(type);

		if (refStyle == RefStyle.FILE) {
			schema.put("$id", configName + ".schema.json");
		}

		schema.put("title", typeName);
		schema.put(X_HITORRO_NAME, typeName);

		if (type.isPrimitiveType()) {
			return convertPrimitive(schema, type);
		}

		return convertComposite(schema, type);
	}

	private ObjectNode convertPrimitive(ObjectNode schema, Type type) {
		TypeFieldDataType pt = type.getPrimitiveType();
		schema.put("type", mapPrimitiveToJsonSchemaType(pt));
		schema.put(X_HITORRO_PRIMITIVETYPE, pt.getShortName());
		return schema;
	}

	private ObjectNode convertComposite(ObjectNode schema, Type type) {
		Type superType = type.getSuper();
		JsonNode metaNode = type.getMetaNode();

		// Handle index seeker
		IndexSeeker indexSeeker = type.getIndexSeeker();
		if (indexSeeker != null && metaNode != null) {
			JsonNode seekerNode = metaNode.get("indexseeker");
			if (seekerNode != null) {
				schema.set(X_HITORRO_INDEXSEEKER, seekerNode.deepCopy());
			}
		}

		// Handle fetchlang
		if (type.fetchLang()) {
			schema.put(X_HITORRO_FETCHLANG, true);
		}

		if (superType != null) {
			// Inheritance via allOf
			schema.put(X_HITORRO_SUPER, superType.getName());
			ArrayNode allOf = JsonNodeFactory.instance.arrayNode();

			ObjectNode superRef = JsonNodeFactory.instance.objectNode();
			superRef.put("$ref", makeRef(getConfigName(superType)));
			allOf.add(superRef);

			ObjectNode ownProps = JsonNodeFactory.instance.objectNode();
			ownProps.put("type", "object");
			addProperties(ownProps, type);
			allOf.add(ownProps);

			schema.set("allOf", allOf);
		} else {
			schema.put("type", "object");
			addProperties(schema, type);
		}

		return schema;
	}

	private void addProperties(ObjectNode target, Type type) {
		JsonNode metaNode = type.getMetaNode();
		if (metaNode == null) {
			return;
		}

		JsonNode fieldsNode = metaNode.get("fields");
		if (fieldsNode == null || !fieldsNode.isArray()) {
			return;
		}

		ObjectNode properties = JsonNodeFactory.instance.objectNode();

		for (JsonNode fieldMeta : fieldsNode) {
			String fieldName = fieldMeta.has("name") ? fieldMeta.get("name").asText() : null;
			if (fieldName == null) {
				continue;
			}
			String fieldTypeName = fieldMeta.has("type") ? fieldMeta.get("type").asText() : null;
			if (fieldTypeName == null) {
				continue;
			}

			boolean isVector = fieldMeta.has("vector") && fieldMeta.get("vector").asBoolean();

			ObjectNode propSchema = JsonNodeFactory.instance.objectNode();

			String typeConfigName = normalizeTypeName(fieldTypeName);
			String typeRef = makeRef(typeConfigName);

			if (isVector) {
				propSchema.put("type", "array");
				ObjectNode items = JsonNodeFactory.instance.objectNode();
				items.put("$ref", typeRef);
				propSchema.set("items", items);
			} else {
				propSchema.put("$ref", typeRef);
			}

			// Dynamic field config — read from meta node directly
			JsonNode dynamicNode = fieldMeta.get("dynamic");
			if (dynamicNode != null && !dynamicNode.isNull()) {
				propSchema.set(X_HITORRO_DYNAMIC, dynamicNode.deepCopy());
			}

			// Groups
			JsonNode groupsNode = fieldMeta.get("groups");
			if (groupsNode != null && groupsNode.isArray() && groupsNode.size() > 0) {
				propSchema.set(X_HITORRO_GROUPS, groupsNode.deepCopy());
			}

			// i18n
			if (fieldMeta.has("i18n") && fieldMeta.get("i18n").asBoolean()) {
				propSchema.put(X_HITORRO_I18N, true);
			}

			properties.set(fieldName, propSchema);
		}

		if (properties.size() > 0) {
			target.set("properties", properties);
		}
	}

	private String makeRef(String configName) {
		if (refStyle == RefStyle.FILE) {
			return configName + ".schema.json";
		}
		return "#/$defs/" + configName;
	}

	/**
	 * Ensure type name has the "core_" prefix for config file naming.
	 */
	private String normalizeTypeName(String typeName) {
		if (typeName.startsWith("core_") || typeName.startsWith("demo_")) {
			return typeName;
		}
		return "core_" + typeName;
	}

	/**
	 * Get the config file name for a type (e.g. "core_string" for type named "string").
	 */
	private String getConfigName(Type type) {
		JsonNode metaNode = type.getMetaNode();
		// If the meta node was loaded from a config named "core_X", try to recover that
		String name = type.getName();
		if (!name.startsWith("core_") && !name.startsWith("demo_")) {
			return "core_" + name;
		}
		return name;
	}

	private String mapPrimitiveToJsonSchemaType(TypeFieldDataType pt) {
		switch (pt) {
			case Long:
			case Int:
			case Short:
			case Byte:
				return "integer";
			case Double:
			case Float:
				return "number";
			case Boolean:
				return "boolean";
			case String:
				return "string";
			case Date:
				return "string";
			default:
				return "object";
		}
	}
}
