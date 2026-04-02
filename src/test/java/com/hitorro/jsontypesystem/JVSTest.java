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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.testframework.TestPlus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Disabled;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVS (JSON Value System) Tests")
class JVSTest implements TestPlus {

	@Nested
	@DisplayName("Construction")
	class Construction {

		@Test
		@DisplayName("Should create JVS from JSON node")
		void shouldCreateJvsFromJsonNode() {
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("name", "test");

			JVS jvs = new JVS(node);

			assertThat(jvs).isNotNull();
		}

		@Test
		@DisplayName("Should create empty JVS")
		void shouldCreateEmptyJvs() {
			JVS jvs = new JVS();

			assertThat(jvs).isNotNull();
		}

		@Test
		@DisplayName("Should read JVS from JSON string")
		void shouldReadJvsFromJsonString() {
			String jsonString = "{\"key\":\"value\",\"number\":42}";

			JVS jvs = JVS.read(jsonString);

			assertThat(jvs).isNotNull();
		}
	}

	@Nested
	@DisplayName("Property Access - get/set/getString")
	class PropertyAccess {

		@Test
		@DisplayName("Should get and set simple string values")
		void shouldGetAndSetSimpleStringValues() {
			JVS jvs = new JVS();
			jvs.set("name", "hello");

			assertThat(jvs.getString("name")).isEqualTo("hello");
		}

		@Test
		@DisplayName("Should get and set nested path values")
		void shouldGetAndSetNestedPathValues() {
			JVS jvs = new JVS();
			jvs.set("outer.inner", "deep");

			assertThat(jvs.getString("outer.inner")).isEqualTo("deep");
		}

		@Test
		@DisplayName("Should return null for missing path")
		void shouldReturnNullForMissingPath() {
			JVS jvs = new JVS();

			assertThat(jvs.get("nonexistent")).isNull();
		}

		@Test
		@DisplayName("Should return null for null path")
		void shouldReturnNullForNullPath() {
			JVS jvs = new JVS();

			assertThat(jvs.get((String) null)).isNull();
		}

		@Test
		@DisplayName("Should handle predefined property accessors")
		void shouldHavePredefinedPropertyAccessors() {
			assertThat(JVS.typeKey).isNotNull();
			assertThat(JVS.didKey).isNotNull();
			assertThat(JVS.idKey).isNotNull();
			assertThat(JVS.createdKey).isNotNull();
			assertThat(JVS.modifiedKey).isNotNull();
			assertThat(JVS.titleKey).isNotNull();
			assertThat(JVS.bodyKey).isNotNull();
			assertThat(JVS.domainKey).isNotNull();
			assertThat(JVS.docKey).isNotNull();
		}

		@Test
		@DisplayName("Should have variable delimiters")
		void shouldHaveVariableDelimiters() {
			assertThat(JVS.VariableStart).isEqualTo("${");
			assertThat(JVS.VariableEnd).isEqualTo("}");
		}
	}

	@Nested
	@DisplayName("getBoolean")
	class GetBoolean {

		@Test
		@DisplayName("Should return true for boolean true")
		void shouldReturnTrueForTrue() {
			JVS jvs = JVS.read("{\"flag\":true}");

			assertThat(jvs.getBoolean("flag")).isTrue();
		}

		@Test
		@DisplayName("Should return false for boolean false")
		void shouldReturnFalseForFalse() {
			JVS jvs = JVS.read("{\"flag\":false}");

			assertThat(jvs.getBoolean("flag")).isFalse();
		}

		@Test
		@DisplayName("Should return false for missing path")
		void shouldReturnFalseForMissing() {
			JVS jvs = new JVS();

			assertThat(jvs.getBoolean("nonexistent")).isFalse();
		}
	}

	@Nested
	@DisplayName("isEmpty")
	class IsEmpty {

