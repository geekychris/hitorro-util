package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.keys.mappers.JsonNArrayToMapT;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.Map;
import java.util.function.Function;

public class MapProperty<K, T> extends BaseMappingProperty<Map<K, T>> {
    public MapProperty(final Propaccess access, final String description,
                       final Map<K, T> defaultValue, final BaseMappingProperty<T> keyMapper, final Function<JsonNode, K> mapper) throws PropertyException {
        super(access, description, defaultValue, (Function<JsonNode, Map<K, T>>) new JsonNArrayToMapT(keyMapper, mapper));
    }

    public MapProperty(final String path, final String description,
                       final Map<K, T> defaultValue, final BaseMappingProperty<K> keyMapper, final Function<JsonNode, T> mapper) throws PropertyException {
        super(new Propaccess(path), description, defaultValue, (Function<JsonNode, Map<K, T>>) new JsonNArrayToMapT(keyMapper, mapper));
    }
}


