package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Initialize this module and also any modules that it depends on. This must be a syncrhonous call. Can be called either
 * via using the function "init" or referring to the method with this annotation.
 * <p/>
 * public String init (boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion)
 *
 * @param dbInit         true if we are performing a database loadAnnotation / re-loadAnnotation. This is not normal
 *                       startup but performed during an initdb=true
 * @param upgrading
 * @param currentVersion
 * @param targetVersion  @return null if initialized ok, else an error message
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceInit {
}
