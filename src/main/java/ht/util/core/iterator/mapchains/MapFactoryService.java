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
