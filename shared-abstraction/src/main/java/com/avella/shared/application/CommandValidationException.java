package com.avella.shared.application;

public class CommandValidationException extends RuntimeException {
    public CommandValidationException(String message) {
        super(message);
    }
}
