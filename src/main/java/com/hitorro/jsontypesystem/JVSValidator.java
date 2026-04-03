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
import com.hitorro.util.typesystem.TypeFieldDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates a JVS document against a Type definition.
 * Reports missing fields, type mismatches, and unexpected fields.
 */
public class JVSValidator {

	public enum Level { INFO, WARNING, ERROR }

	public record Violation(String path, String message, Level level) {
		@Override
		public String toString() {
			return String.format("[%s] %s: %s", level, path, message);
		}
	}

	/**
	 * Validate a JVS document against a Type object.
	 */
	public static List<Violation> validate(JVS doc, Type type) {
		return validateAgainstDefinition(doc, type.getMetaNode());
	}

	/**
	 * Validate a JVS document against a raw type definition JsonNode.
	 * Does not require Type.init() or JsonTypeSystem.
	 */
	public static List<Violation> validateAgainstDefinition(JVS doc, JsonNode typeDef) {
		var violations = new ArrayList<Violation>();
		JsonNode root = doc.getJsonNode();
		validateFields(root, typeDef, "", violations);
		String typeName = typeDef.has("name") ? typeDef.get("name").asText() : "unknown";
		checkExtraFields(root, typeDef, "", typeName, violations);
		return violations;
	}

	public static String report(JVS doc, Type type) {
		return reportAgainstDefinition(doc, type.getMetaNode());
	}

	public static String reportAgainstDefinition(JVS doc, JsonNode typeDef) {
		var violations = validateAgainstDefinition(doc, typeDef);
		String typeName = typeDef.has("name") ? typeDef.get("name").asText() : "unknown";
		if (violations.isEmpty()) {
			return "OK — document is valid for type: " + typeName;
		}
		var sb = new StringBuilder();
		sb.append(violations.size()).append(" violation(s) for type ").append(typeName).append(":\n");
		for (Violation v : violations) {
			sb.append("  ").append(v).append("\n");
		}
		return sb.toString();
	}

	private static void validateFields(JsonNode node, JsonNode typeDef, String prefix, List<Violation> violations) {
		if (typeDef == null || !typeDef.has("fields")) return;

		JsonNode fieldsNode = typeDef.get("fields");
		if (!fieldsNode.isArray()) return;

		for (JsonNode fieldDef : fieldsNode) {
			String fieldName = fieldDef.has("name") ? fieldDef.get("name").asText() : null;
			if (fieldName == null) continue;

			String path = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
			boolean isDynamic = fieldDef.has("dynamic") && !fieldDef.get("dynamic").isNull();
			boolean isVector = fieldDef.has("vector") && fieldDef.get("vector").asBoolean();

			// Skip dynamic fields — they're computed, not stored
			if (isDynamic) continue;

			JsonNode value = node.get(fieldName);

			// Missing field
			if (value == null || value.isNull()) {
				violations.add(new Violation(path, "missing field", Level.WARNING));
				continue;
			}

			// Vector check
			if (isVector && !value.isArray()) {
				violations.add(new Violation(path, "expected array (vector) but got " + value.getNodeType(), Level.ERROR));
				continue;
			}
			if (!isVector && value.isArray()) {
				violations.add(new Violation(path, "expected scalar but got array", Level.ERROR));
				continue;
			}

			// Primitive type check
			String fieldTypeName = fieldDef.has("type") ? fieldDef.get("type").asText() : null;
			if (fieldTypeName != null && !isVector) {
				checkPrimitiveType(path, value, fieldTypeName, violations);
			}
		}
	}

	private static void checkPrimitiveType(String path, JsonNode value, String typeName, List<Violation> violations) {
		// Normalize type name
		String normalized = typeName.startsWith("core_") ? typeName.substring(5) : typeName;

		switch (normalized) {
			case "string" -> {
				if (!value.isTextual() && !value.isObject()) {
					violations.add(new Violation(path, "expected string but got " + value.getNodeType(), Level.ERROR));
				}
			}
			case "long", "int" -> {
				if (!value.isNumber()) {
					violations.add(new Violation(path, "expected number but got " + value.getNodeType(), Level.ERROR));
				}
			}
			case "boolean" -> {
				if (!value.isBoolean()) {
					violations.add(new Violation(path, "expected boolean but got " + value.getNodeType(), Level.ERROR));
				}
			}
			case "date" -> {
				if (!value.isTextual()) {
					violations.add(new Violation(path, "expected date string but got " + value.getNodeType(), Level.ERROR));
				}
			}
			// Composite types (id, mls, dates, etc.) are objects — no primitive check needed
		}
	}

	private static void checkExtraFields(JsonNode node, JsonNode typeDef, String prefix, String typeName, List<Violation> violations) {
		if (!node.isObject()) return;

		var knownFields = getKnownFieldNames(typeDef);
		// Also allow common metadata fields
		knownFields.add("type");

		Iterator<String> fieldNames = node.fieldNames();
		while (fieldNames.hasNext()) {
			String name = fieldNames.next();
			if (!knownFields.contains(name)) {
				String path = prefix.isEmpty() ? name : prefix + "." + name;
				violations.add(new Violation(path, "field not defined in type '" + typeName + "'", Level.INFO));
			}
		}
	}

	private static Set<String> getKnownFieldNames(JsonNode typeDef) {
		var names = new HashSet<String>();
		if (typeDef != null && typeDef.has("fields") && typeDef.get("fields").isArray()) {
			for (JsonNode fieldDef : typeDef.get("fields")) {
				if (fieldDef.has("name")) {
					names.add(fieldDef.get("name").asText());
				}
			}
		}
		return names;
	}
}
