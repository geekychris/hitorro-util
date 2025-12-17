package ht.util.core.iterator.mappers;

import java.util.function.Function;

public class String2Integer implements Function<String, Integer> {
    public static String2Integer me = new String2Integer();

    @Override
    public Integer apply(final String s) {
        return Integer.parseInt(s);
    }
}
