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
 * Extracts and analyzes the dependency graph between dynamic fields in a type definition.
 * Dynamic fields declare their input dependencies via the {@code "fields"} array
 * (e.g., {@code [".text", ".lang"]}).
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>Direct dependency lookup</li>
 *   <li>Topological sort (correct enrichment order)</li>
 *   <li>Impact analysis (what downstream fields are affected by a change)</li>
 *   <li>Cycle detection</li>
 *   <li>Text and Mermaid visualization</li>
 * </ul>
 */
public class FieldDependencyGraph {

	// field name → set of field names it depends on
	private final Map<String, Set<String>> dependencies = new LinkedHashMap<>();
	// field name → set of field names that depend on it (reverse edges)
	private final Map<String, Set<String>> dependents = new LinkedHashMap<>();
	private final String typeName;

	private FieldDependencyGraph(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * Build a dependency graph from a raw type definition JsonNode.
	 */
	public static FieldDependencyGraph fromDefinition(JsonNode typeDef) {
		String name = typeDef.has("name") ? typeDef.get("name").asText() : "unknown";
		var graph = new FieldDependencyGraph(name);

		if (!typeDef.has("fields") || !typeDef.get("fields").isArray()) {
			return graph;
		}

		// First pass: register all field names
		for (JsonNode fieldDef : typeDef.get("fields")) {
			String fieldName = fieldDef.has("name") ? fieldDef.get("name").asText() : null;
			if (fieldName != null) {
				graph.dependencies.putIfAbsent(fieldName, new LinkedHashSet<>());
				graph.dependents.putIfAbsent(fieldName, new LinkedHashSet<>());
			}
		}

		// Second pass: extract dependency edges from dynamic field configs
		for (JsonNode fieldDef : typeDef.get("fields")) {
			String fieldName = fieldDef.has("name") ? fieldDef.get("name").asText() : null;
			if (fieldName == null) continue;

			if (fieldDef.has("dynamic") && !fieldDef.get("dynamic").isNull()) {
				JsonNode dynamic = fieldDef.get("dynamic");
				if (dynamic.has("fields") && dynamic.get("fields").isArray()) {
					for (JsonNode dep : dynamic.get("fields")) {
						String depPath = dep.asText();
						// Normalize: ".text" -> "text", ".lang" -> "lang"
						if (depPath.startsWith(".")) {
							depPath = depPath.substring(1);
						}
						graph.dependencies.get(fieldName).add(depPath);
						graph.dependents.computeIfAbsent(depPath, k -> new LinkedHashSet<>()).add(fieldName);
					}
				}
			}
		}

		return graph;
	}

	/**
	 * Build from a Type object.
	 */
	public static FieldDependencyGraph fromType(Type type) {
		return fromDefinition(type.getMetaNode());
	}

	/**
	 * Get the direct dependencies of a field (what it reads from).
	 */
	public Set<String> getDependencies(String fieldName) {
		return dependencies.getOrDefault(fieldName, Set.of());
	}

	/**
	 * Get the direct dependents of a field (what reads from it).
	 */
	public Set<String> getDependents(String fieldName) {
		return dependents.getOrDefault(fieldName, Set.of());
	}

	/**
	 * Get all transitively impacted fields if the given field changes.
	 * Follows the dependents graph recursively.
	 */
	public Set<String> getImpacted(String fieldName) {
		var impacted = new LinkedHashSet<String>();
		var queue = new ArrayDeque<String>();
		queue.add(fieldName);

		while (!queue.isEmpty()) {
			String current = queue.poll();
			for (String dep : getDependents(current)) {
				if (impacted.add(dep)) {
					queue.add(dep);
				}
			}
		}
		return impacted;
	}

	/**
	 * Topological sort — returns field names in dependency order.
	 * Fields with no dependencies come first. Throws if there's a cycle.
	 */
	public List<String> topologicalSort() {
		var inDegree = new HashMap<String, Integer>();
		for (String field : dependencies.keySet()) {
			inDegree.put(field, 0);
		}
		for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
			for (String dep : entry.getValue()) {
				// dep might be external (e.g., "lang" not in this type's fields)
				inDegree.putIfAbsent(dep, 0);
			}
		}
		for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
			inDegree.merge(entry.getKey(), 0, Integer::sum);
			for (String dep : entry.getValue()) {
				// Increment in-degree of the dependent field
			}
		}
		// Recompute properly
		inDegree.replaceAll((k, v) -> 0);
		for (Map.Entry<String, Set<String>> entry : dependents.entrySet()) {
			for (String dependent : entry.getValue()) {
				inDegree.merge(dependent, 1, Integer::sum);
			}
		}

		var queue = new ArrayDeque<String>();
		for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
			if (entry.getValue() == 0) {
				queue.add(entry.getKey());
			}
		}

		var sorted = new ArrayList<String>();
		while (!queue.isEmpty()) {
			String current = queue.poll();
			sorted.add(current);
			for (String dependent : getDependents(current)) {
				int newDegree = inDegree.merge(dependent, -1, Integer::sum);
				if (newDegree == 0) {
					queue.add(dependent);
				}
			}
		}

		return sorted;
	}

	/**
	 * Check if the dependency graph has a cycle.
	 */
	public boolean hasCycle() {
		return topologicalSort().size() < dependencies.size();
	}

	/**
	 * Get all field names in the graph.
	 */
	public Set<String> getFields() {
		return Collections.unmodifiableSet(dependencies.keySet());
	}

	/**
	 * Readable text dump of the dependency graph.
	 */
	public String dump() {
		var sb = new StringBuilder();
		sb.append("Dependency graph for type: ").append(typeName).append("\n");
		for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
			String field = entry.getKey();
			Set<String> deps = entry.getValue();
			if (deps.isEmpty()) {
				sb.append("  ").append(field).append(" (source)\n");
			} else {
				sb.append("  ").append(String.join(", ", deps))
						.append(" → ").append(field).append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Generate a Mermaid diagram of the dependency graph.
	 */
	public String toMermaid() {
		var sb = new StringBuilder();
		sb.append("graph TD\n");
		for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
			String field = entry.getKey();
			for (String dep : entry.getValue()) {
				sb.append("    ").append(dep).append(" --> ").append(field).append("\n");
			}
		}
		// Add nodes without edges
		for (String field : dependencies.keySet()) {
			if (dependencies.get(field).isEmpty() && getDependents(field).isEmpty()) {
				sb.append("    ").append(field).append("\n");
			}
		}
		return sb.toString();
	}
}
