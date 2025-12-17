package ht.util.propertykeys;

import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

/**
 *
 */
public class EnumKey<E extends Enum> extends PropertyKey<E> {
    protected E vals[];
    protected String names[];
    protected E defaultVal;

    public EnumKey(String key, String description, E[] vals, E defaultVal) {
        super(key, description);
        this.vals = vals;
        this.defaultVal = defaultVal;
        names = new String[vals.length];
        setNames(vals);
    }

    @Override
    public String getPropertyType() {
        return "Enum";
    }

    private void setNames(E e[]) {
        names = new String[e.length];
        for (int i = 0; i < e.length; i++) {
            names[i] = getName(e[i]);
        }
    }

    public String getName(E e) {
        return e.name().toLowerCase();
    }


    @Override
    public E apply(final Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        return get(sValue);
    }

    private E get(String sValue) {
        if (sValue == null) {
            if (defaultVal != null) {
                return defaultVal;
            }
            return null;
        }
        sValue = sValue.toLowerCase();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(sValue)) {
                return vals[i];
            }
        }
        return null;
    }

    @Override
    protected void validate(final String sVal) throws PropertyKeyValidationException {
        try {

            E e = get(sVal);
            if (e == null) {
                throw new PropertyKeyValidationException("Property is not a valid enumeration", getKey(), sVal);
            }
        } catch (NumberFormatException nfe) {
            throw new PropertyKeyValidationException("Property is not a valid enumeration", getKey(), sVal);
        }

        return;
    }

}
