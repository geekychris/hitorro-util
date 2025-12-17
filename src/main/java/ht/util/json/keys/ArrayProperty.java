package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by chris on 1/9/17.
 */
public class ArrayProperty<T> extends BaseMappingProperty<List<T>> {
    private Function<JsonNode, T> mappingFunction;

    public ArrayProperty(String path, String description, List<T> defaultValue, Function<JsonNode, T> mappingFunction) {
        super(new Propaccess(path), description, defaultValue, new Json2ListOfF(mappingFunction));
    }

    public ArrayProperty(Propaccess path, String description, List<T> defaultValue, Function<JsonNode, T> mappingFunction) {
        super(path, description, defaultValue, new Json2ListOfF(mappingFunction));
    }
}


class Json2ListOfF<T> implements Function<JsonNode, List<T>> {
    private Function<JsonNode, T> mappingFunction;

    public Json2ListOfF(Function<JsonNode, T> mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    @Override
    public List<T> apply(final JsonNode jsonNode) {
        ArrayList<T> list = new ArrayList();

        if (jsonNode.isArray()) {
            ArrayNode an = (ArrayNode) jsonNode;
            for (JsonNode node : an) {
                list.add(mappingFunction.apply(node));
            }
        } else {
            list.add(mappingFunction.apply(jsonNode));
        }

        return list;
    }
}