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
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.iterator.MappingIterator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GroovyTransformMapper Tests")
class GroovyTransformMapperTest {

	private static File generatorsDir;

	@BeforeAll
	static void findGeneratorsDir() {
		generatorsDir = new File("config/generators");
		if (!generatorsDir.exists()) {
			generatorsDir = new File("hitorro-util/config/generators");
		}
	}

	private DataGenerators generators() {
		return new DataGenerators(generatorsDir);
	}

	@Nested
	@DisplayName("Basic DSL operations")
	class BasicOps {

		@Test
		@DisplayName("copyAll should clone all source fields")
		void copyAll() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString(
					"copyAll()", generators());

			JVS source = JVS.read("{\"name\":\"test\",\"value\":42}");
			JVS result = mapper.apply(source);

			assertThat(result.getString("name")).isEqualTo("test");
			assertThat(result.getLong("value")).isEqualTo(42);
		}

		@Test
		@DisplayName("set should write a value to target")
		void setOperation() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString(
					"set 'target.status', 'published'", generators());

			JVS source = JVS.read("{}");
			JVS result = mapper.apply(source);

			assertThat(result.getString("status")).isEqualTo("published");
		}

		@Test
		@DisplayName("copy X to Y should transfer values")
		void copyToOperation() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					copy "source.name" to "target.title"
					""", generators());

			JVS source = JVS.read("{\"name\":\"hello\"}");
			JVS result = mapper.apply(source);

			assertThat(result.getString("title")).isEqualTo("hello");
		}

		@Test
		@DisplayName("delete should remove a field from target")
		void deleteOperation() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					copyAll()
					delete "target.secret"
					""", generators());

			JVS source = JVS.read("{\"name\":\"keep\",\"secret\":\"remove\"}");
			JVS result = mapper.apply(source);

			assertThat(result.getString("name")).isEqualTo("keep");
			assertThat(result.get("secret")).isNull();
		}

		@Test
		@DisplayName("nested path set should create structure")
		void nestedSet() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.id.domain", "synthetic"
					set "target.id.did", "abc123"
					""", generators());

			JVS source = JVS.read("{}");
			JVS result = mapper.apply(source);

			assertThat(result.getString("id.domain")).isEqualTo("synthetic");
			assertThat(result.getString("id.did")).isEqualTo("abc123");
		}
	}

	@Nested
	@DisplayName("MLS operations")
	class MlsOps {

		@Test
		@DisplayName("mls should create MLS structure")
		void mlsCreation() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					mls "target.title", text: "Hello World", lang: "en"
					""", generators());

			JVS source = JVS.read("{}");
			JVS result = mapper.apply(source);

			JsonNode titleMls = result.get("title.mls");
			assertThat(titleMls).isNotNull();
			assertThat(titleMls.isArray()).isTrue();
			assertThat(titleMls.get(0).get("text").asText()).isEqualTo("Hello World");
			assertThat(titleMls.get(0).get("lang").asText()).isEqualTo("en");
		}
	}

	@Nested
	@DisplayName("Conditional execution")
	class Conditionals {

		@Test
		@DisplayName("when(true) should execute body")
		void whenTrue() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					when(true) {
					    set "target.executed", "yes"
					}
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getString("executed")).isEqualTo("yes");
		}

		@Test
		@DisplayName("when(false) should skip body")
		void whenFalse() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					when(false) {
					    set "target.executed", "yes"
					}
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.get("executed")).isNull();
		}

		@Test
		@DisplayName("when with source field condition")
		void whenSourceField() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					when(sourceString("kind") == "article") {
					    set "target.matched", "yes"
					}
					""", generators());

			JVS article = JVS.read("{\"kind\":\"article\"}");
			JVS result1 = mapper.apply(article);
			assertThat(result1.getString("matched")).isEqualTo("yes");

			JVS other = JVS.read("{\"kind\":\"person\"}");
			JVS result2 = mapper.apply(other);
			assertThat(result2.get("matched")).isNull();
		}
	}

	@Nested
	@DisplayName("Loops")
	class Loops {

		@Test
		@DisplayName("loop over source array")
		void loopOverArray() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					loop("source.tags") { tag ->
					    append "target.categories", tag
					}
					""", generators());

			JVS source = JVS.read("{\"tags\":[\"a\",\"b\",\"c\"]}");
			JVS result = mapper.apply(source);

			JsonNode cats = result.get("categories");
			assertThat(cats.isArray()).isTrue();
			assertThat(cats.size()).isEqualTo(3);
		}

		@Test
		@DisplayName("times N loop")
		void timesLoop() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					times(3) { i ->
					    append "target.items", "item_${i}"
					}
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			JsonNode items = result.get("items");
			assertThat(items.isArray()).isTrue();
			assertThat(items.size()).isEqualTo(3);
			assertThat(items.get(0).asText()).isEqualTo("item_0");
			assertThat(items.get(2).asText()).isEqualTo("item_2");
		}
	}

	@Nested
	@DisplayName("Data generators")
	class Generators {

		@Test
		@DisplayName("gen.fullName() should produce non-empty name")
		void fullName() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.name", gen.fullName()
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			String name = result.getString("name");
			assertThat(name).isNotNull().isNotEmpty();
			assertThat(name).contains(" "); // first + space + last
		}

		@Test
		@DisplayName("gen.email() should produce valid-looking email")
		void email() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.email", gen.email()
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			String email = result.getString("email");
			assertThat(email).contains("@").contains(".");
		}

		@Test
		@DisplayName("gen.uuid() should produce unique values")
		void uuid() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.id", gen.uuid()
					""", generators());

			JVS r1 = mapper.apply(JVS.read("{}"));
			JVS r2 = mapper.apply(JVS.read("{}"));
			assertThat(r1.getString("id")).isNotEqualTo(r2.getString("id"));
		}

		@Test
		@DisplayName("gen.intBetween should produce values in range")
		void intBetween() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.val", gen.intBetween(10, 20)
					""", generators());

			for (int i = 0; i < 20; i++) {
				JVS result = mapper.apply(JVS.read("{}"));
				long val = result.getLong("val");
				assertThat(val).isBetween(10L, 20L);
			}
		}

		@Test
		@DisplayName("gen.pick should choose from options")
		void pick() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.color", gen.pick("red", "green", "blue")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getString("color")).isIn("red", "green", "blue");
		}
	}

	@Nested
	@DisplayName("Pipeline integration")
	class PipelineIntegration {

		@Test
		@DisplayName("Should work as MappingIterator mapper")
		void mappingIterator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					copyAll()
					set "target.processed", "true"
					""", generators());

			List<JVS> inputs = Arrays.asList(
					JVS.read("{\"n\":1}"),
					JVS.read("{\"n\":2}"),
					JVS.read("{\"n\":3}")
			);

			MappingIterator<JVS, JVS> iter = new MappingIterator<>(inputs.iterator(), mapper);
			List<JVS> results = new ArrayList<>();
			while (iter.hasNext()) {
				results.add(iter.next());
			}

			assertThat(results).hasSize(3);
			for (JVS r : results) {
				assertThat(r.getString("processed")).isEqualTo("true");
			}
			assertThat(results.get(0).getLong("n")).isEqualTo(1);
			assertThat(results.get(2).getLong("n")).isEqualTo(3);
		}

		@Test
		@DisplayName("Should transform 5 docs into 5 unique enriched docs")
		void multiDocTransform() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					copyAll()
					set "target.id.did", gen.uuid()
					set "target.author", gen.fullName()
					mls "target.title", text: gen.product(), lang: "en"
					""", generators());

			List<JVS> inputs = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				inputs.add(JVS.read("{\"kind\":\"article\",\"seq\":" + i + "}"));
			}

			MappingIterator<JVS, JVS> iter = new MappingIterator<>(inputs.iterator(), mapper);
			List<String> ids = new ArrayList<>();
			List<String> authors = new ArrayList<>();
			int count = 0;
			while (iter.hasNext()) {
				JVS r = iter.next();
				ids.add(r.getString("id.did"));
				authors.add(r.getString("author"));
				count++;
			}

			assertThat(count).isEqualTo(5);
			// UUIDs should all be unique
			assertThat(ids).doesNotHaveDuplicates();
		}

		@Test
		@DisplayName("docIndex should increment across calls")
		void docIndexIncrement() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					set "target.idx", docIndex
					""", generators());

			JVS r0 = mapper.apply(JVS.read("{}"));
			JVS r1 = mapper.apply(JVS.read("{}"));
			JVS r2 = mapper.apply(JVS.read("{}"));

			assertThat(r0.getLong("idx")).isEqualTo(0);
			assertThat(r1.getLong("idx")).isEqualTo(1);
			assertThat(r2.getLong("idx")).isEqualTo(2);
		}
	}

	@Nested
	@DisplayName("Complex transform scripts")
	class ComplexScripts {

		@Test
		@DisplayName("Person enrichment script")
		void personEnrichment() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					copyAll()
					set "target.kind", "person"
					set "target.id.domain", "synthetic"
					set "target.id.did", gen.uuid()
					def first = gen.firstName()
					def last = gen.lastName()
					set "target.first_name", first
					set "target.last_name", last
					mls "target.title", text: "${first} ${last}", lang: "en"
					set "target.email", gen.email()
					times(gen.intBetween(1, 3)) {
					    append "target.skills", gen.pick("Java", "Python", "Go")
					}
					""", generators());

			JVS input = JVS.read("{\"existing\":\"data\"}");
			JVS result = mapper.apply(input);

			assertThat(result.getString("kind")).isEqualTo("person");
			assertThat(result.getString("id.domain")).isEqualTo("synthetic");
			assertThat(result.getString("first_name")).isNotEmpty();
			assertThat(result.getString("last_name")).isNotEmpty();
			assertThat(result.getString("email")).contains("@");
			assertThat(result.getString("existing")).isEqualTo("data");

			JsonNode skills = result.get("skills");
			assertThat(skills.isArray()).isTrue();
			assertThat(skills.size()).isBetween(1, 3);
		}

		@Test
		@DisplayName("Conditional + loop transform")
		void conditionalLoop() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					copyAll()
					when(sourceString("kind") == "article") {
					    set "target.category", "content"
					    loop("source.tags") { tag ->
					        append "target.processed_tags", tag.asText().toUpperCase()
					    }
					}
					when(sourceString("kind") != "article") {
					    set "target.category", "other"
					}
					""", generators());

			JVS article = JVS.read("{\"kind\":\"article\",\"tags\":[\"search\",\"ml\"]}");
			JVS result1 = mapper.apply(article);
			assertThat(result1.getString("category")).isEqualTo("content");
			assertThat(result1.get("processed_tags").get(0).asText()).isEqualTo("SEARCH");
			assertThat(result1.get("processed_tags").get(1).asText()).isEqualTo("ML");

			JVS doc = JVS.read("{\"kind\":\"document\"}");
			JVS result2 = mapper.apply(doc);
			assertThat(result2.getString("category")).isEqualTo("other");
		}
	}
}
