package com.avella.shared.application;

public interface QueryValidator<Q extends Query<R>, R> {
    void validate(Q query);
}
