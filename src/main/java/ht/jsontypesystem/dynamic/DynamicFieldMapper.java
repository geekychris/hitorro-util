package ht.jsontypesystem.dynamic;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.core.Log;
import ht.util.json.JsonInitable;
import ht.util.json.keys.CollectionProperty;
import ht.util.json.keys.PropaccesspMap;
import ht.util.json.keys.propaccess.Propaccess;
import ht.util.json.keys.propaccess.PropaccessError;

import java.util.List;

/**
 * Created by chris on 3/8/16.
 */


public abstract class DynamicFieldMapper implements JsonInitable {
    public static final CollectionProperty<Propaccess> propsKey = new CollectionProperty("fields", "", null, PropaccesspMap.instance);
    private Propaccess fields[];

    public boolean init(JsonNode node) {
        List<Propaccess> p = propsKey.apply(node);
        if (p == null) {
            fields = new Propaccess[0];
        } else {
            fields = p.toArray(new Propaccess[p.size()]);
        }
        return true;
    }

    public abstract JsonNode map(JVS jvs, Propaccess pa, int depth);

    protected JsonNode[] getValues(JVS jvs, Propaccess pa, int depth) {
        JsonNode arr[] = new JsonNode[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Propaccess f = fields[i];
            if (f.isRelative()) {
                Propaccess fNew = pa.clone();
                fNew.pop();
                fNew.append(f);

                try {
                    arr[i] = jvs.get(fNew);
                } catch (PropaccessError propaccessError) {
                    // XXX swallow
                    Log.type.error("DynamicFieldMapper(2) %s %e", propaccessError, propaccessError);
                }
            } else {
                try {
                    arr[i] = jvs.get(f);
                } catch (PropaccessError propaccessError) {
                    // XXX swallow
                    Log.type.error("DynamicFieldMapper(2) %s %e", propaccessError, propaccessError);
                }
            }
        }
        return arr;
    }

}
