package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Alternative way to define what services the service depends on.  You can: a) define these in the Service definition
 * b) use the method: List<Class> getDependentService() c) tag a method that is of the format: List<Class> xxxx ()
 * <p/>
 * This provides the classes that we assume are services that this service must have initialized first.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceDependencies {
}
