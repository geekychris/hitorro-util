package ht.util.core.iterator.json;

/**
 *
 */
public class JSONParseException extends RuntimeException {
    public JSONParseException(String message) {
        super(message);
    }

    public JSONParseException(Exception message) {
        super(message);
    }
}
