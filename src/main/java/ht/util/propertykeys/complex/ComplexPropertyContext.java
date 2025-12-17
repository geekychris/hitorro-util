package ht.util.propertykeys.complex;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StringProperty;
import ht.util.json.keys.propaccess.PropaccessError;

import java.util.*;

/**
 *
 */
public class ComplexPropertyContext {
    public static final StringProperty TypeKey = new StringProperty("cptype", "", null);
    private static Map<String, ComplexPropertyFactoryInterface> adapterMap = new HashMap();

    public static void add(ComplexPropertyFactoryInterface intf) {
        for (String name : intf.getNames()) {
            adapterMap.put(name.toLowerCase(), intf);
        }
    }

    public static <T extends Object> List<T> getList(String path, String defaultType) throws ComplexPropertiesException, PropaccessError {
        JsonNode keys = JVSProperties.getProperties().get(path);
        List<T> list = new ArrayList<T>();
        Iterator<Map.Entry<String, JsonNode>> iter = keys.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> km = iter.next();
            T t = get(km.getValue(), defaultType, km.getKey());
            list.add(t);
        }
        return list;
    }

    /**
     * Given a path attempt to construct an instance of whatever from the configs
     *
     * @param path
     * @return
     */
    public static <T extends Object> T get(String path, String defaultType) throws ComplexPropertiesException, PropaccessError {
        JsonNode map = JVSProperties.getProperties().get(path);
        return (T) get(map, defaultType, path);
    }

    /**
     * Given a path attempt to construct an instance of whatever from the configs
     *
     * @param path
     * @return
     */
    public static <T extends Object> T get(String path) throws ComplexPropertiesException, PropaccessError {
        JsonNode map = JVSProperties.getProperties().get(path);
        return (T) get(map, null, path);
    }

    public static <T extends Object> T get(JsonNode map, String defaultType, String path) throws ComplexPropertiesException {
        String type = TypeKey.apply(map);
        if (StringUtil.nullOrEmptyOrBlankString(type)) {
            type = defaultType;
        }
        if (StringUtil.nullOrEmptyOrBlankString(type)) {
            throw new NoFactoryFound("No factory type defined for");
        }
        ComplexPropertyFactoryInterface<T> intf = adapterMap.get(type.toLowerCase());
        if (intf == null) {
            throw new NoFactoryFound(Fmt.S("No factory found for %s", type));
        }

        return intf.getInstance(map, type, StringUtil.getLastCanonicalPart(path));
    }
}
