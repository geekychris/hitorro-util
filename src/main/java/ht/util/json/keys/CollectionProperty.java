package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.keys.mappers.JsonNArrayToListT;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CollectionProperty<T> extends BaseMappingProperty<List<T>> {

    public CollectionProperty(final Propaccess access, final String description,
                              final List<T> defaultValue, final Function<JsonNode, T> mapper) throws PropertyException {
        super(access, description, defaultValue, (Function<JsonNode, List<T>>) new JsonNArrayToListT(mapper));
    }

    public CollectionProperty(final String path, final String description,
                              final List<T> defaultValue, final Function<JsonNode, T> mapper) throws PropertyException {
        super(new Propaccess(path), description, defaultValue, (Function<JsonNode, List<T>>) new JsonNArrayToListT(mapper));
    }

    public static <T> CollectionProperty<T> getJsonInitableCollection(String path, Class clazz, String description) {
        JsonInitableProperty<T> key = new JsonInitableProperty<T>("", "", null, clazz, clazz);
        CollectionProperty<T> colKey = new CollectionProperty(path, description, new ArrayList(), key);
        return colKey;
    }

    public static CollectionProperty<String> getStringCollection(String path, String description) {
        StringProperty tagKey = new StringProperty("", "", null);
        CollectionProperty<String> colKey = new CollectionProperty(path, description, new ArrayList(), tagKey);
        return colKey;
    }
}


