package ht.util.core.map;

/**
 *
 */
public interface HashHashMapFactory<LAYER1TYPE extends Object, LAYER2TYPE extends Object, PAYLOAD extends Object> {
    public PAYLOAD create(LAYER1TYPE m, LAYER2TYPE n);

    public void remove(LAYER1TYPE m, LAYER2TYPE n, PAYLOAD p);
}

