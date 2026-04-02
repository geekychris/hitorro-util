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
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.date.DateResolution;
import com.hitorro.util.core.params.JsonKeyMap;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.String2JsonMapper;
import com.hitorro.util.json.keys.JsonInitableProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PAContext;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.json.keys.propaccess.PropaccessIterator;
import com.hitorro.util.json.keys.propaccess.VS;
import com.hitorro.util.json.mapper.Json2StringMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class JVS implements VS {

	// Well-known keys
	public static final StringProperty typeKey = new StringProperty("type", "", null);
	public static final Propaccess didKey = new Propaccess("id.did");
	public static final Propaccess idKey = new Propaccess("id.id");
	public static final Propaccess createdKey = new Propaccess("times.created");
	public static final Propaccess modifiedKey = new Propaccess("times.modified");
	public static final Propaccess titleKey = new Propaccess("title.mls");
	public static final Propaccess bodyKey = new Propaccess("body.mls");
	public static final Propaccess domainKey = new Propaccess("id.domain");
	public static final Propaccess docKey = new Propaccess("doc");

	// Variable resolution constants
	public static final String VariableStart = "${";
	public static final String VariableEnd = "}";

	// Comparators and functions
	public static Comparator<JVS> identityComparator = (o1, o2) -> o1.getId().compareTo(o2.getId());
	public static Function<JVS, String> keyGenerator = JVS::getId;

	// Propaccess cache for string-path methods
	private static final ConcurrentHashMap<String, Propaccess> paCache = new ConcurrentHashMap<>();

	private JsonNode root;
	private Type type;
	private PAContext propaccessContext = PAContext.AlwaysCreate;

	public JVS(JsonNode root) {
		this.root = root;
		String typeString = typeKey.apply(root);
		if (StringUtil.nullOrEmptyString(typeString)) {
			return;
		}
		this.type = JsonTypeSystem.getMe().getType(typeKey.apply(root));
		setTypeAux(type);
	}

	public JVS() {
		this(JsonNodeFactory.instance.objectNode());
	}

	public JVS(Type type) {
		root = JsonNodeFactory.instance.objectNode();
		try {
			set(typeKey, type.getName());
		} catch (PropaccessError propaccessError) {
			Log.util.error("Failed to set type on new JVS", propaccessError);
		}
	}

	private static Propaccess cachedPropaccess(String path) {
		Propaccess cached = paCache.get(path);
		if (cached == null) {
			cached = new Propaccess(path);
			paCache.put(path, cached);
		}
		return new Propaccess(cached);
	}

	public static JVS read(String json) {
		String2JsonMapper mapper = new String2JsonMapper();
		return new JVS(mapper.apply(json));
	}

	public static JVS read(File file) throws Exception {
		return new JVS(FileUtil.fsStaxJsonIter.apply(file).getFirstItemAndClose());
	}

	public static JVS read(BaseFile bf) throws Exception {
		JsonNode jn = bf.getJsonNode();
		if (jn == null) {
			return null;
		}
		return new JVS(jn);
	}

	public JVS getJVSChild(Propaccess pa) {
		JsonNode node = get(pa);
		if (node != null) {
			return new JVS(node);
		}
		return null;
	}

	public JVS getDoc() {
		return getJVSChild(docKey);
	}

	public void setDoc(JVS doc) {
		setJVSChild(docKey, doc);
	}

	public void setJVSChild(Propaccess pa, JVS doc) {
		set(pa, doc.getJsonNode());
	}

	public JsonNode getJsonNode() {
		return root;
	}

	public void clear() {
		root = JsonNodeFactory.instance.objectNode();
	}

	public final String resolveJsonVariable(String value, JVS overide) {
		if (value == null) {
			return null;
		}
		try {
			JsonNode n = JVSVariableResolver.resolveTextVariableAux(value, this, overide);
			if (n == null) {
				return value;
			}
			return n.textValue();
		} catch (PropaccessError propaccessError) {
			return null;
		}
	}

	public final String resolveJsonVariable(String value) {
		return resolveJsonVariable(value, null);
	}

	public JVS clone() {
		return new JVS(root.deepCopy());
	}

	public List<JsonKeyMap> getSubMaps(String root) throws PropaccessError {
		List<JsonKeyMap> list = new ArrayList();
		JsonNode childKeys = get(root);
		Iterator<Map.Entry<String, JsonNode>> iter = childKeys.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> e = iter.next();
			list.add(new JsonKeyMap(e.getKey(), e.getValue()));
		}
		return list;
	}

	public String getId() throws PropaccessError {
		return getString(idKey);
	}

	private void setTypeAux(final Type type) {
		this.type = type;
		if (type != null) {
			propaccessContext = type.getPaContext();
		}
	}

	/**
	 * @deprecated Use {@link #addLangText(Propaccess, String, String)} instead.
	 *             The append issue for empty MLS arrays has been fixed in PAContextTyped.
	 */
	@Deprecated
	public JVS addLangTextTemporaryReLook(Propaccess field, String text, String lang) {
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("text", text).put("lang", lang);
		ArrayNode an = JsonNodeFactory.instance.arrayNode();
		an.add(node);
		set(field, an);
		return this;
	}

	public JVS addLangText(Propaccess field, String text, String lang) throws PropaccessError {
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("text", text).put("lang", lang);
		append(field, node);
		return this;
	}

	public JVS setDates(Date published, Date modified) throws PropaccessError {
		set(createdKey, DateResolution.json.getFormatted(published));
		set(modifiedKey, DateResolution.json.getFormatted(modified));
		return this;
	}

	public Type getType() {
		return type;
	}

	public JVS setType(String type) {
		return setType(JsonTypeSystem.getMe().getType(type));
	}

	public JVS setType(Type type) throws PropaccessError {
		set(typeKey, type.getName());
		setTypeAux(type);
		return this;
	}

	public JVS setId(String domain, String id) throws PropaccessError {
		set(domainKey, domain);
		set(didKey, id);
		return this;
	}

	public JVS overidePaContext(PAContext access) {
		this.propaccessContext = access;
		return this;
	}

	public boolean exists(String path) throws PropaccessError {
		return exists(cachedPropaccess(path));
	}

	public boolean exists(Propaccess path) throws PropaccessError {
		return get(path) != null;
	}

	public JsonNode get(String m, String... path) throws PropaccessError {
		return get(Fmt.S(m, path));
	}

	public JsonNode get(String path) throws PropaccessError {
		if (path == null) {
			return null;
		}
		return get(cachedPropaccess(path));
	}

	public int getSize(String path) throws PropaccessError {
		return getSize(cachedPropaccess(path));
	}

	public int getSize(Propaccess pa) throws PropaccessError {
		return pa.getSize(this, root, propaccessContext);
	}

	public JsonNode get(Propaccess pa) throws PropaccessError {
		if (pa == null) {
			return null;
		}
		return pa.get(this, root, propaccessContext);
	}

	public <E> E getObject(Propaccess pa, JsonInitableProperty<E> initableProp) {
		JsonNode n = get(pa);
		return initableProp.apply(n);
	}

	public JVS set(String path, Object value) throws PropaccessError {
		set(cachedPropaccess(path), value);
		return this;
	}

	public JVS setIfNotNull(Propaccess path, Object value) throws PropaccessError {
		if (value == null) {
			return this;
		}
		path.set(this, root, propaccessContext, JSONUtil.ensureJsonNode(value));
		return this;
	}

	public JVS set(Propaccess path, Object value) throws PropaccessError {
		path.set(this, root, propaccessContext, JSONUtil.ensureJsonNode(value));
		return this;
	}

	public JVS append(Propaccess path, Object value) throws PropaccessError {
		path.appendObject(this, root, propaccessContext, JSONUtil.ensureJsonNode(value));
		return this;
	}

	public JVS set(Propaccess path, int depth, JsonNode value) throws PropaccessError {
		path.set(this, root, propaccessContext, value, depth);
		return this;
	}

	public String getString(String m, String... path) throws PropaccessError {
		return getString(Fmt.S(m, path));
	}

	public String getString(String path) throws PropaccessError {
		return getString(cachedPropaccess(path));
	}

	public String getString(Propaccess path) throws PropaccessError {
		return JSONUtil.getString(get(path));
	}

	public boolean getBoolean(String path) throws PropaccessError {
		return getBoolean(cachedPropaccess(path));
	}

	public boolean getBoolean(Propaccess path) throws PropaccessError {
		JsonNode node = get(path);
		if (node == null || node.isNull()) {
			return false;
		}
		return node.asBoolean();
	}

	public Date getDate(String path) throws PropaccessError {
		return getDate(cachedPropaccess(path));
	}

	public Date getDate(Propaccess path) throws PropaccessError {
		return JSONUtil.getDate(get(path));
	}

	public List<String> getStringList(String path) throws PropaccessError {
		return getStringList(cachedPropaccess(path));
	}

	public List<String> getStringList(Propaccess path) throws PropaccessError {
		return JSONUtil.getStringList(get(path));
	}

	public double getDouble(String path) throws PropaccessError {
		return getDouble(cachedPropaccess(path));
	}

	public double getDouble(Propaccess path) throws PropaccessError {
		return JSONUtil.getDouble(get(path), 0.0);

	}

	public long getLong(String path) throws PropaccessError {
		return getLong(cachedPropaccess(path));
	}

	public long getLong(Propaccess path) throws PropaccessError {
		return JSONUtil.getLong(get(path), 0);
	}

	public long[] getLongArray(String path) throws PropaccessError {
		return getLongArray(cachedPropaccess(path));
	}

	public long[] getLongArray(Propaccess path) throws PropaccessError {
		return JSONUtil.getLongArray(get(path));
	}

	public boolean pathContainsKey(String path, String key) throws PropaccessError {
		return pathContainsKey(cachedPropaccess(path), key);
	}

	public boolean pathContainsKey(Propaccess path, String key) throws PropaccessError {
		JsonNode node = get(path);
		if (node == null || !node.isObject()) {
			return false;
		}
		ObjectNode on = (ObjectNode) node;
		return on.has(key);
	}

	public List<String> getKeys(String path) throws PropaccessError {
		return getKeys(cachedPropaccess(path));
	}

	public List<String> getKeys(Propaccess path) throws PropaccessError {
		JsonNode node = get(path);
		if (node == null || !node.isObject()) {
			return null;
		}
		List<String> list = new ArrayList();
		ObjectNode on = (ObjectNode) node;
		Iterator<String> iter = on.fieldNames();
		while (iter.hasNext()) {
			list.add(iter.next());
		}
		return list;
	}

	public boolean isEmpty() {
		return !root.fieldNames().hasNext();
	}

	public JVS remove(String path) throws PropaccessError {
		set(cachedPropaccess(path), (Object) null);
		return this;
	}

	public JVS remove(Propaccess path) throws PropaccessError {
		set(path, (Object) null);
		return this;
	}

	public String getStringRepresentation() {
		return Json2StringMapper.threadedMapper.get().apply(root);
	}

	public PropaccessIterator getPropertyIter() {
		return new PropaccessIterator(root);
	}

	public Properties getAsProperties() throws PropaccessError {
		Properties props = new Properties();
		PropaccessIterator iter = getPropertyIter();
		while (iter.hasNext()) {
			Propaccess acc = iter.next();
			String s = getString(acc);
			props.put(acc.toString(), s);
		}
		return props;
	}

	public void write(BaseFile bf) throws IOException {
		bf.writeJson(root);
	}

	public void resolveVariables(JVS master) throws PropaccessError {
		JVSVariableResolver.resolveVariables(master, root);
	}

	public void addMap(Map<String, String> map) {
		JVSUtils.convertMapToJVS(map, this);
	}

	public void merge(JVS overide) {
		JVSMerger.merge(this.root, overide.root);
	}
}
