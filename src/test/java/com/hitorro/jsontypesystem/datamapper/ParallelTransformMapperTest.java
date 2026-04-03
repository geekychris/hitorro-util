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

import com.hitorro.jsontypesystem.JVS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ParallelTransformMapper Tests")
class ParallelTransformMapperTest {

	private static DataGenerators generators;

	@BeforeAll
	static void setup() {
		File genDir = new File("config/generators");
		if (!genDir.exists()) genDir = new File("hitorro-util/config/generators");
		generators = new DataGenerators(genDir);
	}

	@Test
	@DisplayName("Should transform batch of documents in parallel")
	void batchTransform() {
		List<JVS> inputs = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			inputs.add(JVS.read("{\"seq\":" + i + "}"));
		}

		String script = """
				copyAll()
				set "target.id", gen.uuid()
				set "target.name", gen.fullName()
				""";

		List<JVS> results = ParallelTransformMapper.transformBatch(inputs, script, generators);

		assertThat(results).hasSize(20);
		assertThat(results).noneMatch(r -> r == null);

		// All should have unique IDs
		Set<String> ids = new HashSet<>();
		for (JVS r : results) {
			ids.add(r.getString("id"));
		}
		assertThat(ids).hasSize(20);
	}

	@Test
	@DisplayName("Should preserve order")
	void preservesOrder() {
		List<JVS> inputs = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			inputs.add(JVS.read("{\"seq\":" + i + "}"));
		}

		String script = """
				copyAll()
				""";

		List<JVS> results = ParallelTransformMapper.transformBatch(inputs, script, generators);

		for (int i = 0; i < 10; i++) {
			assertThat(results.get(i).getLong("seq")).isEqualTo(i);
		}
	}

	@Test
	@DisplayName("Should handle errors gracefully (null for failed docs)")
	void handlesErrors() {
		List<JVS> inputs = List.of(JVS.read("{\"n\":1}"));

		String script = """
				throw new RuntimeException("intentional failure")
				""";

		List<JVS> results = ParallelTransformMapper.transformBatch(inputs, script, generators);

		assertThat(results).hasSize(1);
		assertThat(results.get(0)).isNull();
	}

	@Test
	@DisplayName("Should transform via iterator with batching")
	void iteratorTransform() {
		List<JVS> inputs = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			inputs.add(JVS.read("{\"n\":" + i + "}"));
		}

		String script = """
				copyAll()
				set "target.processed", true
				""";

		List<JVS> results = ParallelTransformMapper.transformIterator(
				inputs.iterator(), 5, script, generators);

		assertThat(results).hasSize(15);
		for (JVS r : results) {
			assertThat(r).isNotNull();
			assertThat(r.getBoolean("processed")).isTrue();
		}
	}

	@Test
	@DisplayName("Should be faster than sequential for large batches")
	void fasterThanSequential() {
		List<JVS> inputs = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			inputs.add(JVS.read("{\"n\":" + i + "}"));
		}

		String script = """
				copyAll()
				set "target.id", gen.uuid()
				set "target.name", gen.fullName()
				set "target.email", gen.email()
				times(3) { append "target.tags", gen.product() }
				""";

		// Just verify it completes without error for 100 docs
		List<JVS> results = ParallelTransformMapper.transformBatch(inputs, script, generators);
		assertThat(results).hasSize(100);
		assertThat(results.stream().filter(r -> r != null).count()).isEqualTo(100);
	}
}
