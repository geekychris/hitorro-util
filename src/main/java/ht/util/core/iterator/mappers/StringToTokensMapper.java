package ht.util.core.iterator.mappers;

import ht.util.core.string.StringUtil;

/**
 * Given a string, convert it to an array of strings.
 */
public class StringToTokensMapper extends BaseMapper<String, String[]> {
    public static StringToTokensMapper commaMapper = new StringToTokensMapper(",");
    public static StringToTokensMapper spaceMapper = new StringToTokensMapper(" ");

    private String seperator;

    public StringToTokensMapper(String seperator) {
        this.seperator = seperator;

    }

    @Override
    public String[] apply(final String e) {
        return StringUtil.tokenizeFromSingleChar(e, seperator);
    }
}