package ht.jsontypesystem.predicates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ht.util.core.opers.AlwaysTrueOperator;
import ht.util.core.opers.HTPredicate;
import ht.util.json.keys.BaseMappingProperty;
import ht.util.json.keys.propaccess.PAContext;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public class ForEach {
    protected ForEach root = null;
    protected ForEach next;
    protected Propaccess access;
    private Predicate<JsonNode> predicate;

    public ForEach(Propaccess access) {
        this(access, AlwaysTrueOperator.oper);
    }

    public ForEach(Propaccess access, Predicate<JsonNode> predicate) {
        this.access = access;
        this.predicate = predicate;
        setRoot(root);
    }

    ForEach getRoot() {
        return root;
    }

    void setRoot(ForEach root) {
        if (root == null) {
            this.root = this;
        } else {
            this.root = root;
        }
    }

    public ForEach forEach(Propaccess access) {
        return forEach(access, AlwaysTrueOperator.oper);
    }

    public ForEach forEach(Propaccess access, Predicate<JsonNode> predicate) {
        ForEach fe = new ForEach(access, predicate);
        next = fe;
        fe.setRoot(root);
        return fe;
    }

    public <T> BaseMappingProperty<T> mapToProperty(BaseMappingProperty<T> property) {
        BaseMappingProperty bmp = new BaseMappingProperty(new Propaccess(""), property.getDescription(), property.getDefault(), property.getFunction()) {
            public JsonNode getNode(JsonNode node) {
                return root.match(node);
            }
        };
        return bmp;
    }

    public HTPredicate<JsonNode> getPredicate() {
        return new HTPredicate<JsonNode>() {
            @Override
            public boolean test(final JsonNode jsonNode) {
                JsonNode jn = match(jsonNode);
                return jn != null;
            }
        };
    }

    public JsonNode match(JsonNode root) {
        return match(root, null);
    }

    public JsonNode match(JsonNode root, JsonNode replace) {
        return getRoot().matchAux(root, replace, false);
    }

    public JsonNode remove(JsonNode root) {
        return getRoot().matchAux(root, null, true);
    }

    public <E> void visit(JsonNode root, Collection<E> coll) {
        getRoot().visitAux(root,
                coll,
                null);
    }

    public <E> void visit(JsonNode root, Collection<E> coll, Function<JsonNode, E> map) {
        getRoot().visitAux(root, coll, map);
    }

    public <E> void visitAux(JsonNode root, Collection<E> coll, Function<JsonNode, E> map) {
        JsonNode e = access.get(null, root, PAContext.NeverCreate);
        if (e != null && e.isArray()) {
            ArrayNode an = (ArrayNode) e;
            int s = e.size();
            for (int i = 0; i < s; i++) {
                JsonNode n = an.get(i);
                if (predicate.test(n)) {
                    if (next != null) {
                        next.visitAux(n, coll, map);
                    } else {
                        if (map != null)
                            coll.add(map.apply(n));
                        else
                            coll.add((E) n);
                    }
                }
            }
        }
    }

    private JsonNode matchAux(JsonNode root, JsonNode replace, boolean remove) {
        JsonNode e = access.get(null, root, PAContext.NeverCreate);
        if (e != null && e.isArray()) {
            ArrayNode an = (ArrayNode) e;
            int s = e.size();
            for (int i = 0; i < s; i++) {
                JsonNode n = an.get(i);
                if (predicate.test(n)) {
                    if (next != null) {
                        return next.matchAux(n, replace, remove);
                    }

                    if (replace != null) {

                        an.set(i, replace);
                        return replace;
                    }
                    if (remove) {
                        an.remove(i);
                    }
                    return n;
                }
            }
            if (replace != null) {
                return an.add(replace);
            }
        }
        if (replace != null) {
            return access.appendObject(null, root, PAContext.AlwaysCreate, replace);
        }
        return null;
    }
}
