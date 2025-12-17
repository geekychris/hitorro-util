package ht.util.core;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 18, 2004 Time: 9:30:01 PM
 */
public class CharArrayWrapper implements PooledStorageElement {
    private int size = 0;
    private char array[];

    private CharArrayWrapper() {

    }

    private CharArrayWrapper(char[] arrayIn) {
        size = arrayIn.length;
        array = arrayIn;
    }

    public static CharArrayWrapper getWrapper(int initialSize) {
        return new CharArrayWrapper(new char[initialSize]);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int sizeIn) {
        size = sizeIn;
    }

    public char[] getArray() {
        return array;
    }

    public int getRealStorageSize() {
        return array.length;
    }

    public void activate() {

    }

    public void pasivate() {
        ArrayUtil.nullOutCharArray(array);
        // return to free storage here!
    }


}
