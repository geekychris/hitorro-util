package ht.jsontypesystem.dynamic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ht.jsontypesystem.JVS;
import ht.util.json.keys.StringProperty;
import ht.util.json.keys.propaccess.Propaccess;

//ht.jsontypesystem.dynamic.MultiValueMergerDM
public class MultiValueMergerDM extends DynamicFieldMapper {
    public static final StringProperty separatorKey = new StringProperty("seperator", "", ":");
    public static final StringProperty nullValueKey = new StringProperty("null", "", "null");

    private String nullValue;
    private String seperator;

    public boolean init(JsonNode node) {
        boolean flag = super.init(node);
        nullValue = nullValueKey.apply(node);
        seperator = separatorKey.apply(node);
        return flag;
    }

    @Override
    public JsonNode map(final JVS jvs, final Propaccess pa, final int depth) {
        StringBuilder sb = new StringBuilder();
        JsonNode arr[] = getValues(jvs, pa, depth);
        for (int i = 0; i < arr.length; i++) {
            if (sb.length() != 0) {
                sb.append(seperator);
            }
            if (arr[i] == null) {
                sb.append(nullValue);
            } else {
                sb.append(arr[i].textValue());
            }
        }
        return JsonNodeFactory.instance.textNode(sb.toString());
    }
}
