package com.avella.shared.application;

public interface CommandHandler<C extends Command> {
    void handle(C command);
}
