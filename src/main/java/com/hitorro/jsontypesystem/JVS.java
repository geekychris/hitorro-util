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
import com.hitorro.util.json.keys.propaccess.*;
import com.hitorro.util.json.mapper.Json2StringMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class JVS implements com.hitorro.util.json.keys.propaccess.VS {
    public static final StringProperty typeKey = new StringProperty("type", "", null);
    public static final com.hitorro.util.json.keys.propaccess.Propaccess didKey = new com.hitorro.util.json.keys.propaccess.Propaccess("id.did");
    public static final com.hitorro.util.json.keys.propaccess.Propaccess idKey = new com.hitorro.util.json.keys.propaccess.Propaccess("id.id");
    public static final com.hitorro.util.json.keys.propaccess.Propaccess createdKey = new com.hitorro.util.json.keys.propaccess.Propaccess("times.created");
    public static final String VariableEnd = "}";
    public static final com.hitorro.util.json.keys.propaccess.Propaccess modifiedKey = new com.hitorro.util.json.keys.propaccess.Propaccess("times.modified");
    public static final com.hitorro.util.json.keys.propaccess.Propaccess titleKey = new com.hitorro.util.json.keys.propaccess.Propaccess("title.mls");
    public static final com.hitorro.util.json.keys.propaccess.Propaccess bodyKey = new com.hitorro.util.json.keys.propaccess.Propaccess("body.mls" +
            "");
    public static final com.hitorro.util.json.keys.propaccess.Propaccess domainKey = new com.hitorro.util.json.keys.propaccess.Propaccess("id.domain");
    public static final String VariableStart = "${";
    public static com.hitorro.util.json.keys.propaccess.Propaccess docKey = new com.hitorro.util.json.keys.propaccess.Propaccess("doc");
    public static Comparator<JVS> identityComparator = new Comparator<JVS>() {
        @Override
        public int compare(final JVS o1, final JVS o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
    public static Function<JVS, String> keyGenerator = new Function<JVS, String>() {
        @Override
        public String apply(final JVS o) {
            return o.getId();
        }
    };
    private JsonNode root;
    private Type type;
    // this should be an accessor that uses dynamic fields
    private com.hitorro.util.json.keys.propaccess.PAContext propaccessContext = com.hitorro.util.json.keys.propaccess.PAContext.AlwaysCreate;

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
        } catch (com.hitorro.util.json.keys.propaccess.PropaccessError propaccessError) {
            //
        }
    }

    public static JVS read(String json) {
        String2JsonMapper mapper = new String2JsonMapper();
        return new JVS(mapper.apply(json));
    }

    public static final JsonNode resolveVariableAux(TextNode e, JVS master) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        String value = e.textValue();
        JsonNode n = resolveTextVariableAux(value, master, null);
        if (n == null) {
            return e;
        }
        return n;
    }

    /**
     * Resolve a string that contains a HiTorro variable
     *
     * @return
     */
    public static final JsonNode resolveTextVariableAux(String value, JVS master, JVS overide) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        StringBuilder builder = new StringBuilder();

        if (value == null) {
            Log.util.error("Not given a value to resolve");
            return null;
        }
        int index = value.indexOf(VariableStart);
        if (index == -1) {
            return null;
        }
        int lastPos = 0;
        int endIndex = 0;
        while (index != -1) {
            builder.append(value, lastPos, index);
            endIndex = value.indexOf(VariableEnd, index + 1);
            if (endIndex == -1) {
                // error case, we dont have a matching }
                // is break the correct thing?
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
                        //XXX should probably have a check to make sure its the only variable
                        return res;
                    }
                    substValue = res.textValue();
                    builder.append(substValue);
                }
                lastPos = endIndex + 1;
                index = value.indexOf(VariableStart, lastPos + 1);
            }
        }
        // append tail
        builder.append(value.substring(lastPos));
        String txt = builder.toString();
        return JsonNodeFactory.instance.textNode(txt);
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

    public static void resolveVariables(JVS master, JsonNode node) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
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
                        ar.set(i, e);
                    }
                } else if (e.isArray() || e.isObject()) {
                    resolveVariables(master, e);
                }
            }
        }
    }

    public JVS getJVSChild(com.hitorro.util.json.keys.propaccess.Propaccess pa) {
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

    public void setJVSChild(com.hitorro.util.json.keys.propaccess.Propaccess pa, JVS doc) {
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
        String v = null;
        try {
            JsonNode n = JVS.resolveTextVariableAux(value, this, overide);
            if (n == null) {
                return value;
            }
            return n.textValue();
        } catch (com.hitorro.util.json.keys.propaccess.PropaccessError propaccessError) {
            return null;
        }
    }

    public final String resolveJsonVariable(String value) {
        return resolveJsonVariable(value, null);
    }

    public JVS clone() {
        return new JVS(root.deepCopy());
    }

    public List<JsonKeyMap> getSubMaps(String root) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        List<JsonKeyMap> list = new ArrayList();
        JsonNode childKeys = get(root);
        Iterator<Map.Entry<String, JsonNode>> iter = childKeys.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> e = iter.next();
            list.add(new JsonKeyMap(e.getKey(), e.getValue()));
        }
        return list;
    }

    public String getId() throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getString(idKey);
    }

    private void setTypeAux(final Type type) {
        this.type = type;
        if (type != null) {
            propaccessContext = type.getPaContext();
        }
    }

    public void addLangTextTemporaryReLook(com.hitorro.util.json.keys.propaccess.Propaccess field, String text, String lang) {
        // the addLangText doesnt work....seems dooing a title.mls[] append does not append correctly
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("text", text).put("lang", lang);
        ArrayNode an = JsonNodeFactory.instance.arrayNode();
        an.add(node);
        set(field, an);
    }

    public void addLangText(com.hitorro.util.json.keys.propaccess.Propaccess field, String text, String lang) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("text", text).put("lang", lang);
        append(field, node);
    }

    public void setDates(Date published, Date modified) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        set(createdKey, DateResolution.json.getFormatted(published));
        set(modifiedKey, DateResolution.json.getFormatted(modified));
    }

    public Type getType() {
        return type;
    }

    public JVS setType(String type) {
        return setType(JsonTypeSystem.getMe().getType(type));
    }

    public JVS setType(Type type) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        set(typeKey, type.getName());
        setTypeAux(type);
        return this;
    }

    public void setId(String domain, String id) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        set(domainKey, domain);
        set(didKey, id);
    }

    /**
     * Overide the access compContext with one other than the type accessor
     *
     * @param access
     */
    public void overidePaContext(com.hitorro.util.json.keys.propaccess.PAContext access) {
        this.propaccessContext = access;
    }

    public boolean exists(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return exists(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public boolean exists(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return get(path) != null;
    }

    public JsonNode get(String m, String... path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return get(Fmt.S(m, path));
    }

    public JsonNode get(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        if (path == null) {
            return null;
        }
        return get(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public int getSize(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getSize(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public int getSize(com.hitorro.util.json.keys.propaccess.Propaccess pa) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return pa.getSize(this, root, propaccessContext);
    }

    public JsonNode get(com.hitorro.util.json.keys.propaccess.Propaccess pa) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        if (pa == null) {
            return null;
        }
        return pa.get(this, root, propaccessContext);
    }

    public <E> E getObject(com.hitorro.util.json.keys.propaccess.Propaccess pa, JsonInitableProperty<E> initableProp) {
        JsonNode n = get(pa);
        return initableProp.apply(n);
    }

    public JVS set(String path, Object value) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        set(new com.hitorro.util.json.keys.propaccess.Propaccess(path), value);
        return this;
    }

    public JVS setIfNotNull(com.hitorro.util.json.keys.propaccess.Propaccess path, Object value) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        if (value == null) {
            return this;
        }
        path.set(this, root, propaccessContext, JSONUtil.ensureJsonNode(value));
        return this;
    }

    public JVS set(com.hitorro.util.json.keys.propaccess.Propaccess path, Object value) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        path.set(this, root, propaccessContext, JSONUtil.ensureJsonNode(value));
        return this;
    }

    public JVS append(com.hitorro.util.json.keys.propaccess.Propaccess path, Object value) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        path.appendObject(this, root, propaccessContext, JSONUtil.ensureJsonNode(value));
        return this;
    }

    public JVS set(com.hitorro.util.json.keys.propaccess.Propaccess path, int depth, JsonNode value) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        path.set(this, root, propaccessContext, value, depth);
        return this;
    }

    public String getString(String m, String... path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getString(Fmt.S(m, path));
    }

    public String getString(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getString(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public String getString(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return JSONUtil.getString(get(path));
    }

    public Date getDate(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getDate(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public Date getDate(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return JSONUtil.getDate(get(path));
    }

    public List<String> getStringList(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getStringList(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public List<String> getStringList(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return JSONUtil.getStringList(get(path));
    }

    public double getDouble(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getDouble(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public double getDouble(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return JSONUtil.getDouble(get(path), 0.0);

    }

    public long getLong(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getLong(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public long getLong(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return JSONUtil.getLong(get(path), 0);
    }

    public long[] getLongArray(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getLongArray(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public long[] getLongArray(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return JSONUtil.getLongArray(get(path));
    }

    public boolean pathContainsKey(String path, String key) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return pathContainsKey(new com.hitorro.util.json.keys.propaccess.Propaccess(path), key);
    }

    public boolean pathContainsKey(com.hitorro.util.json.keys.propaccess.Propaccess path, String key) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        JsonNode node = get(path);
        if (node == null || !node.isObject()) {
            return false;
        }
        ObjectNode on = (ObjectNode) node;
        return on.has(key);
    }

    public List<String> getKeys(String path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        return getKeys(new com.hitorro.util.json.keys.propaccess.Propaccess(path));
    }

    public List<String> getKeys(com.hitorro.util.json.keys.propaccess.Propaccess path) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
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

    public String getStringRepresentation() {
        return Json2StringMapper.threadedMapper.get().apply(root);
    }

    public com.hitorro.util.json.keys.propaccess.PropaccessIterator getPropertyIter() {
        return new com.hitorro.util.json.keys.propaccess.PropaccessIterator(root);
    }

    public Properties getAsProperties() throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        Properties props = new Properties();
        com.hitorro.util.json.keys.propaccess.PropaccessIterator iter = getPropertyIter();
        while (iter.hasNext()) {
            com.hitorro.util.json.keys.propaccess.Propaccess acc = iter.next();
            String s = getString(acc);
            props.put(acc.toString(), s);
        }
        return props;
    }

    public void write(BaseFile bf) throws IOException {
        bf.writeJson(root);
    }

    public void resolveVariables(JVS master) throws com.hitorro.util.json.keys.propaccess.PropaccessError {
        resolveVariables(master, root);
    }

    public void addMap(Map<String, String> map) {
        JVSUtils.convertMapToJVS(map, this);
    }

    public void merge(JVS overide) {
        merge(this.root, overide.root);
    }


    private void merge(JsonNode base, JsonNode overide) {
        if (overide.isObject()) {
            ObjectNode on = (ObjectNode) overide;

            String keys[] = new String[on.size()];
            JsonNode val[] = new JsonNode[on.size()];
            JSONUtil.populateKeyValue(on, keys, val);
            int size = keys.length;
            for (int i = 0; i < size; i++) {
                if (val[i].isObject() || val[i].isArray()) {
                    JsonNode baseChild = base.get(keys[i]);
                    if (baseChild != null) {
                        // merge listChildren
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
            // assumption that may break that the base is also an array.
            ArrayNode tar = (ArrayNode) base;
            if (tar.size() < ar.size()) {
                for (int j = tar.size() - 1; j <= ar.size(); j++) {
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
                    }
                    merge(baseChild, e);
                } else {
                    ArrayNode target = (ArrayNode) base;
                    target.set(i, e);
                }
            }
        }

    }
}

class JVSChangeNotifier {
    public void change(com.hitorro.util.json.keys.propaccess.Propaccess access, ConfigChangeType type) {

    }

    public enum ConfigChangeType {
        Added, Deleted, Updated
    }
}

