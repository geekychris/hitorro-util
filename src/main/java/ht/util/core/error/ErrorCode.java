package ht.util.core.error;

import ht.util.core.ArrayUtil;
import ht.util.core.string.Fmt;

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
