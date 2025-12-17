package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a property key that is required by a service.  Each property key has its validate() method called to ensure
 * it has a satisfactory value before initializing.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProperty {
}
