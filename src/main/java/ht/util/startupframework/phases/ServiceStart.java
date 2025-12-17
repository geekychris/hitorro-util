package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * All services once initialized get a second call to tell them that all services have been started. This is useful for
 * such things that require to separate out initialization from spawning threads for instance (if there are some kind of
 * cyclic dependencies). Either called via the start(dbinit) method or by identifying an alternate method name and
 * tagging with this annotation.
 * <p/>
 * public String start (boolean dbInit)
 *
 * @param dbInit true if we are performing a database loadAnnotation / re-loadAnnotation. This is not normal startup but
 *               performed during an initdb=true
 * @return null if initialized ok, else an error message
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceStart {
}
