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
package com.hitorro.util.core.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps symbolic implementation names to Java class names.
 *
 * <p>Type configs and JSON Schemas can use symbolic names like {@code "html-scrubber"}
 * instead of FQN class names like {@code "com.hitorro.jsontypesystem.dynamic.mappers.Json2HTMLScrubbedJson"}.
 * The registry resolves symbolic names to class names at instantiation time.</p>
 *
 * <h3>Config file</h3>
 * <p>Defaults are loaded from {@code config/implementations.json}:</p>
 * <pre>
 * {
 *   "html-scrubber": "com.hitorro.jsontypesystem.dynamic.mappers.Json2HTMLScrubbedJson",
 *   "pos-tokenizer": "com.hitorro.jsontypesystem.dynamic.POSTokenizer",
 *   ...
 * }
 * </pre>
 *
 * <h3>Programmatic override</h3>
 * <pre>
 * ImplementationRegistry.getMe().register("html-scrubber", "com.example.MyCustomScrubber");
 * </pre>
 *
 * <h3>Resolution order</h3>
 * <ol>
 *   <li>Programmatic overrides (registered via {@link #register})</li>
 *   <li>Config file entries (from {@code implementations.json})</li>
 *   <li>If no match, the value is returned as-is (assumed to be a FQN class name)</li>
 * </ol>
 */
public class ImplementationRegistry {

	private static final String CONFIG_FILE = "implementations.json";
	private static ImplementationRegistry me;

	private final ConcurrentHashMap<String, String> registry = new ConcurrentHashMap<>();
	private boolean loaded = false;

	public static synchronized ImplementationRegistry getMe() {
		if (me == null) {
			me = new ImplementationRegistry();
		}
		return me;
	}

	/**
	 * Register a symbolic name to a class name. Overrides any existing mapping.
	 */
	public void register(String symbolicName, String className) {
		registry.put(symbolicName, className);
	}

	/**
	 * Resolve a name — returns the FQN class name if the input is a symbolic name,
	 * or returns the input unchanged if it's already a FQN or not found in the registry.
	 */
	public String resolve(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		ensureLoaded();
		String resolved = registry.get(name);
		if (resolved != null) {
			return resolved;
		}
		// Not a symbolic name — return as-is (FQN class name or static field ref)
		return name;
	}

	/**
	 * Check if a name is a known symbolic name (not a FQN).
	 */
	public boolean isSymbolic(String name) {
		if (name == null) return false;
		ensureLoaded();
		return registry.containsKey(name);
	}

	/**
	 * Load entries from a JSON object node. Each key is a symbolic name,
	 * each value is the FQN class name.
	 */
	public void loadFromJson(JsonNode node) {
		if (node == null || !node.isObject()) {
			return;
		}
		Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> entry = iter.next();
			String symbolicName = entry.getKey();
			JsonNode value = entry.getValue();
			if (value.isTextual()) {
				// Don't overwrite programmatic registrations
				registry.putIfAbsent(symbolicName, value.asText());
			}
		}
	}

	/**
	 * Get the current number of registered mappings.
	 */
	public int size() {
		ensureLoaded();
		return registry.size();
	}

	/**
	 * Clear all registrations (including defaults). Primarily for testing.
	 */
	public void clear() {
		registry.clear();
		loaded = false;
	}

	/**
	 * Create a standalone registry that doesn't auto-load from config files.
	 * Useful for tests that want to control exactly what's registered.
	 */
	public static ImplementationRegistry createStandalone() {
		ImplementationRegistry r = new ImplementationRegistry();
		r.loaded = true; // prevent ensureLoaded() from hitting the filesystem
		return r;
	}

	private synchronized void ensureLoaded() {
		if (loaded) {
			return;
		}
		loaded = true;
		try {
			BaseFile configBase = Env.getBinConfigBaseFile();
			BaseFile configFile = configBase.getChild(CONFIG_FILE);
			if (configFile.exists()) {
				JsonNode node = configFile.getJsonNode();
				loadFromJson(node);
				Log.util.info("Loaded %d implementation mappings from %s",
						registry.size(), configFile.getAbsolutePath());
			} else {
				Log.util.debug("No implementations config found at %s",
						configFile.getAbsolutePath());
			}
		} catch (Exception e) {
			Log.util.error("Failed to load implementations config: %s", e.getMessage());
		}
	}
}
