package ht.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Alternative method for identifying a method for registering "hooks.  Method must take one boolean value which is true
 * if in dbinit mode. Alternatively use the method:
 * <p/>
 * public String registerHooks (boolean dbinit)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterHooks {
}