package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Function;

public class EnumKeysMapper<E extends Enum> implements Function<JsonNode, E> {
    private E values[];

    public EnumKeysMapper(E values[]) {
        this.values = values;
    }


    @Override
    public E apply(final JsonNode jsonNode) {
        String v = jsonNode.textValue();
        for (E e : values) {
            if (e.name().equals(v)) {
                return e;
            }
        }
        return null;
    }
}
