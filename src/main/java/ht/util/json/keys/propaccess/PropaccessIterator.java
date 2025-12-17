package ht.util.json.keys.propaccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.util.core.iterator.AbstractIterator;
import ht.util.json.JSONUtil;

import java.util.Arrays;
import java.util.Stack;

public class PropaccessIterator extends AbstractIterator<Propaccess> {
    private JsonNode root;
    private Propaccess access = new Propaccess("");
    private Stack<It> stack = new Stack();
    private It it;

    private boolean hasAnotherValue = true;

    public PropaccessIterator(JsonNode root) {
        this.root = root;
        it = It.getRecurse(root, stack);
        it.appendPath(access, stack);
    }


    @Override
    public boolean hasNext() {
        if (hasAnotherValue) {
            it.appendPath(access, stack);
        }
        return hasAnotherValue;
    }

    @Override
    public Propaccess next() {
        hasAnotherValue = false;
        while (stack.size() > 0) {
            if (stack.peek().next(stack)) {
                hasAnotherValue = true;
                break;
            } else {
                // doesnt have another value, lets remove it and see if its parent has a next value.
                stack.pop();
            }
        }
        return access;

    }
}

abstract class It {
    protected int size;
    protected int i;

    public static boolean isIterable(JsonNode node) {
        return node.isArray() || node.isObject();
    }

    public static It getRecurse(JsonNode node, Stack<It> stack) {
        It root = get(node, stack);
        It it = root;
        while (it != null) {
            it = It.get(it.get(), stack);
        }
        return root;
    }

    public static It get(JsonNode node, Stack<It> stack) {
        It it = null;

        if (node.isObject()) {
            it = new ObjectIt((ObjectNode) node);
        } else if (node.isArray()) {
            it = new ArrayIt((ArrayNode) node);
        }
        if (it != null) {
            stack.push(it);
        }
        return it;
    }

    Propaccess appendPath(Propaccess access, Stack<It> its) {
        access.setLength(0);
        for (int i = 0; i < its.size(); i++) {
            its.get(i).appendPath(access);
        }
        return access;
    }

    abstract Propaccess appendPath(Propaccess access);

    abstract JsonNode get();

    boolean next(Stack<It> stack) {
        if (i < size - 1) {
            i++;
            JsonNode node = get();
            if (It.isIterable(node)) {
                It it = It.getRecurse(node, stack);
                return it != null;
            }
            return true;
        }
        return false;
    }
}

class ArrayIt extends It {
    private ArrayNode node;

    ArrayIt(ArrayNode node) {
        this.node = node;
        size = node.size();
        i = 0;
    }

    JsonNode get() {
        return node.get(i);
    }

    Propaccess appendPath(Propaccess access) {
        access.getLast().setIndex(i);
        return access;
    }
}

class ObjectIt extends It {
    private ObjectNode node;
    private String keys[];

    public ObjectIt(ObjectNode node) {
        this.node = node;
        keys = new String[node.size()];
        size = node.size();
        JSONUtil.populateKeyValue(node, keys, null);
        Arrays.sort(keys);
    }

    JsonNode get() {
        return node.get(keys[i]);
    }

    Propaccess appendPath(Propaccess access) {
        JsonNode c = node.get(keys[i]);
        if (c != null) {
            if (c.isArray()) {
                access.append(new IndexedPart(keys[i]));
            } else {
                access.append(new Part(keys[i]));
            }
        }
        return access;
    }
}

