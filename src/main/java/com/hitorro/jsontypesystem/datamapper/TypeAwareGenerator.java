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
package com.hitorro.jsontypesystem.datamapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Type;

/**
 * Generates JVS documents from Type definitions using DataGenerators.
 * Reads the type's field definitions and produces appropriate generated values
 * for each field based on its primitive type. Dynamic fields are skipped
 * (they would be computed during enrichment).
 */
public class TypeAwareGenerator {

	/**
	 * Generate a JVS document from a Type object.
	 */
	public static JVS generate(Type type, DataGenerators gen) {
		return generateFromDefinition(type.getMetaNode(), gen);
	}

	/**
	 * Generate a JVS document from a raw type definition JsonNode.
	 * Does not require Type.init() or JsonTypeSystem — works with the raw JSON.
	 */
	public static JVS generateFromDefinition(JsonNode typeDef, DataGenerators gen) {
		var doc = new JVS();

		// Set type name
		String typeName = typeDef.has("name") ? typeDef.get("name").asText() : "generated";
		doc.set("type", typeName);

		if (!typeDef.has("fields")) return doc;

		JsonNode fieldsNode = typeDef.get("fields");
		if (!fieldsNode.isArray()) return doc;

		ObjectNode root = (ObjectNode) doc.getJsonNode();

		for (JsonNode fieldDef : fieldsNode) {
			String fieldName = fieldDef.has("name") ? fieldDef.get("name").asText() : null;
			if (fieldName == null) continue;

			// Skip dynamic fields — they're computed at enrichment time
			boolean isDynamic = fieldDef.has("dynamic") && !fieldDef.get("dynamic").isNull();
			if (isDynamic) continue;

			String fieldTypeName = fieldDef.has("type") ? fieldDef.get("type").asText() : null;
			boolean isVector = fieldDef.has("vector") && fieldDef.get("vector").asBoolean();

			if (isVector) {
				var arr = JsonNodeFactory.instance.arrayNode();
				int count = gen.intBetween(1, 4);
				for (int i = 0; i < count; i++) {
					JsonNode val = generatePrimitiveValue(fieldTypeName, gen);
					if (val != null) arr.add(val);
				}
				root.set(fieldName, arr);
			} else {
				JsonNode val = generateFieldValue(fieldTypeName, gen);
				if (val != null) {
					root.set(fieldName, val);
				}
			}
		}

		return doc;
	}

	private static JsonNode generateFieldValue(String typeName, DataGenerators gen) {
		if (typeName == null) return JsonNodeFactory.instance.textNode(gen.lorem());

		String normalized = typeName.startsWith("core_") ? typeName.substring(5) : typeName;

		return switch (normalized) {
			case "string" -> JsonNodeFactory.instance.textNode(gen.firstName() + " " + gen.lastName());
			case "long" -> JsonNodeFactory.instance.numberNode(gen.longBetween(1, 100000));
			case "int" -> JsonNodeFactory.instance.numberNode(gen.intBetween(1, 10000));
			case "boolean" -> JsonNodeFactory.instance.booleanNode(gen.bool());
			case "date" -> JsonNodeFactory.instance.textNode(gen.date());
			case "double", "float" -> JsonNodeFactory.instance.numberNode(gen.doubleBetween(0, 1000));
			case "mls" -> generateMls(gen);
			case "dates" -> generateDates(gen);
			case "id" -> generateId(gen);
			case "url" -> JsonNodeFactory.instance.textNode("https://example.com/" + gen.uuid());
			default -> generateCompositeStub(normalized, gen);
		};
	}

	private static JsonNode generatePrimitiveValue(String typeName, DataGenerators gen) {
		if (typeName == null) return JsonNodeFactory.instance.textNode(gen.product());

		String normalized = typeName.startsWith("core_") ? typeName.substring(5) : typeName;

		return switch (normalized) {
			case "string" -> JsonNodeFactory.instance.textNode(gen.product());
			case "long" -> JsonNodeFactory.instance.numberNode(gen.longBetween(1, 100000));
			case "int" -> JsonNodeFactory.instance.numberNode(gen.intBetween(1, 10000));
			case "boolean" -> JsonNodeFactory.instance.booleanNode(gen.bool());
			case "date" -> JsonNodeFactory.instance.textNode(gen.date());
			case "double", "float" -> JsonNodeFactory.instance.numberNode(gen.doubleBetween(0, 1000));
			default -> JsonNodeFactory.instance.textNode(gen.lorem());
		};
	}

	private static JsonNode generateMls(DataGenerators gen) {
		var mls = JsonNodeFactory.instance.objectNode();
		var arr = JsonNodeFactory.instance.arrayNode();
		var elem = JsonNodeFactory.instance.objectNode();
		elem.put("text", gen.lorem());
		elem.put("lang", "en");
		arr.add(elem);
		mls.set("mls", arr);
		return mls;
	}

	private static JsonNode generateDates(DataGenerators gen) {
		var dates = JsonNodeFactory.instance.objectNode();
		dates.put("created", gen.date());
		dates.put("modified", gen.date());
		return dates;
	}

	private static JsonNode generateId(DataGenerators gen) {
		var id = JsonNodeFactory.instance.objectNode();
		id.put("domain", gen.company().toLowerCase().replace(" ", "_"));
		id.put("did", gen.uuid());
		return id;
	}

	private static JsonNode generateCompositeStub(String typeName, DataGenerators gen) {
		// For unknown composite types, generate an object with a text field
		var obj = JsonNodeFactory.instance.objectNode();
		obj.put("_generated", true);
		obj.put("_type", typeName);
		return obj;
	}
}
