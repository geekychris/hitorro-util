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
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Mapper&lt;JVS, JVS&gt; that transforms documents using a Groovy DSL script.
 * Implements the HiTorro Mapper interface so it can be used with MappingIterator,
 * NestingIterator, and the standard pipeline infrastructure.
 *
 * <h3>Usage</h3>
 * <pre>
 * // From a script file
 * GroovyTransformMapper mapper = GroovyTransformMapper.fromFile(
 *     new File("config/transforms/enrich_person.groovy"),
 *     new File("config/generators")
 * );
 *
 * // In a pipeline
 * Iterator&lt;JVS&gt; source = new MappingIterator&lt;&gt;(jsonIter, Json2JVSMapper.me);
 * Iterator&lt;JVS&gt; transformed = new MappingIterator&lt;&gt;(source, mapper);
 * </pre>
 */
public class GroovyTransformMapper extends BaseMapper<JVS, JVS> {

	private final Script compiledScript;
	private final DataGenerators generators;
	private final AtomicInteger counter = new AtomicInteger(0);
	private AIOperations aiOperations;
	private EnrichOperations enrichOperations;

	public GroovyTransformMapper(Script compiledScript, DataGenerators generators) {
		this.compiledScript = compiledScript;
		this.generators = generators;
	}

	public GroovyTransformMapper withAI(AIOperations ai) {
		this.aiOperations = ai;
		return this;
	}

	public GroovyTransformMapper withEnrich(EnrichOperations enrich) {
		this.enrichOperations = enrich;
		return this;
	}

	public static GroovyTransformMapper fromFile(File scriptFile, File generatorsDir) throws IOException {
		String scriptText = new String(Files.readAllBytes(scriptFile.toPath()));
		return fromString(scriptText, generatorsDir);
	}

	public static GroovyTransformMapper fromString(String scriptText, File generatorsDir) {
		DataGenerators gen = new DataGenerators(generatorsDir);
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(TransformDSL.class.getName());

		GroovyShell shell = new GroovyShell(
				GroovyTransformMapper.class.getClassLoader(), config);
		Script script = shell.parse(scriptText);

		return new GroovyTransformMapper(script, gen);
	}

	public static GroovyTransformMapper fromString(String scriptText, DataGenerators gen) {
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(TransformDSL.class.getName());

		GroovyShell shell = new GroovyShell(
				GroovyTransformMapper.class.getClassLoader(), config);
		Script script = shell.parse(scriptText);

		return new GroovyTransformMapper(script, gen);
	}

	@Override
	public JVS apply(JVS input) {
		MappingContext ctx = new MappingContext(input, generators, aiOperations, enrichOperations, counter.getAndIncrement());
		try {
			TransformDSL dsl = (TransformDSL) compiledScript;
			dsl.setContext(ctx);
			dsl.run();
			return ctx.target;
		} catch (Exception e) {
			Log.util.error("Transform script failed on doc %d: %s — %s", ctx.docIndex, e.getClass().getSimpleName(), e.getMessage());
			return null;
		}
	}

	@Override
	public boolean isThreadSafe() {
		return false; // Groovy Script is not thread-safe
	}

	public DataGenerators getGenerators() {
		return generators;
	}
}
