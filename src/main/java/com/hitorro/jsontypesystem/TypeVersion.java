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

import java.util.List;

/**
 * Semantic versioning for type definitions.
 * Types can include a {@code "version": "1.2.3"} field.
 * Provides version comparison, bump operations, and suggested bump level
 * based on type diff analysis.
 */
public record TypeVersion(String name, String version) implements Comparable<TypeVersion> {

	private static final String DEFAULT_VERSION = "1.0.0";

	public static TypeVersion fromDefinition(JsonNode typeDef) {
		String name = typeDef.has("name") ? typeDef.get("name").asText() : "unknown";
		String version = typeDef.has("version") ? typeDef.get("version").asText() : DEFAULT_VERSION;
		return new TypeVersion(name, version);
	}

	public int major() { return part(0); }
	public int minor() { return part(1); }
	public int patch() { return part(2); }

	private int part(int index) {
		String[] parts = version.split("\\.");
		return index < parts.length ? Integer.parseInt(parts[index]) : 0;
	}

	public TypeVersion bumpMajor() {
		return new TypeVersion(name, (major() + 1) + ".0.0");
	}

	public TypeVersion bumpMinor() {
		return new TypeVersion(name, major() + "." + (minor() + 1) + ".0");
	}

	public TypeVersion bumpPatch() {
		return new TypeVersion(name, major() + "." + minor() + "." + (patch() + 1));
	}

	@Override
	public int compareTo(TypeVersion other) {
		int c = Integer.compare(major(), other.major());
		if (c != 0) return c;
		c = Integer.compare(minor(), other.minor());
		if (c != 0) return c;
		return Integer.compare(patch(), other.patch());
	}

	/**
	 * Suggest what version bump is needed based on changes between two type definitions.
	 * - Removed fields → major (breaking change)
	 * - Modified field type → major (breaking change)
	 * - Added fields → minor (backward compatible addition)
	 * - No changes → "none"
	 */
	public static String suggestedBump(JsonNode oldDef, JsonNode newDef) {
		List<TypeDiff.Change> changes = TypeDiff.diff(oldDef, newDef);
		if (changes.isEmpty()) return "none";

		boolean hasBreaking = changes.stream().anyMatch(c ->
				c.kind() == TypeDiff.ChangeKind.REMOVED ||
						(c.kind() == TypeDiff.ChangeKind.MODIFIED && c.detail() != null && c.detail().contains("type:")));

		return hasBreaking ? "major" : "minor";
	}

	@Override
	public String toString() {
		return name + "@" + version;
	}
}
