package ht.util.core;

import ht.util.core.string.Fmt;

/**
 * Created by chris on 5/16/16.
 */
public class HTRuntimeException extends RuntimeException {
    public HTRuntimeException(String msg, Object... args) {
        super(Fmt.S(msg, args));
    }
}
