package ht.util.json.keys;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.json.keys.mappers.BasefileMapper;
import ht.util.json.keys.propaccess.Propaccess;

import java.io.IOException;

public class BasefileProperty extends BaseMappingProperty<BaseFile> {
    public BasefileProperty(String path, String description, BaseFile defaultValue) {
        super(new Propaccess(path), description, defaultValue, BasefileMapper.me);
    }

    public BasefileProperty(String path, String description) {
        super(new Propaccess(path), description, null, BasefileMapper.me);
    }

    public BasefileProperty(Propaccess path, String description, BaseFile defaultValue) {
        super(path, description, defaultValue, BasefileMapper.me);
    }

    public BasefileProperty(Propaccess path, String description, String defaultValue) {
        super(path, description, getFile(defaultValue), BasefileMapper.me);
    }

    public BasefileProperty(String path, String description, String defaultValue) {
        super(new Propaccess(path), description, getFile(defaultValue), BasefileMapper.me);
    }

    private static BaseFile getFile(String file) {
        try {
            return BaseFileSystem.getBaseFileFromPath(file);
        } catch (IOException e) {
            return null;
        }
    }
}

