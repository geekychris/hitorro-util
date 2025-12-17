package ht.util.core.error;

/**
 * Created by chris on 7/21/17.
 */
public enum ErrorSeverity {
    fatal(4), error(3), warn(2), info(1), debug(0);

    private int level;

    ErrorSeverity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
