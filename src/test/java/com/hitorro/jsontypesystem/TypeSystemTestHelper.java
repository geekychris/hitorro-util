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

/**
 * Bootstraps a minimal type system for testing by programmatically triggering
 * core type config loading from config/types/ via the JsonTypeSystem cache.
 * This avoids needing the full Spring Boot service initialization.
 *
 * Usage: Call {@link #ensureCoreTypesLoaded()} in a @BeforeAll or @BeforeEach method.
 * Requires that the working directory is hitorro-util (or that config/types/ is
 * accessible relative to the working directory).
 */
public class TypeSystemTestHelper {

	private static final String[] PRIMITIVE_TYPES = {
			"core_string", "core_long", "core_boolean", "core_date"
	};

	private static final String[] COMPOSITE_TYPES = {
			"core_id", "core_mls", "core_mlselem", "core_dates",
			"core_sysobject", "core_url", "core_query", "core_result",
			"core_qanda", "core_port", "core_capability", "core_cme"
	};

	/**
	 * Ensures all core types are loaded into the type system cache.
	 * The JsonTypeSystem uses a HashCache with a Name2JsonMapper that reads
	 * from config/types/. Calling getType triggers lazy loading.
	 * Primitives are loaded first since composites may depend on them.
	 */
	public static void ensureCoreTypesLoaded() {
		JsonTypeSystem ts = JsonTypeSystem.getMe();
		// Load primitives first (no dependencies)
		for (String name : PRIMITIVE_TYPES) {
			ts.getType(name);
		}
		// Then composites
		for (String name : COMPOSITE_TYPES) {
			ts.getType(name);
		}
	}

	/**
	 * Get a Type instance for the given name, triggering loading if needed.
	 */
	public static Type getType(String name) {
		return JsonTypeSystem.getMe().getType(name);
	}
}
