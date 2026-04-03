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

import java.util.*;

/**
 * Diffs two type definitions and reports what changed.
 * Can generate a Groovy migration script for the Data Mapper DSL.
 */
public class TypeDiff {

	public enum ChangeKind { ADDED, REMOVED, MODIFIED }

	public record Change(ChangeKind kind, String fieldName, String detail) {
		@Override
		public String toString() {
			return String.format("[%s] %s%s", kind, fieldName, detail != null ? " — " + detail : "");
		}
	}

	/**
	 * Diff two type definitions (raw JsonNode).
	 */
	public static List<Change> diff(JsonNode oldDef, JsonNode newDef) {
		var changes = new ArrayList<Change>();

		var oldFields = indexFields(oldDef);
		var newFields = indexFields(newDef);

		// Removed fields
		for (String name : oldFields.keySet()) {
			if (!newFields.containsKey(name)) {
				changes.add(new Change(ChangeKind.REMOVED, name,
						"type was " + getFieldType(oldFields.get(name))));
			}
		}

		// Added fields
		for (String name : newFields.keySet()) {
			if (!oldFields.containsKey(name)) {
				changes.add(new Change(ChangeKind.ADDED, name,
						"type " + getFieldType(newFields.get(name))));
			}
		}

		// Modified fields
		for (String name : oldFields.keySet()) {
			if (newFields.containsKey(name)) {
				JsonNode oldField = oldFields.get(name);
				JsonNode newField = newFields.get(name);
				String detail = compareField(oldField, newField);
				if (detail != null) {
					changes.add(new Change(ChangeKind.MODIFIED, name, detail));
				}
			}
		}

		return changes;
	}

	/**
	 * Produce a human-readable diff report.
	 */
	public static String report(JsonNode oldDef, JsonNode newDef) {
		var changes = diff(oldDef, newDef);
		if (changes.isEmpty()) {
			return "No changes between type definitions.";
		}
		String oldName = oldDef.has("name") ? oldDef.get("name").asText() : "old";
		String newName = newDef.has("name") ? newDef.get("name").asText() : "new";
		var sb = new StringBuilder();
		sb.append(changes.size()).append(" change(s) between '")
				.append(oldName).append("' and '").append(newName).append("':\n");
		for (Change c : changes) {
			sb.append("  ").append(c).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Generate a Groovy Data Mapper migration script that transforms documents
	 * from the old type to the new type.
	 */
	public static String generateMigrationScript(JsonNode oldDef, JsonNode newDef) {
		var changes = diff(oldDef, newDef);
		String oldName = oldDef.has("name") ? oldDef.get("name").asText() : "old";
		String newName = newDef.has("name") ? newDef.get("name").asText() : "new";

		var sb = new StringBuilder();
		sb.append("// Migration script: ").append(oldName).append(" → ").append(newName).append("\n");
		sb.append("// Generated from TypeDiff\n\n");
		sb.append("copyAll()\n\n");

		// Handle removed fields
		List<Change> removed = changes.stream()
				.filter(c -> c.kind() == ChangeKind.REMOVED).toList();
		if (!removed.isEmpty()) {
			sb.append("// Remove fields no longer in the type\n");
			for (Change c : removed) {
				sb.append("delete \"target.").append(c.fieldName()).append("\"\n");
			}
			sb.append("\n");
		}

		// Handle added fields
		List<Change> added = changes.stream()
				.filter(c -> c.kind() == ChangeKind.ADDED).toList();
		if (!added.isEmpty()) {
			sb.append("// New fields — set defaults or generate values\n");
			var newFields = indexFields(newDef);
			for (Change c : added) {
				JsonNode fieldDef = newFields.get(c.fieldName());
				String type = getFieldType(fieldDef);
				boolean isVector = fieldDef.has("vector") && fieldDef.get("vector").asBoolean();
				boolean isDynamic = fieldDef.has("dynamic") && !fieldDef.get("dynamic").isNull();

				if (isDynamic) {
					sb.append("// ").append(c.fieldName()).append(" is dynamic (computed at enrichment)\n");
				} else if (isVector) {
					sb.append("// set \"target.").append(c.fieldName()).append("\", []  // vector of ").append(type).append("\n");
				} else {
					sb.append(generateDefaultSetter(c.fieldName(), type));
				}
			}
			sb.append("\n");
		}

		// Handle modified fields
		List<Change> modified = changes.stream()
				.filter(c -> c.kind() == ChangeKind.MODIFIED).toList();
		if (!modified.isEmpty()) {
			sb.append("// Modified fields — review and adjust as needed\n");
			for (Change c : modified) {
				sb.append("// ").append(c.fieldName()).append(": ").append(c.detail()).append("\n");
			}
		}

		return sb.toString();
	}

	private static Map<String, JsonNode> indexFields(JsonNode typeDef) {
		var map = new LinkedHashMap<String, JsonNode>();
		if (typeDef.has("fields") && typeDef.get("fields").isArray()) {
			for (JsonNode f : typeDef.get("fields")) {
				if (f.has("name")) {
					map.put(f.get("name").asText(), f);
				}
			}
		}
		return map;
	}

	private static String getFieldType(JsonNode fieldDef) {
		return fieldDef.has("type") ? fieldDef.get("type").asText() : "unknown";
	}

	private static String compareField(JsonNode oldField, JsonNode newField) {
		var diffs = new ArrayList<String>();

		String oldType = getFieldType(oldField);
		String newType = getFieldType(newField);
		if (!oldType.equals(newType)) {
			diffs.add("type: " + oldType + " → " + newType);
		}

		boolean oldVector = oldField.has("vector") && oldField.get("vector").asBoolean();
		boolean newVector = newField.has("vector") && newField.get("vector").asBoolean();
		if (oldVector != newVector) {
			diffs.add("vector: " + oldVector + " → " + newVector);
		}

		boolean oldI18n = oldField.has("i18n") && oldField.get("i18n").asBoolean();
		boolean newI18n = newField.has("i18n") && newField.get("i18n").asBoolean();
		if (oldI18n != newI18n) {
			diffs.add("i18n: " + oldI18n + " → " + newI18n);
		}

		boolean oldDynamic = oldField.has("dynamic") && !oldField.get("dynamic").isNull();
		boolean newDynamic = newField.has("dynamic") && !newField.get("dynamic").isNull();
		if (oldDynamic != newDynamic) {
			diffs.add("dynamic: " + oldDynamic + " → " + newDynamic);
		}

		return diffs.isEmpty() ? null : String.join(", ", diffs);
	}

	private static String generateDefaultSetter(String fieldName, String type) {
		String normalized = type.startsWith("core_") ? type.substring(5) : type;
		return switch (normalized) {
			case "string" -> "set \"target." + fieldName + "\", \"\"\n";
			case "long", "int" -> "set \"target." + fieldName + "\", 0\n";
			case "boolean" -> "set \"target." + fieldName + "\", false\n";
			case "date" -> "set \"target." + fieldName + "\", gen.date()\n";
			default -> "// set \"target." + fieldName + "\", null  // type: " + type + "\n";
		};
	}
}
