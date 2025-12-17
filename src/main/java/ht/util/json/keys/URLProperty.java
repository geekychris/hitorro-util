package ht.util.json.keys;

import ht.util.json.keys.mappers.JsonNodeToUrl;
import ht.util.json.keys.propaccess.Propaccess;

import java.net.URL;

/**
 *
 */
public class URLProperty extends BaseMappingProperty<URL> {
    public URLProperty(String path, String description, URL defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToUrl.instance);
    }

    public URLProperty(Propaccess path, String description, URL defaultValue) {
        super(path, description, defaultValue, JsonNodeToUrl.instance);
    }
}