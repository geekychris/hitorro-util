package ht.util.json.keys.propaccess;

import com.fasterxml.jackson.databind.JsonNode;

public interface VS {
    JsonNode get(Propaccess pa) throws PropaccessError;

    VS set(Propaccess path, Object value) throws PropaccessError;

    VS set(Propaccess path, int depth, JsonNode value) throws PropaccessError;
}
