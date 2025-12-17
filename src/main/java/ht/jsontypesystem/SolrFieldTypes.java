package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.Name2JsonMapper;
import ht.util.core.Env;
import ht.util.core.events.cache.HashCache;
import ht.util.core.events.cache.SingletonCache;
import ht.util.core.iterator.Mapper;
import ht.util.json.keys.JsonInitableProperty;
import ht.util.json.keys.MapProperty;

import java.util.HashMap;
import java.util.Map;

public class SolrFieldTypes {
    public static HashCache<String, JsonNode> solrFieldTypesConfig =
            new HashCache<>(0, true,
                    null, "solrconfig",
                    new Name2JsonMapper(Env.getBinConfigBaseFile().getChild("jsonconfigs"), "solr"));

    public static SingletonCache<SolrFieldTypes> solrFieldTypeCache =
            new SingletonCache<SolrFieldTypes>(true,
                    true, "solffieldtypes",
                    new Name2SolrFieldTypesMapper(), null);

    public static JsonInitableProperty<SolrFieldType> SolrFieldTypeKey = new JsonInitableProperty("", "", null, SolrFieldType.class, SolrFieldType.class);

    public static MapProperty<String, SolrFieldType> SolrFields = SolrFieldTypeKey.mapProperty("fields", "", null, SolrFieldType.Name);

    protected Map<String, SolrFieldType> map = new HashMap();

    public SolrFieldType get(String name) {
        return map.get(name);
    }
}

class Name2SolrFieldTypesMapper implements Mapper<Object, SolrFieldTypes> {
    public SolrFieldTypes apply(Object s) {
        JsonNode node = SolrFieldTypes.solrFieldTypesConfig.get("solr_fields");
        if (node == null) {
            return null;
        }
        SolrFieldTypes sft = new SolrFieldTypes();
        sft.map = SolrFieldTypes.SolrFields.apply(node);
        return sft;
    }
}