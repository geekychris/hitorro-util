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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Conditional and Chained Generator Tests")
class ConditionalGeneratorTest {

	@Nested
	@DisplayName("Conditional generator")
	class Conditional {

		@Test
		@DisplayName("Should select generator based on condition")
		void selectByCondition() {
			Generator highSalary = Generators.randomInt(100000, 200000);
			Generator lowSalary = Generators.randomInt(30000, 60000);

			Generator conditional = Generators.conditional(
					() -> true, highSalary, lowSalary);

			int val = (int) conditional.next();
			assertThat(val).isBetween(100000, 200000);
		}

		@Test
		@DisplayName("Should use else branch when condition is false")
		void elseBranch() {
			Generator conditional = Generators.conditional(
					() -> false,
					Generators.constant("YES"),
					Generators.constant("NO"));

			assertThat(conditional.nextString()).isEqualTo("NO");
		}
	}

	@Nested
	@DisplayName("Chained / transformed generators")
	class Chained {

		@Test
		@DisplayName("Should transform generator output")
		void transform() {
			Generator base = Generators.items("hello", "world");
			Generator upper = Generators.transform(base, s -> s.toString().toUpperCase());

			assertThat(upper.nextString()).isEqualTo("HELLO");
			assertThat(upper.nextString()).isEqualTo("WORLD");
		}

		@Test
		@DisplayName("Should chain generators with format string")
		void formatChain() {
			Generator first = Generators.items("John");
			Generator last = Generators.items("Doe");
			Generator formatted = Generators.format("%s %s", first, last);

			assertThat(formatted.nextString()).isEqualTo("John Doe");
		}
	}

	@Nested
	@DisplayName("Weighted pick")
	class WeightedPick {

		@Test
		@DisplayName("Heavily weighted option should appear most often")
		void heavilyWeighted() {
			Generator gen = Generators.weightedPick(
					new String[]{"common", "rare"},
					new double[]{99.0, 1.0});

			int commonCount = 0;
			for (int i = 0; i < 100; i++) {
				if ("common".equals(gen.nextString())) commonCount++;
			}
			assertThat(commonCount).isGreaterThan(80);
		}
	}
}
