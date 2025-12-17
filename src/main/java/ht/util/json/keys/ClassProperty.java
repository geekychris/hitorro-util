package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.classes.ClassUtil;
import ht.util.core.iterator.Mapper;
import ht.util.json.keys.propaccess.Propaccess;

public class ClassProperty extends BaseMappingProperty<Class> {
    public ClassProperty(String path, String description, Class defaultValue) {
        super(new Propaccess(path), description, defaultValue, ClassPropertyMapper.me);
    }

    public ClassProperty(Propaccess path, String description, Class defaultValue) {
        super(path, description, defaultValue, ClassPropertyMapper.me);
    }
}

class ClassPropertyMapper implements Mapper<JsonNode, Class> {
    public static ClassPropertyMapper me = new ClassPropertyMapper();

    ClassPropertyMapper() {

    }

    public Class apply(JsonNode jsonNodes) {
        String sValue = jsonNodes.textValue();

        return ClassUtil.getClassForName(sValue, null);
    }

}