package ht.util.core.iterator.mappers;


/**
 * Cleans up a string by removing non alpha and possibly numeric chars.
 */
public class ConditionStringMapper extends BaseMapper<String, String> {
    public static final ConditionStringMapper alpha = new ConditionStringMapper(true, true, ConditionStringMapper.Keep.Alpha);
    private StringBuilder sb = new StringBuilder();
    private Keep keep;
    private boolean lowerCase;
    private boolean trim;
    public ConditionStringMapper(boolean lowerCase, boolean trim, Keep keep) {
        this.keep = keep;
        this.lowerCase = lowerCase;
        this.trim = trim;
    }

    public boolean isThreadSafe() {
        return false;
    }

    public BaseMapper getCopy() {
        return new ConditionStringMapper(lowerCase, trim, keep);
    }

    @Override
    public String apply(final String e) {
        if (e == null) {
            return null;
        }
        sb.setLength(0);
        char c;
        for (int i = 0; i < e.length(); i++) {
            c = e.charAt(i);
            if (keep.keep(c)) {
                if (lowerCase) {
                    c = Character.toLowerCase(c);
                }
                sb.append(c);
            }
        }
        if (trim) {
            return sb.toString().trim();
        }
        return sb.toString();
    }

    public enum Keep {
        Alpha() {
            public boolean keep(char c) {
                return Character.isLetter(c) || Character.isSpaceChar(c);
            }

        },
        Numeric() {
            public boolean keep(char c) {
                return Character.isDigit(c) || Character.isSpaceChar(c);
            }

        },
        AlphaNumeric() {
            public boolean keep(char c) {
                return Character.isLetter(c) || Character.isDigit(c) || Character.isSpaceChar(c);
            }

        };

        public abstract boolean keep(char c);


    }
}
