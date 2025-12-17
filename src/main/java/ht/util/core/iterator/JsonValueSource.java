package ht.util.core.iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.util.core.params.PropertiesUtil;
import ht.util.json.JSONUtil;
import ht.util.json.keys.PropertyParts;
import ht.util.typesystem.TypeFieldIntf;
import ht.util.typesystem.TypeIntf;
import ht.util.typesystem.annotation.UiProperties;
import ht.util.typesystem.valuesource.ValueMapMapper;
import ht.util.typesystem.valuesource.ValueSourceForClass;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JsonValueSource implements ValueSourceForClass {
    public static String TypeField = "ht_type";
    private JsonNode node;
    private TypeIntf type;
    private PropertyParts pp = new PropertyParts("a");

    public JsonValueSource(JsonNode node) {
        this.node = node;
    }

    public JsonValueSource() {
        this.node = JsonNodeFactory.instance.objectNode();
    }

    public JsonNode getNode() {
        return node;
    }

    public TypeIntf getType() {
        if (type != null) {
            return type;
        }
        return null;
    }

    public void setType(TypeIntf type) {
        setValue(this, TypeField, type.getName());
        this.type = type;
    }

    public Object getValue(Object obj, String fieldName) {
        return getValue(fieldName);
    }

    /**
     * Attempt to get a value from the apply, if not found, attempt to fill in if its a dynamic field
     *
     * @param fieldName
     * @return
     */
    public Object getValue(String fieldName) {
        pp.setPath(fieldName);
        Object o = PropertiesUtil.getNode(pp, node, false);
        if (o != null) {
            return o;
        }
        TypeIntf typeI = getType();
        if (typeI != null) {
            TypeFieldIntf tfi = typeI.getField(fieldName);
            if (tfi == null) {
                return null;
            }
            ValueMapMapper vmm = tfi.getValueMapMapper();
            if (vmm == null) {
                return null;
            }
            vmm.compute(fieldName, this);
            return PropertiesUtil.getNode(pp, node, false);
        }
        return null;
    }

    public void setValue(Object ojb, String fieldName, Object value) {
        setValue(ojb, fieldName, value, false);
    }

    public void setValue(Object obj, String fieldName, Integer integer) {
        setValue(obj, fieldName, JsonNodeFactory.instance.numberNode(integer));
    }

    /**
     * a.b[1] - your assuming a certainly exists, maybe b and you will want to set the nth position a.b - doesn't matter
     * about b, we are assuming b is a property of a where a is a apply.
     *
     * @param obj
     * @param fieldName
     * @param value
     */
    public void setValue(Object obj, String fieldName, Object value, boolean ignoreTypeCheck) {
        // XXX TODO do something with type checking!!!!
        if (value == null) {
            return;
        }
        pp.setPath(fieldName);
        if (pp.lastPartIndexed()) {
            ArrayNode an = PropertiesUtil.getNodeAsArrayCreating(pp, node, true);
            int index = pp.getIndex();
            //do we need to expand?
            while (an.size() <= index) {
                an.addNull();
            }
            an.set(index, JSONUtil.ensureJsonNode(value));
        } else {
            // not indexed
            JsonNode par = PropertiesUtil.getNodeParent(pp, node, true);
            if (par.isObject()) {
                ((ObjectNode) par).put(pp.getName(), JSONUtil.ensureJsonNode(value));
            }
        }
        // throw error?
    }


    public String[] getFieldNames() {
        List<String> n = new ArrayList();
        node.size();
        //n.addAll(node.getFieldNames());
        return null;
    }

    public UiProperties getUiProperties(Object obj, String fieldName) {
        return null;
    }
}
