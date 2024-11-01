package com.avella.store.ProductInfo.configuration.decorator;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class MetricCountCommandHandlerDecorator<C extends Command> implements CommandHandler<C> {

    private final CommandHandler<C> handler;
    private final Counter executionCount;
    private final Counter technicalErrorCount;
    private final Counter businessErrorCount;
    private final Counter errorCount;
    private final Counter successCount;

    public MetricCountCommandHandlerDecorator(CommandHandler<C> handler, String prefix, MeterRegistry registry) {
        this.handler = handler;
        this.executionCount = Counter.builder(prefix + "-execution")
                .description("Number of execution")
                .register(registry);
        this.technicalErrorCount = Counter.builder(prefix + "-error-technical")
                .description("Number of technical error")
                .register(registry);
        this.businessErrorCount = Counter.builder(prefix + "-error-business")
                .description("Number of business error")
                .register(registry);
        this.errorCount = Counter.builder(prefix + "-error")
                .description("Number of error")
                .register(registry);
        this.successCount = Counter.builder(prefix + "-success")
                .description("Number of successful execution")
                .register(registry);
    }

    @Override
    public void handle(C command) {
        executionCount.increment();
        try {
            handler.handle(command);
            successCount.increment();
        } catch (ApplicationException | DomainException e) {
            businessErrorCount.increment();
            errorCount.increment();
            throw e;
        } catch (Exception e) {
            technicalErrorCount.increment();
            errorCount.increment();
            throw e;
        }
    }
}
