package ht.util.commandandcontrol.ano;

import ht.util.json.keys.StringProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DebugArgAno {
    Class propType() default StringProperty.class;

    String keyName() default "";

    String description() default "";

    String defaultValue() default "";

    boolean mustExist() default true;

    ArgType argType() default ArgType.Regular;
}
