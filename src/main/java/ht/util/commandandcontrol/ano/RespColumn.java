package ht.util.commandandcontrol.ano;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Definition of a column in a ResponseDefinition.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RespColumn {
    /**
     * Short name of the column rendered in xml
     *
     * @return
     */
    String name();

    /**
     * Long name of the column.  Rendered in the terminal
     *
     * @return
     */
    String lName();

    /**
     * Type of value in the field.  Even though it is rendered as text for transport, this allows interpretation of the
     * element appriately.
     *
     * @return
     */
    Class type() default String.class;
}
