package com.avella.store.merchant.configuration.decorator;

import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

public class TracingCommandHandlerDecorator<C extends Command> implements CommandHandler<C> {

    private final CommandHandler<C> handler;
    private final String name;
    private final ObservationRegistry registry;
    private final String appName;

    public TracingCommandHandlerDecorator(CommandHandler<C> handler, String name, ObservationRegistry registry,
                                          String appName) {
        this.handler = handler;
        this.name = name;
        this.registry = registry;
        this.appName = appName;
    }

    @Override
    public void handle(C command) {
        var obs = Observation.createNotStarted(name, registry)
                .lowCardinalityKeyValue("app", appName);
        obs.observe(() -> handler.handle(command));
    }
}
