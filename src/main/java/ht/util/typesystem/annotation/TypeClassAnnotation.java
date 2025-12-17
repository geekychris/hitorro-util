package ht.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 13, 2006 Time: 11:10:26 AM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeClassAnnotation {
    String shortName();
}
