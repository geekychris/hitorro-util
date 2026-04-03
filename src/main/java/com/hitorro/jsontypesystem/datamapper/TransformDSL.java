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
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import groovy.lang.Closure;
import groovy.lang.Script;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

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

	public GeneratorRegistry getRegistry() {
		return ctx.gen.getRegistry();
	}

	public JVS getSource() {
		return ctx.source;
	}

	public JVS getTarget() {
		return ctx.target;
	}

	// --- Script library imports ---

	/**
	 * Load a library script from the transforms/lib/ directory.
	 * The library script should return a Map of name→closure.
	 * Usage: def lib = load("common")  // loads transforms/lib/common.groovy
	 *        set "target.slug", lib.slugify("Hello World")
	 */
	public Object load(String libName) {
		try {
			File transformsDir = ctx.gen.getRegistry().getGeneratorsDir();
			if (transformsDir == null) {
				throw new RuntimeException("Generators directory not set — cannot resolve lib path");
			}
			// generators dir is config/generators, transforms/lib is at config/transforms/lib
			File libDir = new File(transformsDir.getParentFile(), "transforms/lib");
			File libFile = new File(libDir, libName + ".groovy");
			if (!libFile.exists()) {
				throw new RuntimeException("Library not found: " + libFile.getAbsolutePath());
			}
			groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell();
			return shell.evaluate(libFile);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load library '" + libName + "': " + e.getMessage(), e);
		}
	}

	// --- JVS type system operations ---

	/**
	 * Enrich the target document using JVS type system enrichment.
	 * Triggers dynamic field computation for all fields with enrich groups.
	 */
	public void enrich(String... tags) {
		if (ctx.enrichOps == null) {
			throw new RuntimeException("Enrich operations not available — requires runtime wiring");
		}
		try {
			JVS enriched = ctx.enrichOps.enrich(ctx.target, tags);
			if (enriched != null) {
				copyJsonInto(ctx.target, enriched);
			}
		} catch (Exception e) {
			throw new RuntimeException("Enrichment failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Get a dynamic field value from the source or target.
	 * Dynamic fields are computed on access via the PAContextTyped.
	 */
	public Object dynamic(String path) {
		try {
			return ctx.target.get(path);
		} catch (PropaccessError e) {
			return null;
		}
	}

	// --- AI / LLM operations ---

	/**
	 * Translate text using the configured AI service.
	 * translate "source.title.mls[0].text", from: "en", to: "de", into: "target.title_de"
	 */
	public void translate(Map<String, Object> params, String sourcePath) {
		translateImpl(sourcePath, params);
	}

	public void translate(String sourcePath, Map<String, Object> params) {
		translateImpl(sourcePath, params);
	}

	private void translateImpl(String sourcePath, Map<String, Object> params) {
		AIOperations ai = ctx.ai;
		if (ai == null || !ai.isAvailable()) {
			throw new RuntimeException("AI service not available for translate operation");
		}
		String text = null;
		try { text = ctx.getString(sourcePath); } catch (PropaccessError e) { /* ignore */ }
		if (text == null || text.isEmpty()) return;

		String fromLang = params.containsKey("from") ? params.get("from").toString() : "en";
		String toLang = params.containsKey("to") ? params.get("to").toString() : "de";
		String intoPath = params.containsKey("into") ? params.get("into").toString() : null;

		String translated = ai.translate(text, fromLang, toLang);
		if (translated == null || translated.isEmpty()) return;
		// Trim LLM artifacts (leading newlines, etc.)
		translated = translated.strip();

		if (intoPath != null) {
			// If "into" points to an MLS array (ends with .mls), append as MLS element
			if (intoPath.endsWith(".mls")) {
				String basePath = intoPath.substring(0, intoPath.length() - 4); // strip ".mls"
				if (basePath.startsWith("target.")) basePath = basePath.substring(7);
				mlsAppendImpl(basePath, Map.of("text", translated, "lang", toLang));
			} else {
				set(intoPath, translated);
			}
		} else {
			// Default: append as MLS element to the source field's MLS array
			String basePath = sourcePath.contains(".mls[") ?
					sourcePath.substring(0, sourcePath.indexOf(".mls[")) : sourcePath;
			if (basePath.startsWith("source.")) basePath = basePath.substring(7);
			mlsAppendImpl(basePath, Map.of("text", translated, "lang", toLang));
		}
	}

	/**
	 * Translate and set an MLS field to multiple languages.
	 * translateMls "target.title", from: "en", to: ["de", "fr", "ja"]
	 */
	public void translateMls(Map<String, Object> params, String fieldPath) {
		translateMlsImpl(fieldPath, params);
	}

	public void translateMls(String fieldPath, Map<String, Object> params) {
		translateMlsImpl(fieldPath, params);
	}

	private void translateMlsImpl(String fieldPath, Map<String, Object> params) {
		AIOperations ai = ctx.ai;
		if (ai == null || !ai.isAvailable()) {
			throw new RuntimeException("AI service not available for translateMls operation");
		}
		String fromLang = params.containsKey("from") ? params.get("from").toString() : "en";
		String actualPath = fieldPath.startsWith("target.") ? fieldPath.substring(7) : fieldPath;

		// Read source text
		String sourceText = null;
		try { sourceText = ctx.target.getString(actualPath + ".mls[" + fromLang + "].text"); }
		catch (PropaccessError e) { /* ignore */ }
		if (sourceText == null || sourceText.isEmpty()) return;

		// Get target languages
		Object toParam = params.get("to");
		java.util.List<String> targetLangs;
		if (toParam instanceof java.util.List) {
			targetLangs = ((java.util.List<?>) toParam).stream().map(Object::toString)
					.collect(Collectors.toList());
		} else {
			targetLangs = java.util.List.of(toParam.toString());
		}

		for (String lang : targetLangs) {
			if (lang.equals(fromLang)) continue;
			String translated = ai.translate(sourceText, fromLang, lang);
			try {
				ctx.target.set(actualPath + ".mls[" + lang + "].text", translated);
			} catch (PropaccessError e) {
				// Fallback: append
				mlsAppendImpl(actualPath, Map.of("text", translated, "lang", lang));
			}
		}
	}

	/**
	 * Summarize text using the configured AI service.
	 * summarize "source.body.mls[0].text", maxWords: 50, into: "target.summary"
	 */
	public void summarize(Map<String, Object> params, String sourcePath) {
		summarizeImpl(sourcePath, params);
	}

	public void summarize(String sourcePath, Map<String, Object> params) {
		summarizeImpl(sourcePath, params);
	}

	private void summarizeImpl(String sourcePath, Map<String, Object> params) {
		AIOperations ai = ctx.ai;
		if (ai == null || !ai.isAvailable()) {
			throw new RuntimeException("AI service not available for summarize operation");
		}
		String text = null;
		try { text = ctx.getString(sourcePath); } catch (PropaccessError e) { /* ignore */ }
		if (text == null || text.isEmpty()) return;

		int maxWords = params.containsKey("maxWords") ? toInt(params.get("maxWords")) : 50;
		String intoPath = params.containsKey("into") ? params.get("into").toString() : null;

		String summary = ai.summarize(text, maxWords);
		if (summary != null) summary = summary.strip();
		if (intoPath != null && summary != null) {
			set(intoPath, summary);
		}
	}

	/**
	 * Ask a question about text using the configured AI service.
	 * ask "source.body.mls[0].text", question: "What is the main topic?", into: "target.topic"
	 */
	public void ask(Map<String, Object> params, String sourcePath) {
		askImpl(sourcePath, params);
	}

	public void ask(String sourcePath, Map<String, Object> params) {
		askImpl(sourcePath, params);
	}

	private void askImpl(String sourcePath, Map<String, Object> params) {
		AIOperations ai = ctx.ai;
		if (ai == null || !ai.isAvailable()) {
			throw new RuntimeException("AI service not available for ask operation");
		}
		String text = null;
		try { text = ctx.getString(sourcePath); } catch (PropaccessError e) { /* ignore */ }
		if (text == null || text.isEmpty()) return;

		String question = params.get("question").toString();
		String intoPath = params.containsKey("into") ? params.get("into").toString() : null;

		String answer = ai.ask(text, question);
		if (answer != null) answer = answer.strip();
		if (intoPath != null && answer != null) {
			set(intoPath, answer);
		}
	}

	/**
	 * Check if AI operations are available.
	 */
	public boolean aiAvailable() {
		return ctx.ai != null && ctx.ai.isAvailable();
	}

	private void copyJsonInto(JVS target, JVS source) {
		com.fasterxml.jackson.databind.node.ObjectNode targetNode = (com.fasterxml.jackson.databind.node.ObjectNode) target.getJsonNode();
		com.fasterxml.jackson.databind.node.ObjectNode sourceNode = (com.fasterxml.jackson.databind.node.ObjectNode) source.getJsonNode();
		java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = sourceNode.fields();
		while (fields.hasNext()) {
			java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = fields.next();
			targetNode.set(entry.getKey(), entry.getValue());
		}
	}

	// --- Generator definition DSL ---

	/**
	 * Define a generator from a CSV file: generator "colors", file: "colors.csv"
	 * Define a random int: generator "age", type: "int", min: 18, max: 65
	 * Define a sequence: generator "seq", type: "sequence", start: 1000
	 * Define a pattern: generator "sku", type: "pattern", pattern: "SKU-####-??"
	 * Define a pick: generator "status", type: "pick", values: ["active","inactive"]
	 * Define from items: generator "tier", type: "items", values: ["gold","silver","bronze"]
	 * Define a random date: generator "dob", type: "date", from: "1960-01-01", to: "2005-12-31"
	 * Define a uuid: generator "txn_id", type: "uuid"
	 * Define a constant: generator "version", type: "constant", value: "2.0"
	 * Define a composite: generator "greeting", type: "template", template: "Dear {first_names} {last_names}"
	 */
	public void generator(Map<String, Object> params, String name) {
		generatorImpl(name, params);
	}

	public void generator(String name, Map<String, Object> params) {
		generatorImpl(name, params);
	}

	private void generatorImpl(String name, Map<String, Object> params) {
		GeneratorRegistry reg = ctx.gen.getRegistry();
		// Use force flag to allow explicit overrides: generator "name", type: "...", force: true
		boolean force = params.containsKey("force") && Boolean.TRUE.equals(params.get("force"));
		// Skip if already registered (generators persist across calls) unless forced
		if (!force && reg.has(name)) return;

		String type = params.containsKey("type") ? params.get("type").toString() : null;

		Generator g;

		if (params.containsKey("file")) {
			// CSV file generator
			String filename = params.get("file").toString();
			File dir = reg.getGeneratorsDir();
			File f = dir != null ? new File(dir, filename) : new File(filename);
			try {
				g = Generators.csv(f);
			} catch (Exception e) {
				throw new RuntimeException("Failed to load CSV generator " + filename, e);
			}
		} else if ("int".equals(type)) {
			int min = toInt(params.getOrDefault("min", 0));
			int max = toInt(params.getOrDefault("max", Integer.MAX_VALUE));
			g = Generators.randomInt(min, max);
		} else if ("long".equals(type)) {
			long min = toLong(params.getOrDefault("min", 0L));
			long max = toLong(params.getOrDefault("max", Long.MAX_VALUE));
			g = Generators.randomLong(min, max);
		} else if ("double".equals(type)) {
			double min = toDouble(params.getOrDefault("min", 0.0));
			double max = toDouble(params.getOrDefault("max", 1.0));
			g = Generators.randomDouble(min, max);
		} else if ("bool".equals(type) || "boolean".equals(type)) {
			g = Generators.randomBool();
		} else if ("uuid".equals(type)) {
			g = Generators.uuid();
		} else if ("date".equals(type)) {
			String from = params.containsKey("from") ? params.get("from").toString() : null;
			String to = params.containsKey("to") ? params.get("to").toString() : null;
			g = (from != null && to != null) ? Generators.dateInRange(from, to) : Generators.date();
		} else if ("timestamp".equals(type)) {
			g = Generators.timestamp();
		} else if ("sequence".equals(type) || "seq".equals(type)) {
			if (params.containsKey("prefix")) {
				g = Generators.sequence(params.get("prefix").toString());
			} else {
				int start = toInt(params.getOrDefault("start", 1));
				g = Generators.sequence(start);
			}
		} else if ("pattern".equals(type)) {
			g = Generators.pattern(params.get("pattern").toString());
		} else if ("pick".equals(type)) {
			java.util.List<?> values = (java.util.List<?>) params.get("values");
			String[] arr = values.stream().map(Object::toString).toArray(String[]::new);
			g = Generators.pick(arr);
		} else if ("items".equals(type)) {
			java.util.List<?> values = (java.util.List<?>) params.get("values");
			java.util.List<String> items = values.stream().map(Object::toString).collect(java.util.stream.Collectors.toList());
			g = Generators.items(items);
		} else if ("constant".equals(type)) {
			g = Generators.constant(params.get("value"));
		} else if ("template".equals(type)) {
			g = Generators.template(params.get("template").toString(), reg);
		} else {
			throw new RuntimeException("Unknown generator type: " + type + " for generator: " + name);
		}

		reg.register(name, g);
	}

	private static int toInt(Object v) {
		if (v instanceof Number) return ((Number) v).intValue();
		return Integer.parseInt(v.toString());
	}

	private static long toLong(Object v) {
		if (v instanceof Number) return ((Number) v).longValue();
		return Long.parseLong(v.toString());
	}

	private static double toDouble(Object v) {
		if (v instanceof Number) return ((Number) v).doubleValue();
		return Double.parseDouble(v.toString());
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

		try {
			String actualPath = path.startsWith("target.") ? path.substring(7) : path;
			// Use JVS addLangTextTemporaryReLook for initial creation (replaces any existing MLS)
			Propaccess pa = new Propaccess(actualPath + ".mls");
			ctx.target.addLangTextTemporaryReLook(pa, text, lang);
		} catch (Exception e) {
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

	void mlsAppendImpl(String path, Map<String, ?> params) {
		String text = params.get("text") != null ? params.get("text").toString() : null;
		String lang = params.containsKey("lang") ? params.get("lang").toString() : "en";
		if (text == null) return;

		try {
			String actualPath = path.startsWith("target.") ? path.substring(7) : path;
			if (!actualPath.endsWith(".mls")) {
				actualPath = actualPath + ".mls";
			}
			// Use JVS addLangText which properly appends to the MLS array
			Propaccess pa = new Propaccess(actualPath);
			ctx.target.addLangText(pa, text, lang);
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
