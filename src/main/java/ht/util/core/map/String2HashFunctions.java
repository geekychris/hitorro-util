package ht.util.core.map;

import ht.util.core.hash.FPHash64;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.string.StringUtil;

/**
 *
 */
public class String2HashFunctions {
    public static BaseMapper<String, Long> string2hash = new BaseMapper<String, Long>() {
        @Override
        public Long apply(final String string) {
            return FPHash64.getFP(string);
        }
    };

    /**
     * take a string such as "the quick brown fox" and tokenizeFromSingleChar hashing each token seperately and combining at the end.
     * This is the same approach used in the phrase emitter of an analyzer.  This is still a naive implementation
     * because if the analyzer does any type of normalization it will not test!
     * <p/>
     * XXX Note that here we use the getFPViaChars method that does not utf-8 encode, but instead uses a char array.
     * This does not produce the same result, but is a much faster method especially in the tokenization chain
     */
    public static BaseMapper<String, Long> stringtokens2hash = new BaseMapper<String, Long>() {

        @Override
        public Long apply(final String string) {
            String[] parts = StringUtil.tokenizeFromSingleChar(string, " ");
            if (parts.length == 1) {
                return FPHash64.getFPViaChars(string);
            }
            long last = FPHash64.getFPViaChars(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                last = FPHash64.combineFingerPrints(last, FPHash64.getFPViaChars(parts[i]));
            }
            return last;
        }
    };
}
