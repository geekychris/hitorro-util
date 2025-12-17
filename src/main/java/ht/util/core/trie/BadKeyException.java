package ht.util.core.trie;

/**
 * User: chris
 */
public class BadKeyException extends Exception {
    public BadKeyException(char c) {
        super("Error " + c);
    }

}