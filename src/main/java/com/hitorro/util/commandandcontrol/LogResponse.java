/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.log.Logger;

/**
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
