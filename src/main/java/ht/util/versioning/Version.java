package ht.util.versioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.Env;
import ht.util.core.GenericKeyValue;
import ht.util.core.string.Fmt;
import ht.util.json.JSONUtil;
import ht.util.json.keys.propaccess.PropaccessError;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 3:13:16 PM Wrapper around
 * the version information of the binary release.
 */
public class Version {
    private JsonNode node;

    public Version() {

        // now put some vm and other info
        try {
            node = JVSProperties.getProperties().get("build");
            Env.addVMPropsToMap(JVSProperties.getProperties());
        } catch (PropaccessError propaccessError) {

        }
    }

    public static final String getFullVersionLabel() {
        try {
            return Fmt.S("%s.%s", JVSProperties.getProperties().getString("build.version"),
                    JVSProperties.getProperties().getString("build.number"));
        } catch (PropaccessError propaccessError) {
            return null;
        }
    }

    public List<GenericKeyValue> getValues() {
        List<GenericKeyValue> list = new ArrayList<GenericKeyValue>();
        getValues(list);
        return list;
    }

    public void getValues(List<GenericKeyValue> list) {
        String keys[] = new String[node.size()];
        JsonNode values[] = new JsonNode[node.size()];
        JSONUtil.populateKeyValue((ObjectNode) node, keys, values);
        for (int i = 0; i < keys.length; i++) {
            list.add(new GenericKeyValue(keys[i], values[i]));
        }
    }

}
