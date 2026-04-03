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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory methods for built-in {@link Generator} implementations.
 */
public class Generators {

	// --- CSV / list-based ---

	public static Generator csv(File file) throws IOException {
		CyclingList list = CyclingList.fromCsv(file);
		return list::next;
	}

	public static Generator csv(File dir, String filename) throws IOException {
		return csv(new File(dir, filename));
	}

	public static Generator items(String... values) {
		CyclingList list = new CyclingList(Arrays.asList(values));
		return list::next;
	}

	public static Generator items(List<String> values) {
		CyclingList list = new CyclingList(values);
		return list::next;
	}

	// --- Random numbers ---

	public static Generator randomInt(int min, int max) {
		return () -> ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static Generator randomLong(long min, long max) {
		return () -> ThreadLocalRandom.current().nextLong(min, max + 1);
	}

	public static Generator randomDouble(double min, double max) {
		return () -> min + ThreadLocalRandom.current().nextDouble() * (max - min);
	}

	public static Generator randomBool() {
		return () -> ThreadLocalRandom.current().nextBoolean();
	}

	// --- Random choice ---

	public static Generator pick(String... options) {
		return () -> options[ThreadLocalRandom.current().nextInt(options.length)];
	}

	public static Generator pick(List<String> options) {
		return () -> options.get(ThreadLocalRandom.current().nextInt(options.size()));
	}

	public static Generator weightedPick(String[] options, double[] weights) {
		double total = 0;
		for (double w : weights) total += w;
		double[] normalized = new double[weights.length];
		for (int i = 0; i < weights.length; i++) normalized[i] = weights[i] / total;
		return () -> {
			double r = ThreadLocalRandom.current().nextDouble();
			double cumulative = 0;
			for (int i = 0; i < normalized.length; i++) {
				cumulative += normalized[i];
				if (r < cumulative) return options[i];
			}
			return options[options.length - 1];
		};
	}

	// --- Dates and times ---

	public static Generator uuid() {
		return () -> UUID.randomUUID().toString();
	}

	public static Generator date() {
		long now = System.currentTimeMillis();
		long fiveYearsAgo = now - (5L * 365 * 24 * 60 * 60 * 1000);
		return dateInRange(fiveYearsAgo, now);
	}

	public static Generator dateInRange(String from, String to) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			long fromMs = sdf.parse(from).getTime();
			long toMs = sdf.parse(to).getTime();
			return dateInRange(fromMs, toMs);
		} catch (Exception e) {
			return date();
		}
	}

	private static Generator dateInRange(long fromMs, long toMs) {
		return () -> {
			long random = ThreadLocalRandom.current().nextLong(fromMs, toMs);
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(random));
		};
	}

	public static Generator timestamp() {
		return () -> System.currentTimeMillis();
	}

	public static Generator isoNow() {
		return () -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
	}

	// --- Sequences ---

	public static Generator sequence() {
		return sequence(1);
	}

	public static Generator sequence(int start) {
		AtomicInteger counter = new AtomicInteger(start);
		return counter::getAndIncrement;
	}

	public static Generator sequence(String prefix) {
		AtomicInteger counter = new AtomicInteger(1);
		return () -> prefix + counter.getAndIncrement();
	}

	// --- Patterns ---

	/**
	 * Generate strings from a pattern. Placeholders:
	 * <ul>
	 *   <li>{@code #} — random digit (0-9)</li>
	 *   <li>{@code ?} — random uppercase letter (A-Z)</li>
	 *   <li>{@code *} — random alphanumeric</li>
	 * </ul>
	 * Example: {@code pattern("###-???-####")} → "123-ABC-4567"
	 */
	public static Generator pattern(String pat) {
		return () -> {
			StringBuilder sb = new StringBuilder(pat.length());
			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			for (int i = 0; i < pat.length(); i++) {
				char c = pat.charAt(i);
				switch (c) {
					case '#':
						sb.append(rnd.nextInt(10));
						break;
					case '?':
						sb.append((char) ('A' + rnd.nextInt(26)));
						break;
					case '*':
						int r = rnd.nextInt(36);
						sb.append(r < 10 ? (char) ('0' + r) : (char) ('A' + r - 10));
						break;
					default:
						sb.append(c);
				}
			}
			return sb.toString();
		};
	}

	// --- Composite ---

	/**
	 * Combine multiple generators with a separator.
	 * Example: {@code composite(" ", firstNameGen, lastNameGen)} → "Alice Smith"
	 */
	public static Generator composite(String separator, Generator... parts) {
		return () -> {
			StringBuilder sb = new StringBuilder();
			for (Generator g : parts) {
				if (sb.length() > 0) sb.append(separator);
				sb.append(g.nextString());
			}
			return sb.toString();
		};
	}

	/**
	 * Template with named generator substitution.
	 * Example: {@code template("Dear {first} {last}", registry)} → "Dear Alice Smith"
	 */
	public static Generator template(String tmpl, GeneratorRegistry registry) {
		Pattern p = Pattern.compile("\\{(\\w+)}");
		return () -> {
			Matcher m = p.matcher(tmpl);
			StringBuilder sb = new StringBuilder();
			while (m.find()) {
				String name = m.group(1);
				Generator g = registry.get(name);
				String replacement = g != null ? g.nextString() : "{" + name + "}";
				m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			}
			m.appendTail(sb);
			return sb.toString();
		};
	}

	// --- Conditional ---

	/**
	 * Conditional generator — picks between two generators based on a runtime condition.
	 * Example: salary depends on department.
	 */
	public static Generator conditional(java.util.function.BooleanSupplier condition,
	                                     Generator ifTrue, Generator ifFalse) {
		return () -> condition.getAsBoolean() ? ifTrue.next() : ifFalse.next();
	}

	// --- Transform ---

	/**
	 * Transform the output of a generator.
	 * Example: {@code transform(nameGen, s -> s.toUpperCase())}
	 */
	public static Generator transform(Generator base, java.util.function.Function<Object, Object> fn) {
		return () -> fn.apply(base.next());
	}

	/**
	 * Format string with generator values.
	 * Example: {@code format("%s %s", firstNameGen, lastNameGen)} → "Alice Smith"
	 */
	public static Generator format(String pattern, Generator... generators) {
		return () -> {
			Object[] args = new Object[generators.length];
			for (int i = 0; i < generators.length; i++) {
				args[i] = generators[i].next();
			}
			return String.format(pattern, args);
		};
	}

	// --- Constant ---

	public static Generator constant(Object value) {
		return () -> value;
	}
}
