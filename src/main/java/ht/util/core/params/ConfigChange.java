package ht.util.core.params;

import ht.util.core.string.Fmt;

/**
 * User: chris
 */
public class ConfigChange {
    private ConfigChangeType type;
    private String key;

    public ConfigChange(ConfigChangeType t, String key) {
        type = t;
        this.key = key;
    }

    public ConfigChangeType getType() {
        return type;
    }

    public void setType(ConfigChangeType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String toString() {
        return Fmt.S("%s : %s", type, key);
    }

    public enum ConfigChangeType {
        Added, Deleted, Updated
    }
}
