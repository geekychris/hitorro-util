package ht.util.typesystem;

/**
 *
 */
public abstract class TypeManagerBase {
    private static TypeManagerBase tmb;

    public static TypeManagerBase get() {
        return tmb;
    }

    public static void set(TypeManagerBase t) {
        tmb = t;
    }

    public abstract TypeIntf getTypeForBaseType(Object bt);

    public abstract TypeIntf getTypeByShortName(String name);
}
