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

import com.hitorro.util.core.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Named registry of {@link Generator} instances. Scripts define generators
 * via DSL methods; the registry holds them for lookup by name.
 *
 * <p>Pre-loaded with defaults from CSV files in the generators directory.
 * Scripts can override or add generators at any time.</p>
 */
public class GeneratorRegistry {

	private final Map<String, Generator> generators = new ConcurrentHashMap<>();
	private final File generatorsDir;

	public GeneratorRegistry(File generatorsDir) {
		this.generatorsDir = generatorsDir;
		loadDefaults();
	}

	public GeneratorRegistry() {
		this.generatorsDir = null;
	}

	private void loadDefaults() {
		if (generatorsDir == null || !generatorsDir.exists()) return;

		// Auto-load any CSV file as a generator named after the file
		File[] csvFiles = generatorsDir.listFiles((d, n) -> n.endsWith(".csv"));
		if (csvFiles != null) {
			for (File f : csvFiles) {
				String name = f.getName().replace(".csv", "");
				try {
					generators.put(name, Generators.csv(f));
				} catch (IOException e) {
					Log.util.error("Failed to load generator CSV %s: %s", f.getName(), e.getMessage());
				}
			}
		}

		// Built-in generators
		generators.putIfAbsent("uuid", Generators.uuid());
		generators.putIfAbsent("date", Generators.date());
		generators.putIfAbsent("timestamp", Generators.timestamp());
		generators.putIfAbsent("iso_now", Generators.isoNow());
		generators.putIfAbsent("bool", Generators.randomBool());
		generators.putIfAbsent("sequence", Generators.sequence());
	}

	public void register(String name, Generator generator) {
		generators.put(name, generator);
	}

	public Generator get(String name) {
		return generators.get(name);
	}

	public boolean has(String name) {
		return generators.containsKey(name);
	}

	/**
	 * Get a value from a named generator. Returns null if the generator doesn't exist.
	 */
	public Object next(String name) {
		Generator g = generators.get(name);
		return g != null ? g.next() : null;
	}

	/**
	 * Get a string value from a named generator.
	 */
	public String nextString(String name) {
		Generator g = generators.get(name);
		return g != null ? g.nextString() : null;
	}

	public File getGeneratorsDir() {
		return generatorsDir;
	}

	public int size() {
		return generators.size();
	}
}
