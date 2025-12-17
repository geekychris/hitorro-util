package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.KeyValue;
import ht.util.json.JSONUtil;
import ht.util.json.keys.propaccess.PAContext;
import ht.util.json.keys.propaccess.Propaccess;
import ht.util.json.keys.propaccess.PropaccessError;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base implementation of accessor to the config
 */
public class BaseMappingProperty<T> extends Propaccess implements Function<JsonNode, T> {
    protected T defaultValue;
    protected Function<JsonNode, T> mapper;
    protected String description;

    public BaseMappingProperty(Propaccess access, String description, T defaultValue, Function<JsonNode, T> mapper) throws PropertyException {
        super(access);
        this.defaultValue = defaultValue;
        this.mapper = mapper;
        this.description = description;
    }

    public Function<JsonNode, T> getFunction() {
        return mapper;
    }

    public T getDefault() {
        return defaultValue;
    }

    public <K> MapProperty<K, T> mapProperty(String key, String description, Map<K, T> defaultValue, Function<JsonNode, K> keyMapper) {
        return mapProperty(new Propaccess(key), description, defaultValue, keyMapper);
    }

    public <K> MapProperty<K, T> mapProperty(Propaccess key, String description, Map<K, T> defaultValue, Function<JsonNode, K> keyMapper) {
        return new MapProperty(key, description, defaultValue, (BaseMappingProperty<T>) keyMapper, this);
    }

    public CollectionProperty<T> collection(String key, String description, List<T> defaultValue) {
        return collection(new Propaccess(key), description, defaultValue);
    }

    public CollectionProperty<T> collection(Propaccess key, String description, List<T> defaultValue) {
        return new CollectionProperty<T>(key, description, defaultValue, this);
    }

    public String getKey() {
        return toString();
    }


    public String getDescription() {
        return description;
    }

    public T apply() {
        return apply(JVSProperties.getProperties());
    }

    public void validate(JVS args)
            throws PropertyKeyValidationException {
        validate(args.getJsonNode());
    }

    public void validate()
            throws PropertyKeyValidationException {
        apply();
    }

    public void validate(JsonNode map)
            throws PropertyKeyValidationException {
        T sVal = apply(map);
        //XXX do some validation
    }

    public void setPath(T t) {
        setPath(getNode(null), t);
    }

    public void setPath(JsonNode node, T t) {
        try {
            set(null, node, PAContext.AlwaysCreate, JSONUtil.ensureJsonNode(t));
        } catch (PropaccessError propaccessError) {
        }
    }

    public T apply(JVS jvs) {
        JsonNode n = jvs.get(this);
        if (n == null) {
            return defaultValue;
        }
        return mapper.apply(n);
    }

    public T apply(JsonNode node) {
        JsonNode n = getNode(node);
        if (n == null) {
            return defaultValue;
        }
        return mapper.apply(n);
    }

    protected JsonNode getNode(JsonNode node) {
        try {
            return get(null, node, PAContext.NeverCreate);
        } catch (PropaccessError propaccessError) {
            return null;
        }
    }


    /**
     * Indicates if this property key provides a listFiles of valid values
     *
     * @return
     */
    public boolean getHasValidationList() {
        return false;
    }

    /**
     * List of valid values for properties that should only allow valid values in the system. The Key is the raw key
     * (that would be entered in the command line). The Value is the description that would be used in a dropdown listFiles.
     *
     * @return
     */
    public List<KeyValue> getValidationList() {
        return null;
    }

    public String getPropertyType() {
        return "unknown_type";
    }
}
