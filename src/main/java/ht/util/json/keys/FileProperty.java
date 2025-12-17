package ht.util.json.keys;


import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.mappers.JsonNodeToFile;
import ht.util.json.keys.propaccess.Propaccess;

import java.io.File;

/**
 *
 */
public class FileProperty extends BaseMappingProperty<File> {
    public FileProperty(String path, String description, String defaultValue) {
        super(new Propaccess(path), description,
                getDefaultFile(defaultValue),
                JsonNodeToFile.instance);
    }

    public FileProperty(String path, String description) {
        super(new Propaccess(path), description, null, JsonNodeToFile.instance);
    }

    public FileProperty(String path, String description, File defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToFile.instance);
    }

    public FileProperty(Propaccess path, String description, File defaultValue) {
        super(path, description, defaultValue, JsonNodeToFile.instance);
    }

    public FileProperty(Propaccess path, String description) {
        super(path, description, null, JsonNodeToFile.instance);
    }

    private static File getDefaultFile(String defaultValue) {
        String resolved = JVSProperties.getProperties().resolveJsonVariable(defaultValue);
        if (StringUtil.nullOrEmptyString(resolved)) {
            return null;
        }
        return new File(resolved);
    }
}
