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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import groovy.lang.Closure;
import groovy.lang.Script;

import java.util.Iterator;
import java.util.Map;

/**
 * Base class for Groovy DSL transform scripts. Scripts extend this class
 * (configured via CompilerConfiguration) and get all DSL methods available.
 *
 * <h3>DSL operations</h3>
 * <pre>
 * copy "source.title" to "target.title"
 * copyAll()
 * delete "target.body.mls[0].clean"
 * set "target.status", "published"
 * mls "target.title", text: gen.fullName(), lang: "en"
 *
 * when(source("type") == "article") {
 *     copy "source.author" to "target.writer"
 * }
 *
 * loop("source.tags[]") { tag ->
 *     append "target.categories", tag
 * }
 * </pre>
 */
public abstract class TransformDSL extends Script {

	private MappingContext ctx;

	public void setContext(MappingContext ctx) {
		this.ctx = ctx;
	}

	// --- Accessors ---

	public DataGenerators getGen() {
		return ctx.gen;
	}

	public JVS getSource() {
		return ctx.source;
	}

	public JVS getTarget() {
		return ctx.target;
	}

	public JVS getWork() {
		return ctx.work;
	}

	public int getDocIndex() {
		return ctx.docIndex;
	}

	// --- Path read helpers ---

	public JsonNode source(String path) {
		try {
			return ctx.source.get(path);
		} catch (PropaccessError e) {
			return null;
		}
	}

	public String sourceString(String path) {
		try {
			return ctx.source.getString(path);
		} catch (PropaccessError e) {
			return null;
		}
	}

	public JsonNode target(String path) {
		try {
			return ctx.target.get(path);
		} catch (PropaccessError e) {
			return null;
		}
	}

	// --- Copy operations ---

	/**
	 * Returns a CopyBuilder for fluent "copy X to Y" syntax.
	 */
	public CopyBuilder copy(String fromPath) {
		return new CopyBuilder(ctx, fromPath);
	}

