package ht.jsontypesystem;

import ht.util.json.JsonInitable;

import java.util.List;

public interface JVSAction extends JsonInitable {
    void invoke(JVS src, List<JVS> args);
}
