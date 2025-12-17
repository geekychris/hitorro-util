package ht.util.propertykeys;

public abstract class PropertySrc {
    private static PropertySrc pc = new MapPropertySrc();

    public static void set(PropertySrc pcIn) {
        pc = pcIn;
    }

    public static PropertySrc get() {
        return pc;
    }

    public abstract String get(String key);

    public abstract String resolve(final String key);
}
