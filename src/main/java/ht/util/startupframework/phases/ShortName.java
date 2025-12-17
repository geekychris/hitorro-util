package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provide a short name for the service alternatively can be provided in the ServiceDefinition or implementing: String
 * getName()
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ShortName {
}
