package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.string.Fmt;
import ht.util.json.JsonInitable;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.StringProperty;

//
public class SolrFieldType implements JsonInitable {
    public static BooleanProperty Is18N = new BooleanProperty("i18n", "", false);
    public static BooleanProperty IsId = new BooleanProperty("isid", "", false);
    public static StringProperty Name = new StringProperty("name", "", null);

    private boolean is_18n;
    private boolean isId;
    private String indexType;

    public String toString() {
        return Fmt.S("SFT: i18n:%s isid: %s type:%s", Boolean.toString(is_18n), Boolean.toString(isId), indexType);
    }

    public void get(StringBuilder fieldPath, String lang, boolean isMulti) {
        if (is_18n) {
            // this.is.my.path.index_type_lang_m
            Fmt.f(fieldPath, ".%s_%s_%s", indexType, lang, getMulti(isMulti));
        } else {
            Fmt.f(fieldPath, ".%s_%s", indexType, getMulti(isMulti));
        }
    }

    private char getMulti(boolean multi) {
        if (multi) {
            return 'm';
        }
        return 's';
    }

    @Override
    public boolean init(final JsonNode node) {
        is_18n = Is18N.apply(node);
        isId = IsId.apply(node);
        indexType = Name.apply(node);
        return true;
    }
}
