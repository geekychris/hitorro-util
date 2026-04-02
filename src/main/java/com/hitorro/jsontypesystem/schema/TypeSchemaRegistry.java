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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JsonTypeSystem;
import com.hitorro.jsontypesystem.Type;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds converted JSON Schema representations of HiTorro types.
 * Lazy-initialized from JsonTypeSystem on first access.
 */
public class TypeSchemaRegistry {

	private final ConcurrentHashMap<String, ObjectNode> schemas = new ConcurrentHashMap<>();
	private final Type2JsonSchemaConverter converter = new Type2JsonSchemaConverter();

	public ObjectNode getSchema(String typeName) {
		return schemas.computeIfAbsent(typeName, name -> {
			Type type = JsonTypeSystem.getMe().getType(name);
			if (type == null) {
				return null;
			}
			return converter.convert(type);
		});
	}

	public void clear() {
		schemas.clear();
	}

	public int size() {
		return schemas.size();
	}
}
