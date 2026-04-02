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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVSVariableResolver Tests")
class JVSVariableResolverTest {

	@Test
	@DisplayName("Should resolve single variable")
	void shouldResolveSingleVariable() throws Exception {
		JVS master = JVS.read("{\"name\":\"World\"}");
		TextNode input = JsonNodeFactory.instance.textNode("Hello ${name}");

		JsonNode result = JVSVariableResolver.resolveVariableAux(input, master);

		assertThat(result.textValue()).isEqualTo("Hello World");
	}

	@Test
	@DisplayName("Should resolve multiple variables")
	void shouldResolveMultipleVariables() throws Exception {
		JVS master = JVS.read("{\"first\":\"John\",\"last\":\"Doe\"}");
		TextNode input = JsonNodeFactory.instance.textNode("${first} ${last}");

		JsonNode result = JVSVariableResolver.resolveVariableAux(input, master);

		assertThat(result.textValue()).isEqualTo("John Doe");
	}

	@Test
	@DisplayName("Should return original when no variables present")
	void shouldReturnOriginalWhenNoVariables() throws Exception {
		JVS master = JVS.read("{\"name\":\"World\"}");
		TextNode input = JsonNodeFactory.instance.textNode("Hello World");

		JsonNode result = JVSVariableResolver.resolveVariableAux(input, master);

		// No variables found, returns original node
		assertThat(result).isSameAs(input);
	}

	@Test
	@DisplayName("Should handle missing variables gracefully")
	void shouldHandleMissingVariables() throws Exception {
		JVS master = JVS.read("{\"name\":\"World\"}");
		TextNode input = JsonNodeFactory.instance.textNode("Hello ${missing}");

		JsonNode result = JVSVariableResolver.resolveVariableAux(input, master);

		assertThat(result.textValue()).isEqualTo("Hello ");
	}

	@Test
	@DisplayName("Should return non-textual variable value directly")
	void shouldReturnNonTextualVariableDirectly() throws Exception {
		JVS master = JVS.read("{\"count\":42}");
		TextNode input = JsonNodeFactory.instance.textNode("${count}");

		JsonNode result = JVSVariableResolver.resolveVariableAux(input, master);

		assertThat(result.isNumber()).isTrue();
		assertThat(result.intValue()).isEqualTo(42);
	}

	@Test
	@DisplayName("Should resolve variables case-insensitively")
	void shouldResolveVariablesCaseInsensitively() throws Exception {
		JVS master = JVS.read("{\"name\":\"World\"}");
		TextNode input = JsonNodeFactory.instance.textNode("Hello ${NAME}");

		JsonNode result = JVSVariableResolver.resolveVariableAux(input, master);

		assertThat(result.textValue()).isEqualTo("Hello World");
	}

	@Test
	@DisplayName("resolveVariables should resolve in object nodes")
	void resolveVariablesShouldResolveInObjectNodes() throws Exception {
		JVS master = JVS.read("{\"greeting\":\"Hi\"}");
		JVS target = JVS.read("{\"msg\":\"${greeting} there\"}");

		JVSVariableResolver.resolveVariables(master, target.getJsonNode());

		assertThat(target.getString("msg")).isEqualTo("Hi there");
	}

	@Test
	@DisplayName("resolveVariables should resolve in array nodes")
	void resolveVariablesShouldResolveInArrayNodes() throws Exception {
		JVS master = JVS.read("{\"val\":\"X\"}");
		JVS target = JVS.read("{\"items\":[\"${val}\",\"fixed\"]}");

		JVSVariableResolver.resolveVariables(master, target.getJsonNode());

		JsonNode items = target.get("items");
		assertThat(items.get(0).textValue()).isEqualTo("X");
		assertThat(items.get(1).textValue()).isEqualTo("fixed");
	}

	@Test
	@DisplayName("resolveTextVariableAux should return null for null input")
	void shouldReturnNullForNullInput() throws Exception {
		JVS master = JVS.read("{}");

		JsonNode result = JVSVariableResolver.resolveTextVariableAux(null, master, null);

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("resolveTextVariableAux should prefer override over master")
	void shouldPreferOverrideOverMaster() throws Exception {
		JVS master = JVS.read("{\"val\":\"from-master\"}");
		JVS override = JVS.read("{\"val\":\"from-override\"}");

		JsonNode result = JVSVariableResolver.resolveTextVariableAux("${val}", master, override);

		assertThat(result.textValue()).isEqualTo("from-override");
	}
}
