package ht.util.core;

import ht.util.core.string.Fmt;

/**
 * Created by chris on 5/16/16.
 */
public class HTException extends RuntimeException {
    public HTException(Exception e) {
        super(e);
    }

    public HTException(String msg, Object... args) {
        super(Fmt.S(msg, args));
    }
}
