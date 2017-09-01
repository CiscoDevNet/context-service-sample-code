package com.cisco.thunderhead.rest;

/**
 * Represents an exception.
 */
public class ContextException extends RuntimeException {
    public ContextException(String message) {
        super(message);
    }

    public ContextException(String nessage, Throwable e) {
        super(nessage,e);
    }
}
