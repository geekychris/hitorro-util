package ht.util.commandandcontrol;

import ht.util.core.string.StringUtil;
import ht.util.log.Logger;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 31, 2006 Time: 6:52:23 PM
 */
public class LogResponse extends Response {
    private Logger logger;

    public LogResponse(Logger logger) {
        this.logger = logger;
    }

    public void addBannerRow(String row) {
        logger.info(row);
    }

    public void addHeader(String... columnHeaders) {
        addHeaderArray(columnHeaders);
    }

    public void addHeaderArray(String columnHeaders[]) {
        logger.info(StringUtil.mergeWithJoinToken(columnHeaders, ""));
    }

    public void addHeaderShortNames(String... columnHeaders) {
        addHeaderShortNamesArray(columnHeaders);
    }

    public void addHeaderShortNamesArray(String columnHeaders[]) {
        // do nothing
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    public void addRowArray(Object elements[]) {
        logger.info(StringUtil.mergeWithJoinToken(elements, ""));
    }

    public void addRowTypes(Class... types) {
        addRowTypesArray(types);
    }

    public void addRowTypesArray(Class types[]) {
        // Do nothing
    }


    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        logger.info("%s %s", info, percentComplete);
    }

    public void addInfo(InfoLevel level, String info) {
        switch (level) {
            case Info:
                logger.info(info);
                break;
            case Warn:
                logger.warn(info);
                break;
            case Error:
                logger.error(info);
                break;
        }
    }

    public void end() {
        logger.info("End");
    }
}
