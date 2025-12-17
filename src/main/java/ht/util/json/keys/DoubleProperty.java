package ht.util.json.keys;

import ht.util.json.keys.mappers.JsonNodeToDouble;
import ht.util.json.keys.propaccess.Propaccess;

/**
 *
 */
public class DoubleProperty extends BaseMappingProperty<Double> {
    public DoubleProperty(String path, String description, Double defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToDouble.instance);
    }

    public DoubleProperty(Propaccess path, String description, Double defaultValue) {
        super(path, description, defaultValue, JsonNodeToDouble.instance);
    }
}