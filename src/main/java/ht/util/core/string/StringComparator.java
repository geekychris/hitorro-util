package ht.util.core.string;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {
    public static StringComparator me = new StringComparator();

    public int compare(final String s, final String s1) {
        return s.compareTo(s1);
    }
}