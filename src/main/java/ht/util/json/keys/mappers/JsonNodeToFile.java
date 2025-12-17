package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;
import ht.util.core.params.PropertiesUtil;

import java.io.File;

/**
 *
 */
public class JsonNodeToFile implements Mapper<JsonNode, File> {
    public static JsonNodeToFile instance = new JsonNodeToFile();

    public File apply(JsonNode jsonNodes) {
        return new File(PropertiesUtil.resolveJsonVariable(jsonNodes.asText()));
    }
}

