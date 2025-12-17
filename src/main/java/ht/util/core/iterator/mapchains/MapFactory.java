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
package ht.util.core.iterator.mapchains;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.util.commandandcontrol.InfoLevel;
import ht.util.commandandcontrol.Response;
import ht.util.core.GenericKeyValue;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.iterator.mappers.MapperCollection;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StaticVarProperty;
import ht.util.json.keys.StringProperty;

import java.util.HashMap;
import java.util.Iterator;

/**
 * allows one to construct a set of short names for mapping objects in a chain using BaseMappers.  for instance:
 * <p/>
 * trim:toint
 * <p/>
 * could trim a string and then convertToPdf it into an int.  Factories can be nested, thus allowing a "base" factory to
 * be constructed with common utility mappings, then a more application specific one can be built ontop.  Uses for this
 * include:
 * <p/>
 * type convertion within a load and transform pipeline mapping of a value in an indexer.
 * <p/>
 * Note that current it supports a single value in and single value out
 * <p/>
 * Each step can be defined as either a class or a static member of a class using either:
 * <p/>
 * ht.util.core.iterator.mapchains.ToIntMapStep or ht.util.file.fs.BaseFileUtil#bf2utf8Writer
 */
public class MapFactory {
    public static final StaticVarProperty<BaseMapper> clazz = new StaticVarProperty("class", "class for the mapping", true, null, BaseMapper.class);
    public static final StringProperty ParentName = new StringProperty("parent", "class for the mapping", null);
    public static final StringProperty Name = new StringProperty("name", "class for the mapping", null);
    public static final StringProperty Description = new StringProperty("description", "Description of this factory", "");
    private HashMap<String, BaseMapper> nameToStep = new HashMap();
    private MapFactory parent;
    private String parentName;
    private String name;
    private String description;


    public MapFactory() {

    }

    public void addResponseRow(Response response) {
        for (String key : nameToStep.keySet()) {
            BaseMapper step = nameToStep.get(key);
            response.addRow(key, step.getDescription(), step.inputType(), step.outputType());
        }
        if (parent != null) {
            response.addInfo(InfoLevel.Info, Fmt.S("extends %s", parentName));
        }
    }

    /**
     * It is assumed that any mapper can be cached, that a apply DOES NOT hold state and therefor can be used in a
     * reentrant role.
     *
     * @param chain
     * @return
     * @throws ChainNotConstructable
     */
    public BaseMapper getChain(String chain) throws ChainNotConstructable {
        chain = chain.toLowerCase();
        synchronized (nameToStep) {
            BaseMapper bm = nameToStep.get(chain);
            if (bm != null) {
                // cached result
                return bm;
            }
            String parts[] = StringUtil.tokenizeFromSingleChar(chain, ":", true);
            BaseMapper mappers[] = new BaseMapper[parts.length];
            for (int i = 0; i < parts.length; i++) {
                BaseMapper curr = getPart(parts[i]);
                if (curr == null) {
                    throw new ChainNotConstructable(Fmt.S("Unable to get part: %s for chain %s", parts[i], chain));
                }
                mappers[i] = curr;
            }
            if (mappers.length == 1) {
                bm = mappers[0];
            } else {
                bm = new MapperCollection(mappers);
            }
            nameToStep.put(chain, bm);
            return bm;
        }
    }

    private BaseMapper getPart(String part) {
        BaseMapper curr = nameToStep.get(part);
        if (curr != null) {
            return curr;
        }
        if (parent != null) {
            return parent.getPart(part);
        }
        return null;
    }

    public GenericKeyValue getKeyValueDescription() {
        return new GenericKeyValue(name, description);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean init(JsonNode args) {
        parentName = ParentName.apply(args);
        name = Name.apply(args);
        if (StringUtil.nullOrEmptyString(name)) {
            //return Fmt.S("Name not specified in MapFactory configuration");
            return false;
        }
        name = name.toLowerCase();
        if (!StringUtil.nullOrEmptyString(parentName)) {
            parent = MapFactoryService.getService().getMapFactory(parentName);
            if (parent == null) {
                //return Fmt.S("Unable to get parent MapFactory for name %s", parentName);
                return false;
            }
        }
        description = Description.apply(args);

        ObjectNode maps = (ObjectNode) args.get("functions");

        Iterator<String> fieldNames = maps.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();

            ObjectNode val = (ObjectNode) maps.get(key);
            add(key, val);
        }
        return true;
    }

    public String add(String name, ObjectNode val) {
        name = name.toLowerCase();

        BaseMapper ms = clazz.apply(val);
        if (ms == null) {
            return Fmt.S("Unable to create MapStep for class %s ", name);
        }

        ObjectNode props = (ObjectNode) val.get("properties");
        boolean success = ms.init(props);
        if (!success) {
            return "cant init";
        }
        nameToStep.put(name, ms);
        return null;
    }
}
