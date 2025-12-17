package ht.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ht.util.core.Constants;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.thread.ThreadStash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Json2ByteArrayMapper extends BaseMapper<JsonNode, byte[]> {
    public static ThreadStash<Json2ByteArrayMapper> threadedMapper = new ThreadStash() {
        public Json2ByteArrayMapper getNew() {
            return new Json2ByteArrayMapper();
        }
    };
    protected ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] apply(final JsonNode jsonNode) {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try {
            OutputStreamWriter ow = new OutputStreamWriter(boas, Constants.UTF8);
            mapper.writeValue(ow, jsonNode);
            boas.flush();
        } catch (IOException e) {
            return null;
        }
        return boas.toByteArray();
    }
}