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
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Processes batches of JVS documents in parallel using Java 21 virtual threads.
 * Each document gets its own virtual thread for transform execution.
 *
 * <p>Use when you have a batch of documents to transform and want to maximize
 * throughput, especially for transforms that include AI operations (translate,
 * summarize) which are I/O-bound.</p>
 *
 * <pre>
 * List&lt;JVS&gt; inputs = ...;
 * GroovyTransformMapper mapper = GroovyTransformMapper.fromFile(...);
 * List&lt;JVS&gt; results = ParallelTransformMapper.transformBatch(inputs, mapper);
 * </pre>
 */
public class ParallelTransformMapper {

	/**
	 * Transform a batch of documents in parallel using virtual threads.
	 * Each document is processed by a separate virtual thread.
	 * Order is preserved — result[i] corresponds to input[i].
	 *
	 * @param inputs   documents to transform
	 * @param scriptText Groovy script source
	 * @param generators data generators
	 * @return list of transformed documents (null entries for failed transforms)
	 */
	public static List<JVS> transformBatch(List<JVS> inputs, String scriptText,
	                                        DataGenerators generators) {
		return transformBatch(inputs, scriptText, generators, null, null);
	}

	public static List<JVS> transformBatch(List<JVS> inputs, String scriptText,
	                                        DataGenerators generators,
	                                        AIOperations ai, EnrichOperations enrich) {
		// Each virtual thread gets its own compiled mapper (Script is not thread-safe)
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<JVS>> futures = new ArrayList<>(inputs.size());

			for (JVS input : inputs) {
				futures.add(executor.submit(() -> {
					GroovyTransformMapper mapper = GroovyTransformMapper.fromString(scriptText, generators);
					if (ai != null) mapper.withAI(ai);
					if (enrich != null) mapper.withEnrich(enrich);
					return mapper.apply(input);
				}));
			}

			List<JVS> results = new ArrayList<>(inputs.size());
			for (Future<JVS> future : futures) {
				try {
					results.add(future.get());
				} catch (Exception e) {
					Log.util.error("Parallel transform failed: %s", e.getMessage());
					results.add(null);
				}
			}
			return results;
		}
	}

	/**
	 * Transform documents from an iterator, collecting results.
	 * Uses virtual threads for parallelism with a configurable batch size.
	 */
	public static List<JVS> transformIterator(Iterator<JVS> inputs, int batchSize,
	                                           String scriptText, DataGenerators generators) {
		List<JVS> allResults = new ArrayList<>();
		List<JVS> batch = new ArrayList<>(batchSize);

		while (inputs.hasNext()) {
			batch.clear();
			for (int i = 0; i < batchSize && inputs.hasNext(); i++) {
				batch.add(inputs.next());
			}
			allResults.addAll(transformBatch(batch, scriptText, generators));
		}
		return allResults;
	}
}
