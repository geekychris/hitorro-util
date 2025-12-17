package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.dynamic.DynamicFieldMapper;
import ht.util.core.map.MapUtil;
import ht.util.core.string.Fmt;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.CollectionProperty;
import ht.util.json.keys.JsonInitableProperty;
import ht.util.json.keys.propaccess.Propaccess;
import ht.util.typesystem.FieldBaseIntf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 */
public class Field extends BaseT implements FieldBaseIntf {
    public static final BooleanProperty i18nKey = new BooleanProperty("i18n", "", false);
    public static final BooleanProperty vectorKey = new BooleanProperty("vector", "", false);
    public static JsonInitableProperty<DynamicFieldMapper> dynamicFieldMapperKey = new JsonInitableProperty("dynamic", "", null, DynamicFieldMapper.class, null);
    public static JsonInitableProperty<Group> groupKey = new JsonInitableProperty("", "", null, Group.class, Group.class);
    public static CollectionProperty<Group> groupsKey = groupKey.collection("groups", "", new ArrayList());
    private Type type;
    private boolean vector;
    private Map<String, Collection<Group>> groups = new HashMap();

    private DynamicFieldMapper dynamicFieldMapper = null;
    private boolean i18n = false;

    public void visit(TypeVisitor visitor, Predicate<BaseT> filter, Propaccess path) {
        type.visit(visitor, filter, path);
        for (Collection<Group> gs : groups.values()) {
            for (Group g : gs) {
                if (filter.test(g)) {
                    visitor.enterGroup(this, g, path);
                    g.visit(visitor, filter, path);
                    visitor.leaveGroup(this, g, path);
                }
            }
        }
    }

    public boolean isI18n() {
        return i18n;
    }

    public Class getImplementingClass() {
        if (type.isPrimitiveType()) {
            type.getPrimitiveType().getSerializedClass();
        }
        return null;
    }

    @Override
    public boolean init(final JsonNode node) {
        boolean flag = super.init(node);
        this.type = JsonTypeSystem.getMe().getType(typeKey.apply(node));
        this.vector = vectorKey.apply(node);
        this.dynamicFieldMapper = dynamicFieldMapperKey.apply(node);
        this.i18n = i18nKey.apply(node);
        Collection<Group> g = groupsKey.apply(node);
        MapUtil.addToMapArray(groups, g, Group::getName);
        return true;
    }

    public Group getDefaultGroupFor(String groupName) {
        Collection<Group> gr = groups.get(groupName);
        if (gr != null) {
            Group first = null;
            for (Group g : gr) {
                if (first == null) {
                    first = g;
                }
                if (g.isDefault()) {
                    return g;
                }
            }
            return first;
        }
        return null;
    }

    public boolean isVector() {
        return vector;
    }

    public DynamicFieldMapper getDynamicFieldMapper() {
        return dynamicFieldMapper;
    }

    public boolean isDynamic() {
        return dynamicFieldMapper != null;
    }

    public String toString() {
        if (getType() != null) {
            return Fmt.S("Field: %s, type: %s", getName(), getType().getName());
        }
        return null;
    }

    public Type getType() {
        return type;
    }

    public Collection<Group> getGroup(String name) {
        return groups.get(name);
    }


}