		@Test
		@DisplayName("Should return true for empty JVS")
		void shouldReturnTrueForEmpty() {
			JVS jvs = new JVS();

			assertThat(jvs.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Should return false for non-empty JVS")
		void shouldReturnFalseForNonEmpty() {
			JVS jvs = JVS.read("{\"key\":\"value\"}");

			assertThat(jvs.isEmpty()).isFalse();
		}
	}

	@Nested
	@DisplayName("remove")
	class Remove {

		@Test
		@DisplayName("Should remove field by path")
		void shouldRemoveFieldByPath() {
			JVS jvs = JVS.read("{\"a\":\"1\",\"b\":\"2\"}");

			jvs.remove("a");

			assertThat(jvs.get("a")).isNull();
			assertThat(jvs.getString("b")).isEqualTo("2");
		}
	}

	@Nested
	@DisplayName("clone")
	class Clone {

		@Test
		@DisplayName("Should create independent copy")
		void shouldCreateIndependentCopy() {
			JVS original = JVS.read("{\"key\":\"value\"}");

			JVS cloned = original.clone();
			cloned.set("key", "changed");

			assertThat(original.getString("key")).isEqualTo("value");
			assertThat(cloned.getString("key")).isEqualTo("changed");
		}
	}

	@Nested
	@DisplayName("exists")
	class Exists {

		@Test
		@DisplayName("Should return true when path exists")
		void shouldReturnTrueWhenExists() {
			JVS jvs = JVS.read("{\"a\":{\"b\":\"val\"}}");

			assertThat(jvs.exists("a.b")).isTrue();
		}

		@Test
		@DisplayName("Should return false when path does not exist")
		void shouldReturnFalseWhenNotExists() {
			JVS jvs = JVS.read("{\"a\":\"val\"}");

			assertThat(jvs.exists("b")).isFalse();
		}
	}

	@Nested
	@DisplayName("getStringList")
	class GetStringList {

		@Test
		@DisplayName("Should return string list from array")
		void shouldReturnStringList() {
			JVS jvs = JVS.read("{\"tags\":[\"a\",\"b\",\"c\"]}");

			List<String> result = jvs.getStringList("tags");

			assertThat(result).containsExactly("a", "b", "c");
		}
	}

	@Nested
	@DisplayName("getKeys")
	class GetKeys {

		@Test
		@DisplayName("Should return object keys")
		void shouldReturnObjectKeys() {
			JVS jvs = JVS.read("{\"obj\":{\"x\":1,\"y\":2,\"z\":3}}");

			List<String> keys = jvs.getKeys("obj");

			assertThat(keys).containsExactlyInAnyOrder("x", "y", "z");
		}

		@Test
		@DisplayName("Should return null for non-object")
		void shouldReturnNullForNonObject() {
			JVS jvs = JVS.read("{\"val\":\"string\"}");

			assertThat(jvs.getKeys("val")).isNull();
		}
	}

	@Nested
	@DisplayName("pathContainsKey")
	class PathContainsKey {

		@Test
		@DisplayName("Should return true when key exists in object at path")
		void shouldReturnTrueWhenKeyExists() {
			JVS jvs = JVS.read("{\"obj\":{\"target\":\"found\"}}");

			assertThat(jvs.pathContainsKey("obj", "target")).isTrue();
		}

		@Test
		@DisplayName("Should return false when key missing")
		void shouldReturnFalseWhenKeyMissing() {
			JVS jvs = JVS.read("{\"obj\":{\"other\":\"val\"}}");

			assertThat(jvs.pathContainsKey("obj", "target")).isFalse();
		}
	}

	@Nested
	@DisplayName("Comparators and Functions")
	class ComparatorsAndFunctions {

		@Test
		@DisplayName("Should have identity comparator")
		void shouldHaveIdentityComparator() {
			assertThat(JVS.identityComparator).isNotNull();
		}

		@Test
		@DisplayName("Should have key generator function")
		void shouldHaveKeyGeneratorFunction() {
			assertThat(JVS.keyGenerator).isNotNull();
		}

		@Test
		@DisplayName("Identity comparator should compare JVS objects by ID")
		void identityComparatorShouldCompareJvsObjectsById() {
			JVS jvs1 = JVS.read("{\"id\":{\"id\":\"id1\"}}");
			JVS jvs2 = JVS.read("{\"id\":{\"id\":\"id2\"}}");

			int result = JVS.identityComparator.compare(jvs1, jvs2);

			assertThat(result).isNotZero();
		}
	}

	@Nested
	@DisplayName("Merge via JVS")
	class MergeViaJVS {

		@Test
		@DisplayName("Should merge override into base")
		void shouldMergeOverrideIntoBase() {
			JVS base = JVS.read("{\"a\":\"1\"}");
			JVS override = JVS.read("{\"b\":\"2\"}");

			base.merge(override);

			assertThat(base.getString("a")).isEqualTo("1");
			assertThat(base.getString("b")).isEqualTo("2");
		}
	}

	@Nested
	@DisplayName("Variable resolution via JVS")
	class VariableResolution {

		@Test
		@DisplayName("Should resolve JSON variable")
		void shouldResolveJsonVariable() {
			JVS jvs = JVS.read("{\"name\":\"World\"}");

			String result = jvs.resolveJsonVariable("Hello ${name}");

			assertThat(result).isEqualTo("Hello World");
		}

		@Test
		@DisplayName("Should return original when no variable")
		void shouldReturnOriginalWhenNoVariable() {
			JVS jvs = JVS.read("{}");

			String result = jvs.resolveJsonVariable("Hello World");

			assertThat(result).isEqualTo("Hello World");
		}

		@Test
		@DisplayName("Should return null for null input")
		void shouldReturnNullForNullInput() {
			JVS jvs = JVS.read("{}");

			assertThat(jvs.resolveJsonVariable(null)).isNull();
		}
	}
}
