package ht.util.core;

/**
 * Implement this guy if you want to compare two objects that have a composite sorting mechanism
 */
public interface JavaObjectOrderEnumCompInterface {
    public Object[] getSortFrame(JavaObjectOrderEnum[] barrel, String names[], int length);

    public void fillFrameFromObject(JavaObjectOrderEnum types[], String names[], int length);

    public JavaObjectOrderEnum[] getBarrelSorter();
}
