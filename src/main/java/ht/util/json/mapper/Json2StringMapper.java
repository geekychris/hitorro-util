package ht.util.json.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ht.util.core.Constants;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.thread.ThreadStash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Json2StringMapper extends BaseMapper<JsonNode, String> {
    public static ThreadStash<Json2StringMapper> threadedMapper = new ThreadStash() {
        public Json2StringMapper getNew() {
            return new Json2StringMapper();
        }
    };

    protected ObjectMapper mapper = new ObjectMapper();

    @Override
    public String apply(final JsonNode jsonNode) {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try {
            OutputStreamWriter ow = new OutputStreamWriter(boas, Constants.UTF8);
            mapper.writeValue(ow, jsonNode);
            boas.flush();
        } catch (IOException e) {
            return null;
        }
        return new String(boas.toByteArray());
    }
}