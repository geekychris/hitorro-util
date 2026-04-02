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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A list that cycles: when you call next() past the end, it wraps around.
 * Loaded from a CSV file (first column after header). Thread-safe via AtomicInteger.
 */
public class CyclingList {
	private final List<String> items;
	private final AtomicInteger index = new AtomicInteger(0);

	public CyclingList(List<String> items) {
		this.items = items;
	}

	public static CyclingList fromCsv(File file) throws IOException {
		List<String> items = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String header = reader.readLine(); // skip header
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					// Take first column (before comma), strip quotes
					int comma = line.indexOf(',');
					String val = comma > 0 ? line.substring(0, comma) : line;
					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					}
					items.add(val);
				}
			}
		}
		if (items.isEmpty()) {
			Log.util.error("CyclingList loaded empty file: %s", file.getAbsolutePath());
		}
		return new CyclingList(items);
	}

	public static CyclingList fromCsvAllColumns(File file) throws IOException {
		List<String> items = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String header = reader.readLine(); // skip header
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					items.add(line);
				}
			}
		}
		return new CyclingList(items);
	}

	public String next() {
		if (items.isEmpty()) return "";
		int i = index.getAndIncrement() % items.size();
		return items.get(i);
	}

	public String get(int i) {
		if (items.isEmpty()) return "";
		return items.get(i % items.size());
	}

	public int size() {
		return items.size();
	}

	public void shuffle() {
		Collections.shuffle(items);
	}

	public void reset() {
		index.set(0);
	}
}
