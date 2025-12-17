package ht.jsontypesystem.dynamic.mappers;

import ht.util.core.string.StringUtil;

/**
 * Produces a hash of normalized text.
 * Normalized text in this case lower cases keeps only alpha numerics.  All spaces and other characters are removed.
 * Alpha characters are lower cased.
 * <p>
 * ht.jsontypesystem.dynamic.mappers.NormalizedTextHashMapper
 */
public class NormalizedTextHashMapper extends FPHashMapper {
    public long string2longMap(String s) {
        StringBuilder sb = new StringBuilder();
        StringUtil.normalizeText(s, sb);
        return super.string2longMap(sb.toString());
    }
}
