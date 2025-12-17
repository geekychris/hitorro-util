package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;
import ht.util.core.params.PropertiesUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class JsonNodeToUrl implements Mapper<JsonNode, URL> {
    public static JsonNodeToUrl instance = new JsonNodeToUrl();

    public URL apply(JsonNode jsonNodes) {
        try {
            return new URL(PropertiesUtil.resolveJsonVariable(jsonNodes.asText()));
        } catch (MalformedURLException e) {
            return null;
        }
    }
}

