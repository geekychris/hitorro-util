package ht.util.json.keys;

import ht.util.json.keys.propaccess.Propaccess;

public class PropaccessProperty extends BaseMappingProperty<Propaccess> {
    public PropaccessProperty(String path, String description, Propaccess defaultValue) {
        super(new Propaccess(path), description, defaultValue, PropaccesspMap.instance);
    }

    public PropaccessProperty(Propaccess path, String description, Propaccess defaultValue) {
        super(path, description, defaultValue, PropaccesspMap.instance);
    }
}


