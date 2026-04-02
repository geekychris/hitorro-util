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

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Convenience facade over the {@link GeneratorRegistry}. Provides named methods
 * for common generators (firstName, email, etc.) and delegates to the registry
 * for everything else.
 *
 * <p>All convenience methods are backed by named generators in the registry.
 * Scripts can override any default by registering a replacement generator
 * with the same name.</p>
 */
public class DataGenerators {

	private final GeneratorRegistry registry;

	public DataGenerators(File generatorsDir) {
		this.registry = new GeneratorRegistry(generatorsDir);
		registerCompositeDefaults();
	}

	public DataGenerators(GeneratorRegistry registry) {
		this.registry = registry;
		registerCompositeDefaults();
	}

	private void registerCompositeDefaults() {
		// Composite generators that combine other generators
		if (!registry.has("full_name")) {
			registry.register("full_name", Generators.composite(" ",
					() -> registry.nextString("first_names"),
					() -> registry.nextString("last_names")));
		}
		if (!registry.has("email_gen")) {
			registry.register("email_gen", () -> {
				String first = registry.nextString("first_names");
				String last = registry.nextString("last_names");
				String domain = registry.nextString("email_domains");
				first = first != null ? first.toLowerCase().replace(" ", "") : "user";
				last = last != null ? last.toLowerCase().replace(" ", "") : "name";
				domain = domain != null ? domain : "example.com";
				return first + "." + last + "@" + domain;
			});
		}
		if (!registry.has("address")) {
			registry.register("address", Generators.composite(", ",
					() -> registry.nextString("streets"),
					() -> registry.nextString("cities")));
		}
	}

	/**
	 * Get the underlying registry for direct generator access and registration.
	 */
	public GeneratorRegistry getRegistry() {
		return registry;
	}

	// --- Registry shortcut: get any named generator's value ---

	/**
	 * Get the next value from a named generator, preserving its native type
	 * (Integer, Double, String, Boolean, etc.).
	 */
	public Object next(String name) {
		return registry.next(name);
	}

	public String nextString(String name) {
		return registry.nextString(name);
	}

	public Generator get(String name) {
		return registry.get(name);
	}

	// --- Convenience methods (backed by named generators) ---

	public String firstName() { return registry.nextString("first_names"); }
	public String lastName() { return registry.nextString("last_names"); }
	public String fullName() { return registry.nextString("full_name"); }
	public String email() { return registry.nextString("email_gen"); }
	public String phone() { return registry.nextString("phone_numbers"); }
	public String city() { return registry.nextString("cities"); }
	public String street() { return registry.nextString("streets"); }
	public String address() { return registry.nextString("address"); }
	public String product() { return registry.nextString("product_names"); }
	public String company() { return registry.nextString("company_names"); }
	public String lorem() { return registry.nextString("lorem"); }

	public String uuid() { return registry.nextString("uuid"); }
	public String date() { return registry.nextString("date"); }
	public long timestamp() { return (long) registry.get("timestamp").next(); }
	public int seq() { return (int) registry.get("sequence").next(); }

	public String dateInRange(String from, String to) {
		return Generators.dateInRange(from, to).nextString();
	}

	public int intBetween(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public long longBetween(long min, long max) {
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}

	public double doubleBetween(double min, double max) {
		return min + ThreadLocalRandom.current().nextDouble() * (max - min);
	}

	public boolean bool() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	public String pick(String... options) {
		return options[ThreadLocalRandom.current().nextInt(options.length)];
	}

	/**
	 * @deprecated Use {@code gen.getRegistry().get(name)} instead
	 */
	@Deprecated
	public CyclingList list(String name) {
		Generator g = registry.get(name);
		if (g == null) return null;
		// Wrap the generator in a CyclingList-compatible interface for backward compat
		return new CyclingList(java.util.List.of("use gen.next(\"" + name + "\") instead"));
	}
}
