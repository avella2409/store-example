package com.avella.store.ProductInfo.domain.shared;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Entity<Id> {
    private final Id id;
    private final LocalDateTime lastUpdateTime;
    private final LocalDateTime creationTime;
    private final long version;

    protected Entity(Id id) {
        var now = LocalDateTime.now(ZoneOffset.UTC);
        this.id = id;
        this.lastUpdateTime = now;
        this.creationTime = now;
        this.version = 0;
    }

    public Id id() {
        return id;
    }

    protected Entity(Snapshot<Id> snapshot) {
        this.id = snapshot.id;
        this.lastUpdateTime = snapshot.lastUpdateTime;
        this.creationTime = snapshot.creationTime;
        this.version = snapshot.version;
    }

    protected Snapshot<Id> entitySnapshot() {
        return new Snapshot<>(id, lastUpdateTime, creationTime, version);
    }

    public record Snapshot<Id>(Id id,
                               LocalDateTime lastUpdateTime,
                               LocalDateTime creationTime,
                               long version) {
    }
}
