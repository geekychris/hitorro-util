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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.String2JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads HiTorro type definitions from JSON Schema files (.schema.json).
 *
 * Each type is a separate file with $ref pointing to other files by filename.
 * Files are loaded independently — the loader reads each schema, converts it
 * to native type JSON via {@link JsonSchema2TypeConverter}, and returns the results.
 *
 * <h3>File layout</h3>
 * <pre>
 *   config/schemas/
 *     core_string.schema.json      — primitive
 *     core_long.schema.json        — primitive
 *     core_id.schema.json          — composite, refs core_string/core_long
 *     core_sysobject.schema.json   — composite, refs core_id/core_mls/...
 *     demo_article.schema.json     — extends sysobject via allOf
 * </pre>
 *
 * <h3>Usage</h3>
 * <pre>
 *   SchemaFileLoader loader = new SchemaFileLoader();
 *
 *   // Load a single schema file
 *   ObjectNode articleTypeJson = loader.loadOne(new File("config/schemas/demo_article.schema.json"));
 *
 *   // Load all schemas from a directory
 *   Map&lt;String, ObjectNode&gt; allTypes = loader.loadDirectory(new File("config/schemas"));
 * </pre>
 */
public class SchemaFileLoader {

	private static final String2JsonMapper jsonMapper = new String2JsonMapper();
	private final JsonSchema2TypeConverter converter = new JsonSchema2TypeConverter();

	/**
	 * Load a single schema file and convert it to native type JSON.
	 *
	 * @param schemaFile a .schema.json file
	 * @return the native type JSON that Type.init() expects
	 */
	public ObjectNode loadOne(File schemaFile) throws IOException {
		String json = new String(Files.readAllBytes(schemaFile.toPath()));
		JsonNode schema = jsonMapper.apply(json);
		return converter.convert(schema);
	}

	/**
	 * Load a single schema from a JSON string and convert to native type JSON.
	 */
	public ObjectNode loadFromString(String schemaJson) {
		JsonNode schema = jsonMapper.apply(schemaJson);
		return converter.convert(schema);
	}

	/**
	 * Load all .schema.json files from a directory.
	 * Returns a map of config name (e.g. "core_string") to native type JSON.
	 * Files are loaded in alphabetical order — no dependency resolution is needed
	 * since conversion is purely structural (no type system lookups).
	 */
	public Map<String, ObjectNode> loadDirectory(File dir) throws IOException {
		Map<String, ObjectNode> results = new LinkedHashMap<>();
		File[] files = dir.listFiles((d, name) -> name.endsWith(".schema.json"));
		if (files == null) {
			return results;
		}

		for (File f : files) {
			String configName = f.getName().replace(".schema.json", "");
			try {
				ObjectNode typeJson = loadOne(f);
				results.put(configName, typeJson);
			} catch (Exception e) {
				Log.util.error("Failed to load schema file: %s", f.getName());
			}
		}
		return results;
	}
}
