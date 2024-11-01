package com.avella.shared.configuration;

import com.avella.shared.application.Command;
import com.avella.shared.application.CommandDispatcher;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.application.CommandValidator;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcherBuilder {

    private final Map<Class<? extends Command>, CommandHandler<Command>> handlerByClass = new HashMap<>();

    public static CommandDispatcherBuilder newDispatcher() {
        return new CommandDispatcherBuilder();
    }

    public <C extends Command> CommandDispatcherBuilder register(Class<C> commandClass,
                                                                 CommandHandler<C> handler) {
        return register(commandClass, handler, command -> {
        });
    }

    public <C extends Command> CommandDispatcherBuilder register(Class<C> commandClass,
                                                                 CommandHandler<C> handler,
                                                                 CommandValidator<C> validator) {
        CommandHandler<C> handlerWithValidation = command -> {
            validator.validate(command);
            handler.handle(command);
        };
        handlerByClass.put(commandClass, (CommandHandler<Command>) handlerWithValidation);
        return this;
    }

    public CommandDispatcher build() {
        return command -> {
            if (handlerByClass.containsKey(command.getClass())) handlerByClass.get(command.getClass()).handle(command);
            else throw new RuntimeException("No handler provided for: " + command.getClass().getName());
        };
    }
}
