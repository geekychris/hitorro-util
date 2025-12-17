package ht.util.propertykeys;

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.params.HTProperties;

public class HTPropertySrc extends PropertySrc {
    @Override
    public String get(final String key) {
        return HTProperties.getProperties().get(key);
    }

    public String resolve(final String key) {
        return JVSProperties.getProperties().resolveJsonVariable(key);
    }
}
