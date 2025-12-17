package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.function.Function;

/**
 * Created by chris on 12/31/16.
 */
public class StaticVarProperty<T> extends BaseMappingProperty<T> {
    public StaticVarProperty(String path, String description, boolean mustExist, T defaultVal, Class requiredSuper) {
        super(new Propaccess(path), description, defaultVal, new SVPM(requiredSuper, path));
    }

    public String getPropertyType() {
        return "Object";
    }
}

class SVPM<T> implements Function<JsonNode, T> {
    private Class superC;
    private String key;

    public SVPM(Class superC, String key) {
        this.superC = superC;
        this.key = key;
    }

    public T apply(JsonNode node) {
        String sValue;

        if (node.isTextual()) {
            sValue = node.textValue();
        } else {
            sValue = node.asText();
        }

        return (T) ht.util.propertykeys.StaticVarProperty.getValidated(sValue, superC, key);
    }
}