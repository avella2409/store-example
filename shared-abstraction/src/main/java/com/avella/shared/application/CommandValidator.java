package com.avella.shared.application;

public interface CommandValidator<C extends Command> {
    void validate(C command);
}
