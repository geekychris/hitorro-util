package ht.util.core;

import ht.util.core.date.DateRange;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

import java.text.ParseException;

/**
 *
 */
public class ShardKey {
    private DateRange dateRange;
    private String fullName;
    private String subSegment;

    public ShardKey(String fullName) throws ParseException {
        this.fullName = fullName;
        String parts[] = StringUtil.tokenizeFromSingleChar(fullName, "-");
        if (parts.length >= 2) {
            dateRange = new DateRange(parts[0], parts[1]);
            if (parts.length > 2) {
                subSegment = parts[2];
            }
        } else {
            throw new ParseException("not enough parts to name", parts.length);
        }
        finalizeShardKey();
    }

    public String toString() {
        return fullName;
    }

    public String getFullName() {
        return fullName;
    }

    private void finalizeShardKey() {
        fullName = Fmt.S("%s-%s", dateRange.getDateRangeString(), subSegment);
    }

    public String getSubSegment() {
        return subSegment;
    }

    public DateRange getDateRange() {
        return dateRange;
    }
}
