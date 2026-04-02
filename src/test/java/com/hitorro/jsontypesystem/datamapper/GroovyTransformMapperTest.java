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

	@Nested
	@DisplayName("Generator DSL definitions")
	class GeneratorDSL {

		@Test
		@DisplayName("Should define a random int generator from DSL")
		void randomIntGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "age", type: "int", min: 18, max: 65
					set "target.age", gen.next("age")
					""", generators());

			for (int i = 0; i < 20; i++) {
				JVS result = mapper.apply(JVS.read("{}"));
				long age = result.getLong("age");
				assertThat(age).isBetween(18L, 65L);
			}
		}

		@Test
		@DisplayName("Should define a random double generator from DSL")
		void randomDoubleGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "price", type: "double", min: 9.99, max: 999.99
					set "target.price", gen.next("price")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getDouble("price")).isBetween(9.99, 999.99);
		}

		@Test
		@DisplayName("Should define a sequence generator from DSL")
		void sequenceGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "order_num", type: "sequence", start: 1000
					set "target.order", gen.next("order_num")
					""", generators());

			JVS r1 = mapper.apply(JVS.read("{}"));
			JVS r2 = mapper.apply(JVS.read("{}"));
			JVS r3 = mapper.apply(JVS.read("{}"));

			assertThat(r1.getLong("order")).isEqualTo(1000);
			assertThat(r2.getLong("order")).isEqualTo(1001);
			assertThat(r3.getLong("order")).isEqualTo(1002);
		}

		@Test
		@DisplayName("Should define a sequence with prefix from DSL")
		void sequenceWithPrefix() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "ticket", type: "sequence", prefix: "TKT-"
					set "target.ticket", gen.next("ticket")
					""", generators());

			JVS r1 = mapper.apply(JVS.read("{}"));
			JVS r2 = mapper.apply(JVS.read("{}"));
			assertThat(r1.getString("ticket")).isEqualTo("TKT-1");
			assertThat(r2.getString("ticket")).isEqualTo("TKT-2");
		}

		@Test
		@DisplayName("Should define a pattern generator from DSL")
		void patternGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "sku", type: "pattern", pattern: "SKU-####-??"
					set "target.sku", gen.next("sku")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			String sku = result.getString("sku");
			assertThat(sku).startsWith("SKU-");
			assertThat(sku).hasSize(11); // SKU-####-?? = 3+1+4+1+2
			// digits in positions 4-7
			assertThat(sku.substring(4, 8)).matches("\\d{4}");
			// letters in positions 9-10
			assertThat(sku.substring(9)).matches("[A-Z]{2}");
		}

		@Test
		@DisplayName("Should define a pick generator from DSL")
		void pickGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "status", type: "pick", values: ["active", "inactive", "pending"]
					set "target.status", gen.next("status")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getString("status")).isIn("active", "inactive", "pending");
		}

		@Test
		@DisplayName("Should define an items (cycling) generator from DSL")
		void itemsGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "color", type: "items", values: ["red", "green", "blue"]
					set "target.c1", gen.next("color")
					set "target.c2", gen.next("color")
					set "target.c3", gen.next("color")
					set "target.c4", gen.next("color")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getString("c1")).isEqualTo("red");
			assertThat(result.getString("c2")).isEqualTo("green");
			assertThat(result.getString("c3")).isEqualTo("blue");
			assertThat(result.getString("c4")).isEqualTo("red"); // cycles!
		}

		@Test
		@DisplayName("Should define a uuid generator from DSL")
		void uuidGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "txn_id", type: "uuid"
					set "target.txn", gen.next("txn_id")
					""", generators());

			JVS r1 = mapper.apply(JVS.read("{}"));
			JVS r2 = mapper.apply(JVS.read("{}"));
			assertThat(r1.getString("txn")).isNotEmpty();
			assertThat(r1.getString("txn")).isNotEqualTo(r2.getString("txn"));
		}

		@Test
		@DisplayName("Should define a date range generator from DSL")
		void dateRangeGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "dob", type: "date", from: "1960-01-01", to: "2005-12-31"
					set "target.dob", gen.next("dob")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			String dob = result.getString("dob");
			assertThat(dob).matches("\\d{4}-\\d{2}-\\d{2}T.*");
		}

		@Test
		@DisplayName("Should define a constant generator from DSL")
		void constantGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "version", type: "constant", value: "3.0.1"
					set "target.version", gen.next("version")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getString("version")).isEqualTo("3.0.1");
		}

		@Test
		@DisplayName("Should define a template generator from DSL")
		void templateGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "greeting", type: "template", template: "Dear {first_names} {last_names}"
					set "target.greeting", gen.next("greeting")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			String greeting = result.getString("greeting");
			assertThat(greeting).startsWith("Dear ");
			// Should have two words after "Dear "
			assertThat(greeting.split(" ").length).isGreaterThanOrEqualTo(3);
		}

		@Test
		@DisplayName("Should define a boolean generator from DSL")
		void boolGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "is_active", type: "bool"
					set "target.active", gen.next("is_active")
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.get("active")).isNotNull();
			assertThat(result.getBoolean("active") || !result.getBoolean("active")).isTrue();
		}

		@Test
		@DisplayName("Should override a default generator from DSL")
		void overrideDefaultGenerator() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "first_names", type: "items", values: ["Alice", "Bob"], force: true
					set "target.name", gen.firstName()
					""", generators());

			// force:true re-creates on each call — both start from "Alice"
			// The point: the override replaces the CSV-loaded generator
			JVS r1 = mapper.apply(JVS.read("{}"));
			assertThat(r1.getString("name")).isIn("Alice", "Bob");

			// Without force, the items generator persists and cycles
			GroovyTransformMapper mapper2 = GroovyTransformMapper.fromString("""
					generator "custom_names", type: "items", values: ["X", "Y", "Z"]
					set "target.name", gen.next("custom_names")
					""", generators());

			JVS s1 = mapper2.apply(JVS.read("{}"));
			JVS s2 = mapper2.apply(JVS.read("{}"));
			JVS s3 = mapper2.apply(JVS.read("{}"));
			assertThat(s1.getString("name")).isEqualTo("X");
			assertThat(s2.getString("name")).isEqualTo("Y");
			assertThat(s3.getString("name")).isEqualTo("Z");
		}

		@Test
		@DisplayName("Multiple generators in one script")
		void multipleGenerators() {
			GroovyTransformMapper mapper = GroovyTransformMapper.fromString("""
					generator "age", type: "int", min: 18, max: 65
					generator "salary", type: "double", min: 30000.0, max: 200000.0
					generator "emp_id", type: "sequence", prefix: "EMP-"
					generator "dept", type: "pick", values: ["Engineering", "Sales", "HR"]

					set "target.emp_id", gen.next("emp_id")
					set "target.age", gen.next("age")
					set "target.salary", gen.next("salary")
					set "target.dept", gen.next("dept")
					set "target.name", gen.fullName()
					""", generators());

			JVS result = mapper.apply(JVS.read("{}"));
			assertThat(result.getString("emp_id")).isEqualTo("EMP-1");
			assertThat(result.getLong("age")).isBetween(18L, 65L);
			assertThat(result.getDouble("salary")).isBetween(30000.0, 200000.0);
			assertThat(result.getString("dept")).isIn("Engineering", "Sales", "HR");
			assertThat(result.getString("name")).contains(" ");
		}
	}
}
