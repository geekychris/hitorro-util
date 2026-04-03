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
package com.hitorro.jsontypesystem.executors;

import java.util.ArrayList;
import java.util.List;

/**
 * Records runtime execution trace for the projection pipeline.
 * Attach to a ProjectionContext to capture which fields were projected,
 * which were skipped, timing, and errors.
 *
 * <pre>
 * ProjectionContext pc = new ProjectionContext();
 * pc.trace = new ExecutionTrace();
 * executionNode.project(pc);
 * System.out.println(pc.trace.summary());
 * </pre>
 */
public class ExecutionTrace {

	public record ActionEntry(String path, String actionType, long elapsedMicros) {}
	public record SkipEntry(String path, String reason) {}
	public record ErrorEntry(String path, String message) {}

	private final List<ActionEntry> entries = new ArrayList<>();
	private final List<SkipEntry> skips = new ArrayList<>();
	private final List<ErrorEntry> errors = new ArrayList<>();
	private long totalMicros = 0;

	public void recordAction(String path, String actionType, long elapsedMicros) {
		entries.add(new ActionEntry(path, actionType, elapsedMicros));
		totalMicros += elapsedMicros;
	}

	public void recordSkip(String path, String reason) {
		skips.add(new SkipEntry(path, reason));
	}

	public void recordError(String path, String message) {
		errors.add(new ErrorEntry(path, message));
	}

	public List<ActionEntry> getEntries() { return entries; }
	public List<SkipEntry> getSkips() { return skips; }
	public List<ErrorEntry> getErrors() { return errors; }
	public long getTotalMicros() { return totalMicros; }

	public String summary() {
		return String.format("%d action(s) in %d\u00b5s, %d skip(s), %d error(s)",
				entries.size(), totalMicros, skips.size(), errors.size());
	}

	public String detail() {
		var sb = new StringBuilder();
		sb.append(summary()).append("\n");
		for (ActionEntry e : entries) {
			sb.append(String.format("  [%s] %s — %d\u00b5s\n", e.actionType, e.path, e.elapsedMicros));
		}
		for (SkipEntry s : skips) {
			sb.append(String.format("  [SKIP] %s — %s\n", s.path, s.reason));
		}
		for (ErrorEntry e : errors) {
			sb.append(String.format("  [ERROR] %s — %s\n", e.path, e.message));
		}
		return sb.toString();
	}
}
