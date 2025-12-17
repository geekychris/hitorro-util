package ht.jsontypesystem;

import com.fasterxml.jackson.databind.node.ArrayNode;
import ht.util.json.JsonInitable;
import ht.util.json.keys.propaccess.Propaccess;

public interface IndexSeeker extends JsonInitable {
    int getIndex(ArrayNode node, Propaccess access, int depth, String value, JVS jvs);
}
