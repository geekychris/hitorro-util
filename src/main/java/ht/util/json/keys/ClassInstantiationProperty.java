package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.classes.ClassUtil;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.function.Function;

public class ClassInstantiationProperty<T> extends BaseMappingProperty<T> {
    public ClassInstantiationProperty(final Propaccess access, final String description, final T defaultValue, final Class superC) throws PropertyException {
        super(access, description, defaultValue, new ClassInstantiationMapper(superC));
    }

    public ClassInstantiationProperty(final String access, final String description, final T defaultValue, Class superC) throws PropertyException {
        super(new Propaccess(access), description, defaultValue, new ClassInstantiationMapper(superC));
    }
}

class ClassInstantiationMapper<T> implements Function<JsonNode, T> {
    private Class superC;

    public ClassInstantiationMapper(Class c) {
        this.superC = c;
    }

    @Override
    public T apply(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        String className;
        if (jsonNode.isTextual()) {
            className = jsonNode.textValue();
        } else {
            className = jsonNode.asText();
        }
        return (T) ClassUtil.getInstanceSwallowError(className, superC);
    }
}