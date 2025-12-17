package ht.util.propertykeys;

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;
import java.util.Set;

/**
 * Only allow a value from the listFiles of values
 */
public class StringListFromValidValuesKey extends StringProperty {
    private Set<String> values;

    public StringListFromValidValuesKey(String key,
                                        String description,
                                        String defaultValue,
                                        Set<String> values) {
        super(key, description, defaultValue);
        this.values = values;
    }

    public String getPropertyType() {
        return "List";
    }

    public String apply(Map<String, String> map) {
        String val = getValueFromConfig(map);
        if (val == null && m_notNull == false) {
            if (this.m_resolveVariable && m_defaultValue != null) {
                return JVSProperties.getProperties().resolveJsonVariable(m_defaultValue);
            }
            val = m_defaultValue;
        }
        String tmp = val.toLowerCase();
        if (!values.contains(tmp)) {
            return null;
        }
        return val;
    }

    protected void validate(String sVal)
            throws PropertyKeyValidationException {
        if (sVal == null) {
            if (m_notNull && m_defaultValue == null) {
                throw new PropertyKeyValidationException("Key does not exist", this.m_key, "<<null>>");
            }
            sVal = m_defaultValue;
        }
        String tmp = sVal.toLowerCase();
        if (!values.contains(tmp)) {
            throw new PropertyKeyValidationException("Key not valid value", this.m_key, sVal);
        }
    }

}
