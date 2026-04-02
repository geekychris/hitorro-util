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
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVSMerger Tests")
class JVSMergerTest {

	@Test
	@DisplayName("Should merge disjoint object keys")
	void shouldMergeDisjointKeys() {
		JVS base = JVS.read("{\"a\":\"1\"}");
		JVS override = JVS.read("{\"b\":\"2\"}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		assertThat(base.getString("a")).isEqualTo("1");
		assertThat(base.getString("b")).isEqualTo("2");
	}

	@Test
	@DisplayName("Should override scalar values")
	void shouldOverrideScalarValues() {
		JVS base = JVS.read("{\"key\":\"old\"}");
		JVS override = JVS.read("{\"key\":\"new\"}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		assertThat(base.getString("key")).isEqualTo("new");
	}

	@Test
	@DisplayName("Should merge nested objects recursively")
	void shouldMergeNestedObjects() {
		JVS base = JVS.read("{\"outer\":{\"a\":\"1\",\"b\":\"2\"}}");
		JVS override = JVS.read("{\"outer\":{\"b\":\"X\",\"c\":\"3\"}}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		assertThat(base.getString("outer.a")).isEqualTo("1");
		assertThat(base.getString("outer.b")).isEqualTo("X");
		assertThat(base.getString("outer.c")).isEqualTo("3");
	}

	@Test
	@DisplayName("Should merge arrays by index position")
	void shouldMergeArraysByIndex() {
		JVS base = JVS.read("{\"arr\":[\"a\",\"b\",\"c\"]}");
		JVS override = JVS.read("{\"arr\":[\"X\"]}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		JsonNode arr = base.get("arr");
		assertThat(arr.size()).isEqualTo(3);
		assertThat(arr.get(0).textValue()).isEqualTo("X");
		assertThat(arr.get(1).textValue()).isEqualTo("b");
		assertThat(arr.get(2).textValue()).isEqualTo("c");
	}

	@Test
	@DisplayName("Should expand base array when override is larger")
	void shouldExpandArrayWhenOverrideIsLarger() {
		JVS base = JVS.read("{\"arr\":[\"a\"]}");
		JVS override = JVS.read("{\"arr\":[\"X\",\"Y\",\"Z\"]}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		JsonNode arr = base.get("arr");
		assertThat(arr.size()).isEqualTo(3);
		assertThat(arr.get(0).textValue()).isEqualTo("X");
		assertThat(arr.get(1).textValue()).isEqualTo("Y");
		assertThat(arr.get(2).textValue()).isEqualTo("Z");
	}

	@Test
	@DisplayName("Should merge empty base array correctly")
	void shouldMergeEmptyBaseArray() {
		JVS base = JVS.read("{\"arr\":[]}");
		JVS override = JVS.read("{\"arr\":[\"X\",\"Y\"]}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		JsonNode arr = base.get("arr");
		assertThat(arr.size()).isEqualTo(2);
		assertThat(arr.get(0).textValue()).isEqualTo("X");
		assertThat(arr.get(1).textValue()).isEqualTo("Y");
	}

	@Test
	@DisplayName("Should merge nested objects within arrays")
	void shouldMergeNestedObjectsInArrays() {
		JVS base = JVS.read("{\"arr\":[{\"x\":1,\"y\":2}]}");
		JVS override = JVS.read("{\"arr\":[{\"y\":99,\"z\":3}]}");

		JVSMerger.merge(base.getJsonNode(), override.getJsonNode());

		JsonNode elem = base.get("arr").get(0);
		assertThat(elem.get("x").intValue()).isEqualTo(1);
		assertThat(elem.get("y").intValue()).isEqualTo(99);
		assertThat(elem.get("z").intValue()).isEqualTo(3);
	}
}
