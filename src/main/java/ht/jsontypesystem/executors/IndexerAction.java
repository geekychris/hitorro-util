package ht.jsontypesystem.executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.jsontypesystem.Field;
import ht.jsontypesystem.Group;
import ht.jsontypesystem.SolrFieldType;
import ht.jsontypesystem.SolrFieldTypes;
import ht.util.json.keys.propaccess.Propaccess;
import ht.util.json.keys.propaccess.PropaccessError;

public class IndexerAction implements ExecutorAction<ExecutionBuilder> {
    protected Group group;
    protected Field field;
    private SolrFieldType sft;
    private String method;

    public IndexerAction(final Field field, Group group, final Propaccess path) {
        this.group = group;
        this.field = field;
        SolrFieldTypes sfts = SolrFieldTypes.solrFieldTypeCache.get();
        method = group.getMethod();
        sft = sfts.get(method);
    }


    public void project(ProjectionContext pc, Propaccess path, final boolean isMulti, final String lang) {
        try {
            JsonNode val = pc.source.get(path);

            if (val != null) {
                if (val.isNull()) {
                    return;
                }
                pc.sb.setLength(0);
                path.getPathSansIndex(pc.sb);
                sft.get(pc.sb, lang, isMulti);
                String field = pc.sb.toString();
                ObjectNode on = (ObjectNode) pc.target.getJsonNode();
                JsonNode n = on.get(field);
                String vText = val.textValue();
                if (n == null) {
                    on.put(field, vText);
                } else {
                    ArrayNode arr;
                    if (n.isArray()) {
                        arr = (ArrayNode) n;
                    } else {
                        arr = JsonNodeFactory.instance.arrayNode();
                        arr.add(n);
                        on.set(field, arr);
                    }

                    arr.add(val);
                }
            }
        } catch (PropaccessError propaccessError) {
            propaccessError.printStackTrace();
        }
    }
}
