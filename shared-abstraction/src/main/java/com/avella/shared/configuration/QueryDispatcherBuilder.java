package com.avella.shared.configuration;

import com.avella.shared.application.Query;
import com.avella.shared.application.QueryDispatcher;
import com.avella.shared.application.QueryHandler;
import com.avella.shared.application.QueryValidator;

import java.util.HashMap;
import java.util.Map;

public class QueryDispatcherBuilder {

    private final Map<Class<? extends Query<?>>, QueryHandler<Query<?>, ?>> handlerByClass = new HashMap<>();

    public static QueryDispatcherBuilder newDispatcher() {
        return new QueryDispatcherBuilder();
    }

    public <Q extends Query<R>, R> QueryDispatcherBuilder register(Class<Q> queryClass, QueryHandler<Q, R> handler) {
        return register(queryClass, handler, query -> {
        });
    }

    public <Q extends Query<R>, R> QueryDispatcherBuilder register(Class<Q> queryClass, QueryHandler<Q, R> handler,
                                                                   QueryValidator<Q, R> validator) {
        QueryHandler<Q, R> handlerWithValidation = query -> {
            validator.validate(query);
            return handler.handle(query);
        };

        handlerByClass.put(queryClass, (QueryHandler<Query<?>, ?>) handlerWithValidation);
        return this;
    }

    public QueryDispatcher build() {
        return new QueryDispatcher() {
            @Override
            public <Q extends Query<R>, R> R dispatch(Q query) {
                if (handlerByClass.containsKey(query.getClass()))
                    return ((QueryHandler<Q, R>) handlerByClass.get(query.getClass())).handle(query);
                else throw new RuntimeException("No handler provided for: " + query.getClass().getName());
            }
        };
    }
}
