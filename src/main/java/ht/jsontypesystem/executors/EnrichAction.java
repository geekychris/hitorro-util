package ht.jsontypesystem.executors;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.Field;
import ht.jsontypesystem.Group;
import ht.jsontypesystem.SolrFieldType;
import ht.jsontypesystem.SolrFieldTypes;
import ht.util.json.keys.propaccess.Propaccess;

public class EnrichAction implements ExecutorAction<ExecutionBuilder> {
    public EnrichAction(final Field field, Group group, final Propaccess path) {
    }

    @Override
    public void project(final ProjectionContext pc, final Propaccess path, final boolean isMulti, final String lang) {
        // touch the path to initiate the object if it doesnt exist.
        JsonNode val = pc.source.get(path);
    }
}
