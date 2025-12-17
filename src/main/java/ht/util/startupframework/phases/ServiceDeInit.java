package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * De-loadAnnotation this module.  This must be a syncrhonous call. Either by defining a method called "deinit" or by
 * tagging a method with this annotation.
 *
 * @return null or a string to report an error on de-loadAnnotation
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceDeInit {
}

