package ht.util.json.keys;

import ht.util.core.string.Fmt;

/**
 * Wrapper of exceptions related to property key validation
 *
 * @author chris
 */
public class PropertyKeyValidationException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 445566L;

    public PropertyKeyValidationException(String message, String keyname, String sValue) {
        super(Fmt.S("%s property: %s with value: %s", message, keyname, sValue));
    }

}
