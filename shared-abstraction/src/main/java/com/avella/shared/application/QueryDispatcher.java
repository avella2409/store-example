package com.avella.shared.application;

public interface QueryDispatcher {
    <Q extends Query<R>, R> R dispatch(Q query);
}
