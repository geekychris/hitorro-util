package ht.util.json.mapper;

import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.string.StringUtil;
import ht.util.json.JSONElement;
import ht.util.json.JSONNumber;
import ht.util.json.JSONType;

/**
 * Keep a min max for two jason fields defined as the min and max fields.  It assumes they are number fields. You can
 * reset the min / max after a run.  Useful for such things as tracking the largest and smalled dates seen in integer /
 * long form.
 */
public class LongMinMaxJSONMapper extends BaseMapper<JSONElement, JSONElement> {
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private String minPath[] = null;
    private String maxPath[] = null;

    public LongMinMaxJSONMapper(String minPath, String maxPath) {
        if (!StringUtil.nullOrEmptyString(minPath)) {
            this.minPath = StringUtil.tokenizeFromSingleChar(minPath, ".");
        }
        if (!StringUtil.nullOrEmptyString(maxPath)) {
            this.maxPath = StringUtil.tokenizeFromSingleChar(maxPath, ".");
        }
    }

    public void reset() {
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    @Override
    public JSONElement apply(final JSONElement e) {
        if (minPath != null) {
            JSONElement minE = e.getFromPath(minPath, 0, minPath.length);
            if (minE != null && minE.getJSONType() == JSONType.Number) {
                long l = ((JSONNumber) minE).get().longValue();
                if (l < min) {
                    min = l;
                }
            }
        }
        if (maxPath != null) {
            JSONElement maxE =
                    e.getFromPath(maxPath, 0, maxPath.length);
            if (maxE != null && maxE.getJSONType() == JSONType.Number) {
                long l = ((JSONNumber) maxE).get().longValue();
                if (l > max) {
                    max = l;
                }
            }
        }
        return e;
    }
}
