package ht.util.json.keys.propaccess;

import com.fasterxml.jackson.databind.JsonNode;


public class PAContextNeverCreate extends PAContext {
    @Override
    public JsonNode getObjectNode(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        return null;
    }

    @Override
    public JsonNode getArrayNode(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        return null;
    }
}
