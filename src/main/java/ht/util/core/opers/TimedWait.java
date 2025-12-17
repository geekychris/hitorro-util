package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class TimedWait<T> implements HTPredicate<T> {
    private long startTime = System.currentTimeMillis();
    private long exitTime;

    public TimedWait(long waitInMillis) {
        exitTime = startTime + waitInMillis;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "TimedWait.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(T t) {
        return exitTime <= System.currentTimeMillis();
    }
}