package ht.jsontypesystem.dynamic.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.urlparser.UrlNormalizer;

public class UrlNormalizerMapper extends BaseJsonInteropMapper {
    protected ThreadLocal<UrlNormalizer> threadData = new ThreadLocal();

    public String stringMap(String s) {
        UrlNormalizer normalizer = getNorm();
        return normalizer.normalize(s);
    }

    private UrlNormalizer getNorm() {
        UrlNormalizer norm = threadData.get();
        if (norm == null) {
            norm = new UrlNormalizer(true, true, false);
            threadData.set(norm);
        }
        return norm;
    }

    @Override
    public JsonNode apply(final JsonNode jsonNode) {
        return applyString2long(jsonNode);
    }
}
