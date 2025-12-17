package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JsonNArrayToListT<T> implements Mapper<JsonNode, List<T>> {
    private Function<JsonNode, T> mapper;

    public JsonNArrayToListT(Function<JsonNode, T> mapper) {
        this.mapper = mapper;
    }

    public List<T> apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isArray()) {
            List<T> list = new ArrayList<T>();
            int size = jsonNodes.size();
            for (int i = 0; i < size; i++) {
                list.add(mapper.apply(jsonNodes.get(i)));
            }
            return list;
        }
        return null;
    }
}
