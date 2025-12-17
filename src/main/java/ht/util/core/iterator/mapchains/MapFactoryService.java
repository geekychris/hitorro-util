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
import ht.util.core.GenericKeyValue;
import ht.util.core.string.StringUtil;
import ht.util.startupframework.phases.ServiceDefinition;
import ht.util.startupframework.phases.ServiceInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
@ServiceDefinition(dependentService = {},
        shortName = "mapchains",
        description = "Map chains service",
        debugCommands = {DumpMapFactories.class},
        typeManagedClasses = {},
        uiDirectories = {},
        dependentServiceInterfaces = {})
public class MapFactoryService {
    public static final String BaseMappers = "basemappers";
    private static MapFactoryService service;
    private HashMap<String, MapFactory> factories = new HashMap();

    public static MapFactoryService getService() {
        return service;
    }

    /**
     * Allow a service to register a set of mappers by defining it in the configs.  One simply provides the root path to
     * the definition.  A definition can refer to a "parent" set of definitions (such as base).  This way you can extend
     * libraries of functions for specific domain specific uses.  The parent though must of been defined before hand
     * else an error will be returned.  You do not have to define a parent if you want a completely different set of
     * functions that do not overlap
     *
     * @param path
     * @return
     */
    public String registerFactoryByPath(String path) {
        MapFactory mf = new MapFactory();
        //XXX TODO provide path to data structure in configs.
        JsonNode args = null;
        boolean err = mf.init(args);
        if (!err) {
            return "Unable to initialize map factory";
        }
        factories.put(mf.getName(), mf);
        return null;
    }

    public List<GenericKeyValue> getDescriptions() {
        List<GenericKeyValue> list = new ArrayList();
        for (MapFactory factory : factories.values()) {
            list.add(factory.getKeyValueDescription());
        }
        return list;
    }

    public MapFactory getMapFactory(String name) {
        return factories.get(name.toLowerCase());
    }

    @ServiceInit
    public String init(final boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        service = this;
        String err = registerFactoryByPath(BaseMappers);
        if (!StringUtil.nullOrEmptyOrBlankString(err)) {
            return err;
        }
        return null;
    }
}
