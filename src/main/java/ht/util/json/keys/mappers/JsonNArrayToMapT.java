package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;
import ht.util.json.keys.BaseMappingProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JsonNArrayToMapT<K, T> implements Mapper<JsonNode, Map<K, T>> {
    private BaseMappingProperty<K> keyMapper;
    private Function<JsonNode, T> mapper;

    public JsonNArrayToMapT(BaseMappingProperty<K> keyMapper, Function<JsonNode, T> mapper) {
        this.keyMapper = keyMapper;
        this.mapper = mapper;
    }

    public Map<K, T> apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isArray()) {
            Map<K, T> map = new HashMap<K, T>();
            int size = jsonNodes.size();
            for (int i = 0; i < size; i++) {
                JsonNode elem = jsonNodes.get(i);
                K k = keyMapper.apply(elem);
                T t = mapper.apply(elem);
                map.put(k, t);
            }
            return map;
        }
        return null;
    }
}
