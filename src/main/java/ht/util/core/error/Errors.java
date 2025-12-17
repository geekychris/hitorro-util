package ht.util.core.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 7/21/17.
 */
public class Errors {
    private List<ErrorCode> errors = new ArrayList();

    public void add(ErrorCode ec) {
        errors.add(ec);
    }

    public boolean hasErrors() {

        for (ErrorCode ec : errors) {
            if (ec.getSeverity().getLevel() >= ErrorSeverity.error.getLevel()) {
                return true;
            }
        }
        return false;
    }
}
