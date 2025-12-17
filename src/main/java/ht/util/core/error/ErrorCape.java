package ht.util.core.error;

/**
 * Created by chris on 7/21/17.
 */
public interface ErrorCape {
    Errors getErrors();

    default boolean hasErrors() {
        return getErrors().hasErrors();
    }
}
