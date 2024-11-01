package com.avella.shared.application.decorator;

import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class TimeoutHandlerDecorator<C extends Command> implements CommandHandler<C> {

    private static final Logger log = Logger.getLogger(TimeoutHandlerDecorator.class.getName());

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final CommandHandler<C> handler;
    private final long timeoutMs;

    public TimeoutHandlerDecorator(CommandHandler<C> handler, long timeoutMs) {
        this.handler = handler;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void handle(C command) {
        try {
            var future = executor.submit(() -> handler.handle(command));

            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.severe("Handler interrupted: " + e.getClass().getName());
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new RuntimeException(cause);
        }
    }
}