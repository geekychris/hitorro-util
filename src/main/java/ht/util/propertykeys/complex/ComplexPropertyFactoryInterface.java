package ht.util.propertykeys.complex;

import com.fasterxml.jackson.databind.JsonNode;


/**
 *
 */
public interface ComplexPropertyFactoryInterface<T> {
    String[] getNames();

    T getInstance(JsonNode map, String type, String parentPathName);
}
