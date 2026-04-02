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
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deep structural comparison of two native type JSON configs.
 * Compares all type-system-meaningful properties: name, super, primitivetype,
 * fields (name, type, vector, i18n, dynamic, groups), indexseeker, fetchlang.
 *
 * Returns a list of difference descriptions. An empty list means the configs
 * are structurally equivalent for the type system's purposes.
 */
public class TypeConfigIntegrityChecker {

	/**
	 * Compare two native type JSON configs and return all differences.
	 *
	 * @param expected the "known good" config (e.g. from native JSON files)
	 * @param actual   the config to verify (e.g. from schema conversion)
	 * @return list of differences; empty if structurally equivalent
	 */
	public List<String> compare(JsonNode expected, JsonNode actual) {
		List<String> diffs = new ArrayList<>();
		compareAt("", expected, actual, diffs);
		return diffs;
	}

	/**
	 * Compare and return a formatted report string.
	 */
	public String report(JsonNode expected, JsonNode actual) {
		List<String> diffs = compare(expected, actual);
		if (diffs.isEmpty()) {
			return "OK — configs are structurally equivalent";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(diffs.size()).append(" difference(s):\n");
		for (String d : diffs) {
			sb.append("  - ").append(d).append("\n");
		}
		return sb.toString();
	}

	private void compareAt(String path, JsonNode expected, JsonNode actual, List<String> diffs) {
		if (expected == null && actual == null) {
			return;
		}
		if (expected == null) {
			diffs.add(path + ": expected null, got " + summarize(actual));
			return;
		}
		if (actual == null) {
			diffs.add(path + ": expected " + summarize(expected) + ", got null");
			return;
		}

		// Root-level scalar properties
		if (path.isEmpty()) {
			compareRootProperties(expected, actual, diffs);
			compareFields(expected, actual, diffs);
			return;
		}

		// Generic node comparison
		if (expected.isObject() && actual.isObject()) {
			compareObjects(path, (ObjectNode) expected, (ObjectNode) actual, diffs);
		} else if (expected.isArray() && actual.isArray()) {
			compareArrays(path, (ArrayNode) expected, (ArrayNode) actual, diffs);
		} else if (!nodesEqual(expected, actual)) {
			diffs.add(path + ": " + summarize(expected) + " != " + summarize(actual));
		}
	}

	private void compareRootProperties(JsonNode expected, JsonNode actual, List<String> diffs) {
		compareScalar("name", expected, actual, diffs);
		compareScalar("primitivetype", expected, actual, diffs);
		compareScalar("super", expected, actual, diffs);

		// indexseeker
		compareNode("indexseeker", expected.get("indexseeker"), actual.get("indexseeker"), diffs);

		// fetchlang
		boolean expFetch = expected.has("fetchlang") && expected.get("fetchlang").asBoolean();
		boolean actFetch = actual.has("fetchlang") && actual.get("fetchlang").asBoolean();
		if (expFetch != actFetch) {
			diffs.add("fetchlang: " + expFetch + " != " + actFetch);
		}
	}

	private void compareFields(JsonNode expected, JsonNode actual, List<String> diffs) {
		JsonNode expFields = expected.get("fields");
		JsonNode actFields = actual.get("fields");

		if (expFields == null && actFields == null) {
			return;
		}
		if (expFields == null) {
			if (actFields != null && actFields.size() > 0) {
				diffs.add("fields: expected none, got " + actFields.size());
			}
			return;
		}
		if (actFields == null) {
			diffs.add("fields: expected " + expFields.size() + ", got none");
			return;
		}

		// Index both field arrays by name for order-independent comparison
		Map<String, JsonNode> expByName = indexFieldsByName(expFields);
		Map<String, JsonNode> actByName = indexFieldsByName(actFields);

		// Check for missing/extra fields
		for (String name : expByName.keySet()) {
			if (!actByName.containsKey(name)) {
				diffs.add("fields." + name + ": missing in actual");
			}
		}
		for (String name : actByName.keySet()) {
			if (!expByName.containsKey(name)) {
				diffs.add("fields." + name + ": unexpected in actual");
			}
		}

		// Deep-compare matching fields
		for (Map.Entry<String, JsonNode> entry : expByName.entrySet()) {
			String name = entry.getKey();
			JsonNode expField = entry.getValue();
			JsonNode actField = actByName.get(name);
			if (actField == null) {
				continue; // already reported
			}
			compareField("fields." + name, expField, actField, diffs);
		}
	}

	private void compareField(String path, JsonNode expected, JsonNode actual, List<String> diffs) {
		// type — normalize names since native configs mix "mlselem" and "core_mlselem"
		String expType = normalizeTypeName(expected.has("type") ? expected.get("type").asText() : null);
		String actType = normalizeTypeName(actual.has("type") ? actual.get("type").asText() : null);
		if (expType != null && !expType.equals(actType)) {
			diffs.add(path + ".type: " + expType + " != " + actType);
		}

		// vector
		boolean expVec = expected.has("vector") && expected.get("vector").asBoolean();
		boolean actVec = actual.has("vector") && actual.get("vector").asBoolean();
		if (expVec != actVec) {
			diffs.add(path + ".vector: " + expVec + " != " + actVec);
		}

		// i18n
		boolean expI18n = expected.has("i18n") && expected.get("i18n").asBoolean();
		boolean actI18n = actual.has("i18n") && actual.get("i18n").asBoolean();
		if (expI18n != actI18n) {
			diffs.add(path + ".i18n: " + expI18n + " != " + actI18n);
		}

		// dynamic — compare the full object tree
		compareNode(path + ".dynamic", expected.get("dynamic"), actual.get("dynamic"), diffs);

		// groups — treat empty array as equivalent to null/missing
		compareGroups(path + ".groups", expected.get("groups"), actual.get("groups"), diffs);
	}

	/**
	 * Normalize a type name by stripping common prefixes so that
	 * "core_string" and "string", "core_mlselem" and "mlselem" compare equal.
	 */
	private String normalizeTypeName(String name) {
		if (name == null) return null;
		if (name.startsWith("core_")) return name.substring(5);
		if (name.startsWith("demo_")) return name.substring(5);
		return name;
	}

	/**
	 * Compare groups arrays, treating empty array as equivalent to null.
	 */
	private void compareGroups(String path, JsonNode expected, JsonNode actual, List<String> diffs) {
		boolean expEmpty = expected == null || expected.isNull() || (expected.isArray() && expected.size() == 0);
		boolean actEmpty = actual == null || actual.isNull() || (actual.isArray() && actual.size() == 0);
		if (expEmpty && actEmpty) return;
		if (expEmpty != actEmpty) {
			diffs.add(path + ": " + summarize(expected) + " != " + summarize(actual));
			return;
		}
		compareNode(path, expected, actual, diffs);
	}

	private void compareScalar(String key, JsonNode expected, JsonNode actual, List<String> diffs) {
		compareScalar(key, expected, actual, key, diffs);
	}

	private void compareScalar(String path, JsonNode expected, JsonNode actual, String key, List<String> diffs) {
		String expVal = expected.has(key) ? expected.get(key).asText() : null;
		String actVal = actual.has(key) ? actual.get(key).asText() : null;

		// Treat "null" text as null
		if ("null".equals(expVal)) expVal = null;
		if ("null".equals(actVal)) actVal = null;

		if (expVal == null && actVal == null) return;
		if (expVal == null || !expVal.equals(actVal)) {
			diffs.add(path + ": " + expVal + " != " + actVal);
		}
	}

	private void compareNode(String path, JsonNode expected, JsonNode actual, List<String> diffs) {
		if (expected == null && actual == null) return;
		if (expected == null || expected.isNull()) {
			if (actual != null && !actual.isNull()) {
				diffs.add(path + ": expected null, got " + summarize(actual));
			}
			return;
		}
		if (actual == null || actual.isNull()) {
			diffs.add(path + ": expected " + summarize(expected) + ", got null");
			return;
		}
		if (expected.isObject() && actual.isObject()) {
			compareObjects(path, (ObjectNode) expected, (ObjectNode) actual, diffs);
		} else if (expected.isArray() && actual.isArray()) {
			compareArrays(path, (ArrayNode) expected, (ArrayNode) actual, diffs);
		} else if (!nodesEqual(expected, actual)) {
			diffs.add(path + ": " + summarize(expected) + " != " + summarize(actual));
		}
	}

	private void compareObjects(String path, ObjectNode expected, ObjectNode actual, List<String> diffs) {
		Iterator<Map.Entry<String, JsonNode>> iter = expected.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> e = iter.next();
			String childPath = path.isEmpty() ? e.getKey() : path + "." + e.getKey();
			if (!actual.has(e.getKey())) {
				diffs.add(childPath + ": missing in actual");
			} else {
				compareNode(childPath, e.getValue(), actual.get(e.getKey()), diffs);
			}
		}
		Iterator<String> actKeys = actual.fieldNames();
		while (actKeys.hasNext()) {
			String key = actKeys.next();
			if (!expected.has(key)) {
				String childPath = path.isEmpty() ? key : path + "." + key;
				diffs.add(childPath + ": unexpected in actual");
			}
		}
	}

	private void compareArrays(String path, ArrayNode expected, ArrayNode actual, List<String> diffs) {
		if (expected.size() != actual.size()) {
			diffs.add(path + ": array size " + expected.size() + " != " + actual.size());
			return;
		}
		for (int i = 0; i < expected.size(); i++) {
			compareNode(path + "[" + i + "]", expected.get(i), actual.get(i), diffs);
		}
	}

	private boolean nodesEqual(JsonNode a, JsonNode b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return a.equals(b);
	}

	private Map<String, JsonNode> indexFieldsByName(JsonNode fieldsArray) {
		Map<String, JsonNode> map = new LinkedHashMap<>();
		if (fieldsArray != null && fieldsArray.isArray()) {
			for (JsonNode field : fieldsArray) {
				if (field.has("name")) {
					map.put(field.get("name").asText(), field);
				}
			}
		}
		return map;
	}

	private String summarize(JsonNode node) {
		if (node == null) return "null";
		String s = node.toString();
		if (s.length() > 60) {
			return s.substring(0, 57) + "...";
		}
		return s;
	}
}
