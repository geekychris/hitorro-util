package ht.util.typesystem.annotation;

import ht.util.typesystem.OnTrigger;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 16, 2006 Time: 4:54:16 PM
 */
public @interface ImplClassMeta {
    Class className();

    OnTrigger.TriggerType trigger();
}
