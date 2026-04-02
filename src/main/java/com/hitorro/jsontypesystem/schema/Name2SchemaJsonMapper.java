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
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

/**
 * Mapper that loads type configs from .schema.json files instead of native .json files.
 * Reads a schema file, converts it to native type JSON via {@link JsonSchema2TypeConverter},
 * and returns the result — making it a drop-in replacement for Name2JsonMapper in the
 * jsonTypeConfig HashCache.
 *
 * Used when {@code JsonTypeSystem.useJsonSchema} is enabled.
 */
public class Name2SchemaJsonMapper implements Mapper<String, JsonNode> {
	private final BaseFile directory;
	private final String defaultDomain;
	private final JsonSchema2TypeConverter converter = new JsonSchema2TypeConverter();

	public Name2SchemaJsonMapper(BaseFile directory, String defaultDomain) {
		this.directory = directory;
		this.defaultDomain = defaultDomain;
		Log.util.info("Schema type loader initialized — directory: %s (exists: %s)",
				directory.getAbsolutePath(), directory.exists());
	}

	@Override
	public JsonNode apply(String name) {
		if (StringUtil.nullOrEmptyString(name)) {
			return null;
		}
		int index = name.indexOf("_");
		if (index == -1 && !StringUtil.nullOrEmptyString(defaultDomain)) {
			name = Fmt.S("%s_%s", defaultDomain, name);
		}
		name = name.toLowerCase();

		BaseFile file = directory.getChild("%s.schema.json", name);
		if (file.exists()) {
			try {
				JsonNode schemaNode = file.getJsonNode();
				ObjectNode typeJson = converter.convert(schemaNode);
				Log.util.info("Loaded type from schema: %s (%s)", name, file.getAbsolutePath());
				return typeJson;
			} catch (Exception e) {
				Log.util.error("Failed to load schema for type: %s — %s", name, e.getMessage());
				return null;
			}
		}
		Log.util.debug("Schema file not found: %s", file.getAbsolutePath());
		return null;
	}
}
