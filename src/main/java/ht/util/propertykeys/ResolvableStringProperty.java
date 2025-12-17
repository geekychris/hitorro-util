package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;

/**
 * Get a string and resolve any variables.
 *
 * @author chris
 */
public class ResolvableStringProperty extends StringProperty {

    public ResolvableStringProperty(String key, String description, boolean notNull) {
        super(key, description, notNull);
        m_resolveVariable = true;
    }

    public ResolvableStringProperty(DebugArgAno ano) {
        this(ano.keyName(), ano.description(), ano.defaultValue());
    }

    public ResolvableStringProperty(String key, String description, String defaultVal) {
        super(key, description, defaultVal);
        m_resolveVariable = true;
    }
}
