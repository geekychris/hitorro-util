package ht.util.json.keys;

import ht.util.json.keys.mappers.ValidatedStringMapper;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.Set;

public class ValidatedStringProperty extends BaseMappingProperty<String> {
    public ValidatedStringProperty(String path, String description, String defaultValue, Set<String> validatedList) {
        super(new Propaccess(path), description, defaultValue, new ValidatedStringMapper(validatedList));
    }

    public ValidatedStringProperty(Propaccess path, String description, String defaultValue, Set<String> validatedList) {
        super(path, description, defaultValue, new ValidatedStringMapper(validatedList));
    }
}

