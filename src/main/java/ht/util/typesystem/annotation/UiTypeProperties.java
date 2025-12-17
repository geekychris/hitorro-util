package ht.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * User Interface properties available for a type. User: chris Date: Oct 25, 2006 Time: 9:33:50 AM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface UiTypeProperties {
    /**
     * The user-friendly name of the type.
     *
     * @return
     */
    public String name();

    /**
     * The views of this class.
     *
     * @return an array of the view references for the class.
     */
    public ViewClassReference[] views() default {};

    /**
     * The name of the custom edit page class.
     */
    public String customEditPage() default "";
}
