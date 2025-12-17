package ht.util.propertykeys;

import java.util.HashMap;
import java.util.Map;

public class MapPropertySrc extends PropertySrc {
    private Map<String, String> map = new HashMap();

    public MapPropertySrc() {
        map = new HashMap();
    }

    public MapPropertySrc(Map<String, String> mapIn) {
        map = mapIn;
    }

    @Override
    public String get(final String key) {
        return map.get(key);
    }

    @Override
    public String resolve(final String key) {
        return null;
    }
}
