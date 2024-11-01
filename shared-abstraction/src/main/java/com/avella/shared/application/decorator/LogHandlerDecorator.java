package com.avella.shared.application.decorator;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;

import java.util.logging.Logger;

public class LogHandlerDecorator<C extends Command> implements CommandHandler<C> {

    private static final Logger log = Logger.getLogger(LogHandlerDecorator.class.getName());

    private final CommandHandler<C> handler;

    public LogHandlerDecorator(CommandHandler<C> handler) {
        this.handler = handler;
    }

    @Override
    public void handle(C command) {
        try {
            log.info(String.format("Handle command %s", command));
            handler.handle(command);
            log.info(String.format("Successfully handled command %s", command));
        } catch (ApplicationException | DomainException e) {
            throw e;
        } catch (Exception e) {
            log.severe(String.format("Error handling command %s %s", command, e.getMessage()));
            throw e;
        }
    }
}