	/**
	 * Deep-copy all fields from source to target.
	 */
	public void copyAll() {
		ObjectNode sourceNode = (ObjectNode) ctx.source.getJsonNode();
		ObjectNode targetNode = (ObjectNode) ctx.target.getJsonNode();
		Iterator<Map.Entry<String, JsonNode>> fields = sourceNode.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			targetNode.set(entry.getKey(), entry.getValue().deepCopy());
		}
	}

	// --- Set / Delete ---

	public void set(String path, Object value) {
		try {
			// Groovy GString → String conversion
			if (value != null && !(value instanceof JsonNode) && !(value instanceof String)
					&& !(value instanceof Number) && !(value instanceof Boolean)) {
				value = value.toString();
			}
			ctx.set(path, value);
		} catch (PropaccessError e) {
			throw new RuntimeException("Failed to set " + path, e);
		}
	}

	public void delete(String path) {
		try {
			if (path.startsWith("target.")) {
				ctx.target.remove(path.substring(7));
			} else {
				ctx.target.remove(path);
			}
		} catch (PropaccessError e) {
			throw new RuntimeException("Failed to delete " + path, e);
		}
	}

	// --- MLS helpers ---

	/**
	 * Create an MLS entry: mls "target.title", text: "Hello", lang: "en"
	 * Groovy collects named params into a Map as the first argument.
	 */
	public void mls(Map<String, String> params, String path) {
		mlsImpl(path, params);
	}

	public void mls(String path, Map<String, String> params) {
		mlsImpl(path, params);
	}

	private void mlsImpl(String path, Map<String, ?> params) {
		String text = params.get("text") != null ? params.get("text").toString() : null;
		String lang = params.containsKey("lang") ? params.get("lang").toString() : "en";
		if (text == null) return;

		ObjectNode elem = JsonNodeFactory.instance.objectNode();
		elem.put("text", text);
		elem.put("lang", lang);

		ArrayNode arr = JsonNodeFactory.instance.arrayNode();
		arr.add(elem);

		ObjectNode wrapper = JsonNodeFactory.instance.objectNode();
		wrapper.set("mls", arr);

		try {
			if (path.startsWith("target.")) {
				ctx.target.set(path.substring(7), wrapper);
			} else {
				ctx.target.set(path, wrapper);
			}
		} catch (PropaccessError e) {
			throw new RuntimeException("Failed to set MLS at " + path, e);
		}
	}

	/**
	 * Append an MLS element to an existing MLS array.
	 */
	public void mlsAppend(Map<String, String> params, String path) {
		mlsAppendImpl(path, params);
	}

	public void mlsAppend(String path, Map<String, String> params) {
		mlsAppendImpl(path, params);
	}

	private void mlsAppendImpl(String path, Map<String, String> params) {
		String text = params.get("text");
		String lang = params.getOrDefault("lang", "en");
		if (text == null) return;

		ObjectNode elem = JsonNodeFactory.instance.objectNode();
		elem.put("text", text);
		elem.put("lang", lang);

		try {
			String actualPath = path.startsWith("target.") ? path.substring(7) : path;
			if (!actualPath.endsWith(".mls")) {
				actualPath = actualPath + ".mls";
			}
			JsonNode existing = ctx.target.get(actualPath);
			if (existing != null && existing.isArray()) {
				((ArrayNode) existing).add(elem);
			} else {
				ArrayNode arr = JsonNodeFactory.instance.arrayNode();
				arr.add(elem);
				ctx.target.set(actualPath, arr);
			}
		} catch (PropaccessError e) {
			throw new RuntimeException("Failed to append MLS at " + path, e);
		}
	}

	// --- Append to array ---

	public void append(String path, Object value) {
		try {
			String actualPath = path.startsWith("target.") ? path.substring(7) : path;
			JsonNode existing = ctx.target.get(actualPath);
			ArrayNode arr;
			if (existing != null && existing.isArray()) {
				arr = (ArrayNode) existing;
			} else {
				arr = JsonNodeFactory.instance.arrayNode();
				ctx.target.set(actualPath, arr);
			}
			if (value instanceof JsonNode) {
				arr.add((JsonNode) value);
			} else if (value instanceof String) {
				arr.add((String) value);
			} else if (value instanceof Number) {
				arr.add(((Number) value).longValue());
			} else {
				arr.add(value.toString());
			}
		} catch (PropaccessError e) {
			throw new RuntimeException("Failed to append to " + path, e);
		}
	}

	// --- Conditional ---

	public void when(boolean condition, Closure<?> body) {
		if (condition) {
			body.setDelegate(this);
			body.setResolveStrategy(Closure.DELEGATE_FIRST);
			body.call();
		}
	}

	// --- Looping ---

	/**
	 * Loop over elements of a source array. The closure receives each element as a JsonNode.
	 */
	public void loop(String sourcePath, Closure<?> body) {
		try {
			String actualPath = sourcePath.startsWith("source.") ? sourcePath.substring(7) : sourcePath;
			// Strip trailing [] if present
			if (actualPath.endsWith("[]")) {
				actualPath = actualPath.substring(0, actualPath.length() - 2);
			}
			JsonNode arr = ctx.source.get(actualPath);
			if (arr != null && arr.isArray()) {
				for (int i = 0; i < arr.size(); i++) {
					body.call(arr.get(i));
				}
			}
		} catch (PropaccessError e) {
			throw new RuntimeException("Failed to loop over " + sourcePath, e);
		}
	}

	/**
	 * Loop N times. Closure receives the index (0-based).
	 */
	public void times(int n, Closure<?> body) {
		for (int i = 0; i < n; i++) {
			body.call(i);
		}
	}

	// --- Fluent copy builder ---

	public static class CopyBuilder {
		private final MappingContext ctx;
		private final String fromPath;

		CopyBuilder(MappingContext ctx, String fromPath) {
			this.ctx = ctx;
			this.fromPath = fromPath;
		}

		public void to(String toPath) {
			try {
				JsonNode val = ctx.get(fromPath);
				if (val != null) {
					ctx.set(toPath, val.deepCopy());
				}
			} catch (PropaccessError e) {
				throw new RuntimeException("Copy failed: " + fromPath + " -> " + toPath, e);
			}
		}
	}
}
