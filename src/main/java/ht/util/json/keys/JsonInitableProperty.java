package ht.util.json.keys;

import ht.util.core.classes.JsonNode2JsonInitable;
import ht.util.json.keys.propaccess.Propaccess;


public class JsonInitableProperty<T> extends BaseMappingProperty<T> {
    private Class c;

    public JsonInitableProperty(String path, String description, T defaultValue, Class c, Class defaultClass) {
        super(new Propaccess(path), description, defaultValue, new JsonNode2JsonInitable(c, "class", defaultClass));
    }

    public JsonInitableProperty(Propaccess path, String description, T defaultValue, Class c, Class defaultClass) {
        super(path, description, defaultValue, new JsonNode2JsonInitable(c, "class", defaultClass));
    }
}