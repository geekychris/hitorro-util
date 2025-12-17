package ht.util.commandandcontrol.ano;

import ht.util.commandandcontrol.RestOperations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionLevelCommandDef {
    /**
     * The name of the command such as "env.time"
     *
     * @return
     */
    String command();

    /**
     * Detailed description of the command.
     *
     * @return
     */
    String description();

    /**
     * should this command be treated as invisible to the outside world?
     *
     * @return
     */
    boolean isInternal() default true;

    /**
     * Determines what kind of rest calls can be used with this.  It is possible that a command can support such things
     * as Post or put (requires extra coding on the part of the programmer)
     *
     * @return
     */
    RestOperations[] restOperations() default {RestOperations.Get};

    /**
     * class to be used to convert the response into a command response.
     *
     * @return
     */
    Class resultResponseConverter();
}
