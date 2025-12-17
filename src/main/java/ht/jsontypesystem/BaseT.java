package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.error.ErrorCape;
import ht.util.core.error.Errors;
import ht.util.json.JsonInitable;
import ht.util.json.keys.StringProperty;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.function.Predicate;

/**
 * Created by chris on 7/21/17.
 */
public abstract class BaseT implements JsonInitable, ErrorCape {
    public static StringProperty typeKey = new StringProperty("type", "object type", null);
    public static StringProperty nameKey = new StringProperty("name", "", null);
    protected JsonNode node;
    private Errors errors = new Errors();
    private String name;

    @Override
    public boolean init(final JsonNode node) {
        this.name = nameKey.apply(node);
        this.node = node;
        return true;
    }

    public abstract void visit(TypeVisitor visitor, Predicate<BaseT> filter, Propaccess path);

    public JsonNode getMetaNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    @Override
    public Errors getErrors() {
        return errors;
    }
}
