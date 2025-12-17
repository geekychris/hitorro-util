package ht.util.commandandcontrol.ano;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a property key that either required or optional for the command.  This means that it is used in the
 * validation of the argument set.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandArgument {
    /**
     * Determines if the argument is required.
     *
     * @return
     */
    boolean required() default true;
}
