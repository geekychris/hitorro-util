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
package com.hitorro.jsontypesystem.schema;

public class HiTorroSchemaConstants {
	public static final String SCHEMA_URI = "https://json-schema.org/draft/2020-12/schema";

	// Extension keys applied to schema root
	public static final String X_HITORRO_NAME = "x-hitorro-name";
	public static final String X_HITORRO_SUPER = "x-hitorro-super";
	public static final String X_HITORRO_PRIMITIVETYPE = "x-hitorro-primitivetype";
	public static final String X_HITORRO_INDEXSEEKER = "x-hitorro-indexseeker";
	public static final String X_HITORRO_FETCHLANG = "x-hitorro-fetchlang";

	// Extension keys applied to properties
	public static final String X_HITORRO_DYNAMIC = "x-hitorro-dynamic";
	public static final String X_HITORRO_GROUPS = "x-hitorro-groups";
	public static final String X_HITORRO_I18N = "x-hitorro-i18n";
}
