/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;


import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * General ArrayList utilities
 *
 * @author chris
 */
public class ArrayUtil {

    public static final ArrayList<String> emptyStringArrayList = new ArrayList();

    public static <K> K[] ensureCapacity(Class c, int l, int oldPartsLength, K oldParts[]) {
        if (oldParts.length < l) {

            K newPart[] = (K[]) Array.newInstance(c, l * 2);
            for (int i = 0; i < oldPartsLength; i++) {
                newPart[i] = oldParts[i];
            }
            return newPart;
        }
        return oldParts;
    }

    /**
     * return the number of elements in common
     *
     * @param a1
     * @param a2
     * @return
     */
    public static int compareArrays(Object a1[], Object a2[]) {
        int depth = Math.min(a1.length, a2.length);
        for (int i = 0; i < depth; i++) {
            if (!a1[i].equals(a2[i])) {
                //
                return i;
            }
        }
        return a1.length;
    }

    /**
     * Ensure all elements not null
     *
     * @param arr
     * @return
     */
    public static boolean allElementsNotNull(Object... arr) {
        for (Object o : arr) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ensure any element is not null
     *
     * @param arr
     * @return
     */
    public static boolean anyElementNotNull(Object... arr) {
        for (Object o : arr) {
            if (o != null) {
                return true;
            }
        }
        return false;
    }

    public static int sizeOfArrayIgnoringNull(Object arr[]) {
        if (arr == null) {
            return 0;
        }
        return arr.length;
    }

    public static int copy(Object target[], int startPos, Object src[]) {
        if (src == null) {
            return 0;
        }
        for (int i = 0; i < src.length; i++) {
            target[startPos + i] = src[i];
        }
        return src.length;
    }

    public static final boolean notNullAndHasNElements(Object ar[], int count) {
        if (ar == null) {
            return false;
        }
        return ar.length == count;
    }

    public static final long[] getLongArrayFromSeperatedString(String s, String seperator) {
        String parts[] = StringUtil.tokenizeFromSingleChar(s, seperator);
        return getLongArrayFromStringArray(parts);
    }

    public static final long[] getLongArrayFromStringArray(String ar[]) {
        long res[] = new long[ar.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = StringUtil.getLongNumberFromText(ar[i]);
        }
        return res;
    }

    public static final int[] getIntArrayFromStringArray(String ar[]) {
        int res[] = new int[ar.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = StringUtil.getIntNumberFromText(ar[i]);
        }
        return res;
    }


    /**
     * Probably not the most optimal mechanism to look for the existence of an object in an object listFiles.
     *
     * @param array
     * @param scanMe
     * @return true if object exists.
     */
    public static final boolean contains(Object array[], Object scanMe) {
        if (scanMe == null) {
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            if (scanMe.equals(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * determine if a value exists in a array of longs.
     *
     * @param array
     * @param value
     * @return
     */
    public static final boolean containsInLongArray(long array[], long value) {
        return getFirstIndexInLongArray(array, value) != -1;
    }

    /**
     * determine if a value exists in a array of int.
     *
     * @param array
     * @param value
     * @return
     */
    public static final boolean containsInIntArray(int array[], int value) {
        return getFirstIndexInIntArray(array, value) != -1;
    }


    /**
     * Copies from source to result arrays only those objects in source that have a corresponding bit set to true in the
     * mask.
     *
     * @param source
     * @param result
     * @param mask
     * @return
     */
    public static final boolean copyArrayUsingBitmask(int source[],
                                                      int result[], boolean mask[]) {
        if (source.length != mask.length) {
            // mask is not the same as source...that is bad
            return false;
        }
        int resultIndex = 0;
        for (int i = 0; i < source.length; i++) {
            if (mask[i] == true) {
                if (resultIndex == result.length) {
                    // we dont have enough space to continue
                    return false;
                }
                // copy the object over
                result[resultIndex++] = source[i];
            }
        }
        return true;
    }

    /**
     * Copies from source to result arrays only those objects in source that have a corresponding bit set to true in the
     * mask.
     *
     * @param source
     * @param result
     * @param mask
     * @return
     */
    public static final boolean copyArrayUsingBitmask(Object source[],
                                                      Object result[], boolean mask[]) {
        if (source.length != mask.length) {
            // mask is not the same as source...that is bad
            return false;
        }
        int resultIndex = 0;
        for (int i = 0; i < source.length; i++) {
            if (mask[i] == true) {
                if (resultIndex == result.length) {
                    // we dont have enough space to continue
                    return false;
                }
                // copy the object over
                result[resultIndex++] = source[i];
            }
        }
        return true;
    }

    public static final byte[] copyByteArray(byte[] source, byte[] dest, int size) {
        for (int i = 0; i < size; i++) {
            dest[i] = source[i];
        }
        return dest;
    }

    /*
        Copy a byte array to a char array (8 ->16 bits....we are assuming that the
        byte array contains ascii characters.

        Further we remove any non alpha numeric characters and lower case everything.
    */

    public static int copyByteArrayToCharNormalizing(byte[] array, char[] result) {
        int j = 0;
        for (int i = 0; i < array.length; i++) {
            char c = (char) array[i];
            if (Character.isDigit(c) || Character.isLetter(c)) {
                result[j++] = Character.toLowerCase(c);
            }
        }
        return j;
    }

    public static final char[] copyCharArray(char[] source, char[] dest, int size) {
        for (int i = 0; i < size; i++) {
            dest[i] = source[i];
        }
        return dest;
    }

    public static final int[] copyIntegerArray(int[] source, int[] dest, int size) {
        for (int i = 0; i < size; i++) {
            dest[i] = source[i];
        }
        return dest;
    }

    public static final long[] copyLongArray(long[] source, long[] dest, int size) {
        for (int i = 0; i < size; i++) {
            dest[i] = source[i];
        }
        return dest;
    }


    public static final String delimiterSeperateArray(int[] array, String delimeter) {
        StringBuffer buf = new StringBuffer();
        int endPoint = array.length - 1;
        for (int i = 0; i <= endPoint; i++) {
            buf.append(array[i]);
            if (i != endPoint) {
                buf.append(delimeter);
            }
        }
        return buf.toString();
    }

    public static final String delimiterSeperateArray(String[] array, String delimeter) {
        StringBuffer buf = new StringBuffer();
        int endPoint = array.length - 1;
        for (int i = 0; i <= endPoint; i++) {
            buf.append(array[i]);
            if (i != endPoint) {
                buf.append(delimeter);
            }
        }
        return buf.toString();
    }

    /**
     * Get the first occurence of an int in an int array
     *
     * @param array of ints
     * @param value to scan for
     * @return -1 if nothing found or index of first occurence.
     */
    public static final int getFirstIndexInIntArray(int array[], int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the first occurence of an long in an long array
     *
     * @param array of longs
     * @param value to scan for
     * @return -1 if nothing found or index of first occurence.
     */
    public static final int getFirstIndexInLongArray(long array[], long value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the first occurence of a String in an String array
     *
     * @param array of Strings
     * @param value to scan for
     * @return -1 if nothing found or index of first occurence.
     */
    public static final int getFirstIndexInStringArray(String array[],
                                                       String value, boolean caseSensitive) {
        if (caseSensitive) {
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals(value)) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (array[i].equalsIgnoreCase(value)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Given a List of Integer objects, return an array of int's
     *
     * @param list to convertToPdf into an int array
     * @return int array containing int values.
     */
    public static final int[] getIntArrayFromIntegerList(List list) {
        int size = list.size();
        int result[] = new int[size];
        for (int i = 0; i < size; i++) {
            Integer val = (Integer) list.get(i);
            result[i] = val.intValue();
        }
        return result;
    }

    /**
     * Given a List of Integer objects, return an array of int's
     *
     * @param list to convertToPdf into an int array
     * @return int array containing int values.
     */
    public static final List getIntegerListFromIntArray(int[] list) {
        int size = list.length;
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            result.add(Constants.getInteger(list[i]));
        }
        return result;
    }

    /**
     * Given a List of String objects, return an array of Strings
     *
     * @param list to convertToPdf into an string array
     * @return string array containing string values.
     */
    public static final String[] getStringArrayFromStringList(List list) {
        int size = list.size();
        String result[] = new String[size];

        for (int i = 0; i < size; i++) {
            String val = (String) list.get(i);
            result[i] = val;
        }

        return result;
    }

    /**
     * Determine if the 1st listFiles contains all elements from the 2nd listFiles. The implementation assumes the lists are small
     * (e.g., < 10 elements each)
     *
     * @param parent the 1st listFiles
     * @param child  the 2nd listFiles
     * @return true iff 2nd listFiles is covered by 1st listFiles
     */
    public static final boolean listContains(int[] parent, int[] child) {
        if (parent.length < child.length) {
            return false;
        } else {
            for (int celt : child) {
                boolean found = false;
                for (int pelt : parent) {
                    if (celt == pelt) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final byte[] loadByteArrayFromFile(File file) throws IOException {
        byte[] b = new byte[(int) file.length()];
        DataInputStream dis = FileUtil.getDataInputStreamForFile(file);
        int i = 0;
        while (dis.available() > 0) {
            b[i] = dis.readByte();
            i++;
        }
        return b;
    }

    public static final int[] loadIntArrayFromFile(File file) throws IOException {
        int[] l = new int[(int) file.length() / 4];
        DataInputStream dis = FileUtil.getDataInputStreamForFile(file);
        int i = 0;
        while (dis.available() > 0) {
            l[i] = dis.readInt();
            i++;
        }
        return l;
    }

    public static final long[] loadLongArrayFromFile(File file) throws IOException {
        long[] l = new long[(int) file.length() / 8];
        DataInputStream dis = FileUtil.getDataInputStreamForFile(file);
        int i = 0;
        while (dis.available() > 0) {
            l[i] = dis.readLong();
            i++;
        }
        return l;
    }

    /**
     * Merge two arrays
     *
     * @param ar1
     * @param ar2
     * @return the array that contains elements from both arrays. Element order is preserved.
     */
    public static final <T> T[] mergeArrays(Class type, T ar1[], T ar2[]) {

        T ar[] = (T[]) Array.newInstance(type, ar1.length + ar2.length);
        int idx = 0;
        for (int i = 0; i < ar1.length; ++i) {
            ar[idx++] = ar1[i];
        }
        for (int i = 0; i < ar2.length; ++i) {
            ar[idx++] = ar2[i];
        }
        return ar;
    }

    /**
     * Merge two int arrays
     *
     * @param ar1
     * @param ar2
     * @return the array that contains elements from both arrays. Element order is preserved.
     */
    public static final int[] mergeArrays(int ar1[], int ar2[]) {
        int ar[] = new int[ar1.length + ar2.length];
        int idx = 0;
        for (int i = 0; i < ar1.length; ++i) {
            ar[idx++] = ar1[i];
        }
        for (int i = 0; i < ar2.length; ++i) {
            ar[idx++] = ar2[i];
        }
        return ar;
    }

    /**
     * test that if the array is null or contains no values.
     *
     * @param array
     * @return true if null or empty
     */
    public static final boolean nullOrEmpty(Object[] array) {
        return array == null || array.length <= 0;
    }


    /*
        empty out a char array.  I am sure there are quicker ways todo this.
    */

    public static void nullOutCharArray(char[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (char) 0;
        }
    }


    /**
     * Resort the elements in the 2nd listFiles based on the order specified in the 1st listFiles. The 1st listFiles must contain all
     * elements in the 2nd listFiles.
     * <p/>
     * The implementation assumes the lists are small (e.g., < 10 elements)
     *
     * @param order the 1st listFiles
     * @param elts  the 2nd listFiles
     * @return resorted 2nd listFiles
     */
    public static final int[] resort(int[] order, int[] elts) {
        int[] newlist = new int[elts.length];
        int idx = 0;
        for (int oelt : order) {
            for (int elt : elts) {
                if (oelt == elt) {
                    newlist[idx++] = elt;
                    break;
                }
            }
        }
        return newlist;
    }

    /**
     * remove the 1st element of the array by shifting every element to the left.
     *
     * @param source
     * @return new array
     */
    public static Object[] shiftArrayLeft(Object source[]) {
        if (source.length > 0) {
            Object target[] = new Object[source.length - 1];
            for (int i = 0; i < source.length - 1; ++i) {
                target[i] = source[i + 1];
            }
            return target;
        } else {
            // return the array as is, this should be safe since you can't
            // modify a zero-length array in place
            return source;
        }
    }

    /**
     * Convert an array to a listFiles
     *
     * @param ar
     * @return list
     */
    @SuppressWarnings("unchecked")
    public static final List<Object> toList(Object ar[]) {
        ArrayList list = new ArrayList();
        for (Object o : ar) {
            list.add(o);
        }
        return list;
    }

    /**
     * Convert a listFiles of objects to longs. This assumes that each string form of the object can be parsed into a long.
     *
     * @param list
     * @return list of longs
     */
    public static final long[] toLong(Object list[]) {
        long[] result = new long[list.length];
        for (int i = 0; i < list.length; i++) {
            String s = list[i].toString();
            try {
                result[i] = Long.parseLong(s);
            } catch (NumberFormatException nfe) {

            }
        }
        return result;
    }

    public static String printIntArray(int arr[][], int pack, boolean desc) {
        StringBuilder sb = new StringBuilder();
        printIntArray(sb, arr, pack, desc);
        return sb.toString();

    }

    public static void printIntArray(StringBuilder sb, int arr[][], int pack, boolean desc) {
        int x = arr.length;
        int y = arr[x - 1].length;
        if (desc) {
            for (int i = x - 1; i >= 0; i--) {
                printIntArrayAux(sb, arr[i], pack, y);
            }
        } else {
            for (int i = 0; i < x; i++) {
                printIntArrayAux(sb, arr[i], pack, y);
            }
        }
    }

    private static void printIntArrayAux(final StringBuilder sb, final int[] ints, final int pack, final int y) {
        for (int j = 0; j < y; j++) {
            if (j > 0) {
                sb.append(",");
            }
            String a = Integer.toString(ints[j]);
            sb.append(StringUtil.padToLength(' ', pack - a.length()));
            sb.append(a);
        }
        Console.bprintln(sb);
    }
}
