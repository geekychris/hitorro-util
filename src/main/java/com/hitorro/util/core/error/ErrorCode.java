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
package com.hitorro.util.core.error;

import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.string.Fmt;

import java.util.List;

public class ErrorCode {
    private int code;
    private String errorMessage;
    private Object args[];
    private ErrorSeverity severity;

    public ErrorCode(int code, String msg, Object... args) {
        this(ErrorSeverity.error, code, msg, args);
    }

    public ErrorCode(ErrorSeverity sev, int code, String msg, Object... args) {
        this.severity = sev;
        this.code = code;
        if (ArrayUtil.nullOrEmpty(args)) {
            this.args = new Object[0];
            errorMessage = msg;
        } else {
            this.args = args;
            errorMessage = Fmt.Sargs(msg, args);
        }
    }

    /**
     * Helper method to put errors to an error listFiles if condition holds true
     *
     * @param flag
     * @param ecList
     * @param code
     * @param msg
     * @param args
     * @return true if there was an error
     */

    public static boolean addFatalTrue(boolean flag, List<ErrorCode> ecList, int code, String msg, String... args) {
        return addMessageTrue(flag, ErrorSeverity.fatal, ecList, code, msg, args);
    }

    public static boolean addErrorTrue(boolean flag, List<ErrorCode> ecList, int code, String msg, String... args) {
        return addMessageTrue(flag, ErrorSeverity.error, ecList, code, msg, args);
    }

    public static boolean addWarnTrue(boolean flag, List<ErrorCode> ecList, int code, String msg, String... args) {
        return addMessageTrue(flag, ErrorSeverity.warn, ecList, code, msg, args);
    }

    public static boolean addInfoTrue(boolean flag, List<ErrorCode> ecList, int code, String msg, String... args) {
        return addMessageTrue(flag, ErrorSeverity.info, ecList, code, msg, args);
    }

    public static boolean addDebugTrue(boolean flag, List<ErrorCode> ecList, int code, String msg, String... args) {
        return addMessageTrue(flag, ErrorSeverity.debug, ecList, code, msg, args);
    }

    private static boolean addMessageTrue(final boolean flag, ErrorSeverity severity, final List<ErrorCode> ecList, final int code, final String msg, final String[] args) {
        if (flag) {
            ErrorCode ec = new ErrorCode(code, msg, args);
            ecList.add(ec);
            ec.setSeverity(severity);
            return true;
        }
        return false;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    void setSeverity(ErrorSeverity severity) {
        this.severity = severity;
    }

    public String toString() {
        return Fmt.Sargs(errorMessage, args);
    }

    public Object getArguments() {
        return args;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return code;
    }
}
