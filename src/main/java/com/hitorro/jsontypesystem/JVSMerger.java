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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.json.JSONUtil;

public class JVSMerger {

	public static void merge(JsonNode base, JsonNode overide) {
		if (overide.isObject()) {
			ObjectNode on = (ObjectNode) overide;

			String[] keys = new String[on.size()];
			JsonNode[] val = new JsonNode[on.size()];
			JSONUtil.populateKeyValue(on, keys, val);
			int size = keys.length;
			for (int i = 0; i < size; i++) {
				if (val[i].isObject() || val[i].isArray()) {
					JsonNode baseChild = base.get(keys[i]);
					if (baseChild != null) {
						merge(baseChild, val[i]);
					} else {
						((ObjectNode) base).put(keys[i], val[i]);
					}
				} else {
					((ObjectNode) base).put(keys[i], val[i]);
				}
			}
		} else if (overide.isArray()) {
			ArrayNode ar = (ArrayNode) overide;
			ArrayNode tar = (ArrayNode) base;
			if (tar.size() < ar.size()) {
				for (int j = tar.size(); j < ar.size(); j++) {
					tar.add(JsonNodeFactory.instance.nullNode());
				}
			}
			int size = ar.size();
			for (int i = 0; i < size; i++) {
				JsonNode e = ar.get(i);

				if (e.isArray() || e.isObject()) {
					JsonNode baseChild = tar.get(i);
					if (baseChild == null) {
						tar.set(i, e);
					} else {
						merge(baseChild, e);
					}
				} else {
					tar.set(i, e);
				}
			}
		}
	}
}
