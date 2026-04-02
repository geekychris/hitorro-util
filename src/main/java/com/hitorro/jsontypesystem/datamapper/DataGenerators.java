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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Data generators for synthetic data production. Provides cycling lists loaded from
 * CSV files plus built-in generators for dates, UUIDs, numbers, etc.
 *
 * <p>Available from Groovy DSL as {@code gen}:</p>
 * <pre>
 * gen.firstName()       // cycles through first_names.csv
 * gen.lastName()        // cycles through last_names.csv
 * gen.fullName()        // "FirstName LastName"
 * gen.email()           // "firstname.lastname@domain.com"
 * gen.phone()           // cycles through phone_numbers.csv
 * gen.city()            // cycles through cities.csv (city only)
 * gen.address()         // "123 Oak St, CityName, ST"
 * gen.product()         // cycles through product_names.csv
 * gen.company()         // cycles through company_names.csv
 * gen.lorem()           // cycles through lorem.csv
 * gen.uuid()            // random UUID
 * gen.date()            // random date within last 5 years
 * gen.intBetween(1,100) // random int
 * gen.list("myfile")    // get a custom CyclingList by name
 * </pre>
 */
public class DataGenerators {

	private final Map<String, CyclingList> lists = new ConcurrentHashMap<>();
	private final File generatorsDir;

	private CyclingList firstNames;
	private CyclingList lastNames;
	private CyclingList cities;
	private CyclingList streets;
	private CyclingList phones;
	private CyclingList products;
	private CyclingList companies;
	private CyclingList emailDomains;
	private CyclingList loremTexts;

	public DataGenerators(File generatorsDir) {
		this.generatorsDir = generatorsDir;
		loadDefaults();
	}

	private void loadDefaults() {
		firstNames = loadList("first_names.csv");
		lastNames = loadList("last_names.csv");
		cities = loadList("cities.csv");
		streets = loadList("streets.csv");
		phones = loadList("phone_numbers.csv");
		products = loadList("product_names.csv");
		companies = loadList("company_names.csv");
		emailDomains = loadList("email_domains.csv");
		loremTexts = loadList("lorem.csv");
	}

	private CyclingList loadList(String filename) {
		File f = new File(generatorsDir, filename);
		if (!f.exists()) {
			Log.util.debug("Generator file not found: %s", f.getAbsolutePath());
			return new CyclingList(java.util.List.of("placeholder"));
		}
		try {
			CyclingList list = CyclingList.fromCsv(f);
			lists.put(filename.replace(".csv", ""), list);
			return list;
		} catch (IOException e) {
			Log.util.error("Failed to load generator file %s: %s", filename, e.getMessage());
			return new CyclingList(java.util.List.of("placeholder"));
		}
	}

	/**
	 * Load a custom CyclingList from a CSV file in the generators directory.
	 */
	public CyclingList list(String name) {
		return lists.computeIfAbsent(name, n -> {
			String filename = n.endsWith(".csv") ? n : n + ".csv";
			File f = new File(generatorsDir, filename);
			if (!f.exists()) {
				Log.util.error("Custom generator file not found: %s", f.getAbsolutePath());
				return new CyclingList(java.util.List.of("placeholder"));
			}
			try {
				return CyclingList.fromCsv(f);
			} catch (IOException e) {
				Log.util.error("Failed to load generator %s: %s", filename, e.getMessage());
				return new CyclingList(java.util.List.of("placeholder"));
			}
		});
	}

	// --- Named generators ---

	public String firstName() {
		return firstNames.next();
	}

	public String lastName() {
		return lastNames.next();
	}

	public String fullName() {
		return firstName() + " " + lastName();
	}

	public String email() {
		String first = firstName().toLowerCase().replace(" ", "");
		String last = lastName().toLowerCase().replace(" ", "");
		String domain = emailDomains.next();
		return first + "." + last + "@" + domain;
	}

	public String phone() {
		return phones.next();
	}

	public String city() {
		return cities.next();
	}

	public String street() {
		return streets.next();
	}

	public String address() {
		return street() + ", " + city();
	}

	public String product() {
		return products.next();
	}

	public String company() {
		return companies.next();
	}

	public String lorem() {
		return loremTexts.next();
	}

	public String uuid() {
		return UUID.randomUUID().toString();
	}

	public String date() {
		long now = System.currentTimeMillis();
		long fiveYearsAgo = now - (5L * 365 * 24 * 60 * 60 * 1000);
		long random = ThreadLocalRandom.current().nextLong(fiveYearsAgo, now);
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(random));
	}

	public String dateInRange(String from, String to) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			long fromMs = sdf.parse(from).getTime();
			long toMs = sdf.parse(to).getTime();
			long random = ThreadLocalRandom.current().nextLong(fromMs, toMs);
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(random));
		} catch (Exception e) {
			return date();
		}
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
}
