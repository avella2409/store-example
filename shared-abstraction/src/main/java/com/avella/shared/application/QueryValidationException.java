package com.avella.shared.application;

public class QueryValidationException extends RuntimeException {
    public QueryValidationException(String message) {
        super(message);
    }
}
