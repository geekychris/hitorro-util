package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.core.iterator.Mapper;

import java.io.IOException;

public class BasefileMapper implements Mapper<JsonNode, BaseFile> {
    public static BasefileMapper me = new BasefileMapper();

    BasefileMapper() {

    }

    public BaseFile apply(JsonNode jsonNodes) {
        try {
            String val = JVSProperties.getProperties().resolveJsonVariable(jsonNodes.textValue());
            if (val == null) {
                return null;
            }
            return BaseFileSystem.getBaseFileFromPath(val);
        } catch (IOException e) {
            return null;
        }
    }
}
