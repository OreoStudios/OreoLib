package com.oreo.lib;

public class OreoException extends RuntimeException {
    public OreoException(String message) {
        super(message);
    }

    public OreoException(String message, Throwable cause) {
        super(message, cause);
    }
}
