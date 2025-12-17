package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.Constants;
import ht.util.core.iterator.Mapper;

import java.text.ParseException;

public class BytesToLong implements Mapper<JsonNode, Long> {
    public static BytesToLong instance = new BytesToLong();

    public Long apply(JsonNode jsonNodes) {
        String sValue = jsonNodes.textValue();
        try {
            return Constants.getBytesFromString(sValue);
        } catch (ParseException e) {
            return null;
        }
    }
}
