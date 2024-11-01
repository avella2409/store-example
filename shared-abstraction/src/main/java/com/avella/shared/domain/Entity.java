package com.avella.shared.domain;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entity {
    private final String id;
    private final LocalDateTime lastUpdateTime;
    private final LocalDateTime creationTime;
    private final long version;
    private final List<DomainEvent> eventsToDispatch;

    protected Entity(String id) {
        var now = LocalDateTime.now(ZoneOffset.UTC);
        this.id = id;
        this.lastUpdateTime = now;
        this.creationTime = now;
        this.version = 0;
        this.eventsToDispatch = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    protected void raiseDomainEvent(DomainEvent event) {
        this.eventsToDispatch.add(event);
    }

    protected Entity(Snapshot snapshot) {
        this.id = snapshot.id;
        this.lastUpdateTime = snapshot.lastUpdateTime;
        this.creationTime = snapshot.creationTime;
        this.version = snapshot.version;
        this.eventsToDispatch = new ArrayList<>();
    }

    protected Snapshot entitySnapshot() {
        return new Snapshot(id, lastUpdateTime, creationTime, version, eventsToDispatch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public record Snapshot(String id,
                           LocalDateTime lastUpdateTime,
                           LocalDateTime creationTime,
                           long version,
                           List<DomainEvent> eventsToDispatch) {
    }
}
