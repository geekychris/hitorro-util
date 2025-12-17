package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.params.HTProperties;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.List;
import java.util.Map;

/**
 * Get the child keys as a listFiles of strings
 *
 * @author chris
 */
public class ChildKey extends PropertyKey<List<String>> {
    public ChildKey(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
    }

    public ChildKey(String key, String description) {
        super(key, description);
    }

    @Override
    public String getPropertyType() {
        return "List";
    }

    public List<String> getList(Map<String, String> map) {
        return HTProperties.getProperties().getChildKeys(this.m_key);
    }

    @Override
    public List<String> apply(Map<String, String> map) {
        return getList(map);
    }

    @Override
    protected void validate(String sVal) throws PropertyKeyValidationException {
        //not used.
    }

    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        if (getList(map) == null) {
            throw new PropertyKeyValidationException("List not available ", this.m_key, "<<null>>");
        }
    }

}
