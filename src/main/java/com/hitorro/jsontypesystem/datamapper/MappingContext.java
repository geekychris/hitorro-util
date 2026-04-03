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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

/**
 * The context for a single mapping operation. Contains three registers accessible
 * from Groovy DSL scripts:
 *
 * <ul>
 *   <li><b>source</b> — the input JVS document (read-only by convention)</li>
 *   <li><b>target</b> — the output JVS document being built</li>
 *   <li><b>work</b> — a scratch register for intermediate values</li>
 * </ul>
 *
 * All three are JVS objects, so the DSL can use paths like "source.title.mls[0].text"
 * uniformly. The context also holds the data generators and a document counter.
 */
public class MappingContext {
	public final JVS source;
	public final JVS target;
	public final JVS work;
	public final DataGenerators gen;
	public final AIOperations ai;
	public final EnrichOperations enrichOps;
	public int docIndex;

	public MappingContext(JVS source, DataGenerators gen, int docIndex) {
		this(source, gen, null, null, docIndex);
	}

	public MappingContext(JVS source, DataGenerators gen, AIOperations ai, int docIndex) {
		this(source, gen, ai, null, docIndex);
	}

	public MappingContext(JVS source, DataGenerators gen, AIOperations ai, EnrichOperations enrichOps, int docIndex) {
		this.source = source;
		this.target = new JVS(source.getJsonNode().deepCopy());
		this.work = new JVS();
		this.gen = gen;
		this.ai = ai;
		this.enrichOps = enrichOps;
		this.docIndex = docIndex;
	}

	// Convenience methods used by DSL

	public JsonNode get(String path) throws PropaccessError {
		if (path.startsWith("source.")) {
			return source.get(path.substring(7));
		} else if (path.startsWith("target.")) {
			return target.get(path.substring(7));
		} else if (path.startsWith("work.")) {
			return work.get(path.substring(5));
		}
		return source.get(path);
	}

	public void set(String path, Object value) throws PropaccessError {
		if (path.startsWith("target.")) {
			target.set(path.substring(7), value);
		} else if (path.startsWith("work.")) {
			work.set(path.substring(5), value);
		} else {
			target.set(path, value);
		}
	}

	public String getString(String path) throws PropaccessError {
		if (path.startsWith("source.")) {
			return source.getString(path.substring(7));
		} else if (path.startsWith("target.")) {
			return target.getString(path.substring(7));
		} else if (path.startsWith("work.")) {
			return work.getString(path.substring(5));
		}
		return source.getString(path);
	}
}
