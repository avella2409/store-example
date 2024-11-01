package com.avella.shared.application.decorator;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;

import java.util.logging.Logger;

public class RetryNonBusinessErrorDecorator<C extends Command> implements CommandHandler<C> {

    private static final Logger log = Logger.getLogger(RetryNonBusinessErrorDecorator.class.getName());

    private final CommandHandler<C> handler;
    private final int numberRetry;
    private final long delayMsBetweenRetry;

    public RetryNonBusinessErrorDecorator(CommandHandler<C> handler, int numberRetry) {
        this(handler, numberRetry, 0);
    }

    public RetryNonBusinessErrorDecorator(CommandHandler<C> handler, int numberRetry, long delayMsBetweenRetry) {
        this.handler = handler;
        this.numberRetry = numberRetry;
        this.delayMsBetweenRetry = delayMsBetweenRetry;
    }

    @Override
    public void handle(C command) {
        handleWithRetry(command, numberRetry);
    }

    private void handleWithRetry(C command, int retryLeft) {
        try {
            handler.handle(command);
        } catch (ApplicationException | DomainException e) {
            throw e;
        } catch (Exception e) {
            if (retryLeft > 0) {
                log.warning(String.format("Retrying failing command: %s %s", command, e.getMessage()));
                if(delayMsBetweenRetry > 0) sleep(delayMsBetweenRetry);
                handleWithRetry(command, retryLeft - 1);
            } else throw e;
        }
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
