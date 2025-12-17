package ht.util.json.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.json.HTJSONParser;
import ht.util.core.iterator.mappers.BaseMapper;

import java.io.IOException;

public class String2JsonMapper extends BaseMapper<String, JsonNode> {
    private final static JsonFactory factory = new JsonFactory();

    public String2JsonMapper() {
    }

    @Override
    public JsonNode apply(final String s) {
        try {
            HTJSONParser parser = new HTJSONParser(factory.createParser(s));
            return parser.read();

        } catch (IOException e) {
            return null;
        }
    }
}