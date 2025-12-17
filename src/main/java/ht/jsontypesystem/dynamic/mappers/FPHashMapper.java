package ht.jsontypesystem.dynamic.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.hash.FPHash64;

public class FPHashMapper extends BaseJsonInteropMapper {

    public long string2longMap(String s) {
        return FPHash64.getFP(s);
    }

    @Override
    public JsonNode apply(final JsonNode jsonNode) {
        return applyString2long(jsonNode);
    }
}
