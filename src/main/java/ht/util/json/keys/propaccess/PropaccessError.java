package ht.util.json.keys.propaccess;

import ht.util.core.HTException;

public class PropaccessError extends HTException {
    public PropaccessError(final String msg, final Object... args) {
        super(msg, args);
    }
}
