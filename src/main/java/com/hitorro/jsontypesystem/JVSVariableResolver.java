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
import com.fasterxml.jackson.databind.node.TextNode;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

public class JVSVariableResolver {

	public static final JsonNode resolveVariableAux(TextNode e, JVS master) throws PropaccessError {
		String value = e.textValue();
		JsonNode n = resolveTextVariableAux(value, master, null);
		if (n == null) {
			return e;
		}
		return n;
	}

	/**
	 * Resolve a string that contains a HiTorro variable
	 */
	public static final JsonNode resolveTextVariableAux(String value, JVS master, JVS overide) throws PropaccessError {
		StringBuilder builder = new StringBuilder();

		if (value == null) {
			Log.util.error("Not given a value to resolve");
			return null;
		}
		int index = value.indexOf(JVS.VariableStart);
		if (index == -1) {
			return null;
		}
		int lastPos = 0;
		int endIndex = 0;
		while (index != -1) {
			builder.append(value, lastPos, index);
			endIndex = value.indexOf(JVS.VariableEnd, index + 1);
			if (endIndex == -1) {
				break;
			} else {
				String variable = value.substring(index + 2, endIndex).toLowerCase();

				String substValue;

				JsonNode res = null;
				if (overide != null) {
					res = overide.get(variable);
				}
				if (res == null) {
					res = master.get(variable);
				}
				if (res != null) {
					if (!res.isTextual()) {
						return res;
					}
					substValue = res.textValue();
					builder.append(substValue);
				}
				lastPos = endIndex + 1;
				index = value.indexOf(JVS.VariableStart, lastPos + 1);
			}
		}
		// append tail
		builder.append(value.substring(lastPos));
		String txt = builder.toString();
		return JsonNodeFactory.instance.textNode(txt);
	}

	public static void resolveVariables(JVS master, JsonNode node) throws PropaccessError {
		if (node.isObject()) {
			ObjectNode on = (ObjectNode) node;

			String keys[] = new String[on.size()];
			JsonNode val[] = new JsonNode[on.size()];
			JSONUtil.populateKeyValue(on, keys, val);
			int size = keys.length;
			for (int i = 0; i < size; i++) {
				if (val[i].isObject() || val[i].isArray()) {
					resolveVariables(master, val[i]);
				} else if (val[i].isTextual()) {
					JsonNode res = resolveVariableAux((TextNode) val[i], master);
					if (val[i] != res) {
						on.put(keys[i], res);
					}
				}
			}
		} else if (node.isArray()) {
			ArrayNode ar = (ArrayNode) node;
			int size = ar.size();
			for (int i = 0; i < size; i++) {
				JsonNode e = ar.get(i);
				if (e.isTextual()) {
					JsonNode res = resolveVariableAux((TextNode) e, master);
					if (e != res) {
						ar.set(i, res);
					}
				} else if (e.isArray() || e.isObject()) {
					resolveVariables(master, e);
				}
			}
		}
	}
}
