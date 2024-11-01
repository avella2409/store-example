package com.avella.store.merchant.unit.impl;

import com.avella.store.merchant.domain.Event;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventToDispatchRepository {

    private final List<Event> events = new ArrayList<>();

    public void save(Event event) {
        events.add(event);
    }

    public List<Event> dispatched() {
        return events;
    }
}
