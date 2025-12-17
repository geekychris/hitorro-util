package ht.util.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.json.HTJSONParser;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.io.ResetableStringReader;

import java.io.IOException;

public class String2JsonMapper extends BaseMapper<String, JsonNode> {
    private final static JsonFactory factory = new JsonFactory();
    protected JsonParser parser = null;
    private ResetableStringReader reader = new ResetableStringReader("");

    public String2JsonMapper() {

    }

    public JsonNode apply(String s) {
        reader.set(s);
        try {
            HTJSONParser jnp = new HTJSONParser(factory.createParser(reader));
            return jnp.read();
        } catch (IOException e) {
            return null;
        }
    }

}
