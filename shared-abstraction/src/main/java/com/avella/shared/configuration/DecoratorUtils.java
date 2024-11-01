package com.avella.shared.configuration;

import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DecoratorUtils {

    public static <C extends Command> CommandHandler<C> withDecoratorsExecutedInOrder(CommandHandler<C> handler,
                                                                                      List<Function<CommandHandler<C>, CommandHandler<C>>> decorators) {
        var reversed = new ArrayList<>(decorators);
        Collections.reverse(reversed);

        for (var decorator : reversed) handler = decorator.apply(handler);

        return handler;
    }
}
