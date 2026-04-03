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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ExecutionTrace — runtime observability for the projection pipeline.
 * Written test-first.
 */
@DisplayName("ExecutionTrace Tests")
class ExecutionTraceTest {

	@Test
	@DisplayName("Trace should record action executions")
	void shouldRecordActions() {
		ExecutionTrace trace = new ExecutionTrace();

		trace.recordAction("title.text_en_s", "IndexerAction", 2);
		trace.recordAction("body.text_en_m", "IndexerAction", 5);

		assertThat(trace.getEntries()).hasSize(2);
		assertThat(trace.getEntries().get(0).path()).isEqualTo("title.text_en_s");
		assertThat(trace.getEntries().get(0).actionType()).isEqualTo("IndexerAction");
		assertThat(trace.getEntries().get(0).elapsedMicros()).isEqualTo(2);
	}

	@Test
	@DisplayName("Trace should track total elapsed time")
	void shouldTrackTotalElapsed() {
		ExecutionTrace trace = new ExecutionTrace();

		trace.recordAction("a", "IndexerAction", 100);
		trace.recordAction("b", "IndexerAction", 200);

		assertThat(trace.getTotalMicros()).isEqualTo(300);
	}

	@Test
	@DisplayName("Trace should record null/skipped fields")
	void shouldRecordSkips() {
		ExecutionTrace trace = new ExecutionTrace();

		trace.recordSkip("optional_field", "value was null");

		assertThat(trace.getSkips()).hasSize(1);
		assertThat(trace.getSkips().get(0).path()).isEqualTo("optional_field");
		assertThat(trace.getSkips().get(0).reason()).isEqualTo("value was null");
	}

	@Test
	@DisplayName("Trace should record errors")
	void shouldRecordErrors() {
		ExecutionTrace trace = new ExecutionTrace();

		trace.recordError("bad_field", "PropaccessError: path not found");

		assertThat(trace.getErrors()).hasSize(1);
	}

	@Test
	@DisplayName("Empty trace should have zero entries and time")
	void emptyTrace() {
		ExecutionTrace trace = new ExecutionTrace();

		assertThat(trace.getEntries()).isEmpty();
		assertThat(trace.getSkips()).isEmpty();
		assertThat(trace.getErrors()).isEmpty();
		assertThat(trace.getTotalMicros()).isEqualTo(0);
	}

	@Test
	@DisplayName("Summary should show counts and timing")
	void summaryShouldShowCounts() {
		ExecutionTrace trace = new ExecutionTrace();

		trace.recordAction("a", "IndexerAction", 100);
		trace.recordAction("b", "EnrichAction", 200);
		trace.recordSkip("c", "null");
		trace.recordError("d", "failed");

		String summary = trace.summary();
		assertThat(summary).contains("2 action");
		assertThat(summary).contains("1 skip");
		assertThat(summary).contains("1 error");
	}

	@Test
	@DisplayName("Trace should be attachable to ProjectionContext")
	void shouldAttachToContext() {
		ProjectionContext pc = new ProjectionContext();
		assertThat(pc.trace).isNull();

		pc.trace = new ExecutionTrace();
		pc.trace.recordAction("test", "TestAction", 1);

		assertThat(pc.trace.getEntries()).hasSize(1);
	}
}
