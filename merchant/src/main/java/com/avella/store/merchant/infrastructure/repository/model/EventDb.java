package com.avella.store.merchant.infrastructure.repository.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "event_to_dispatch")
public class EventDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String type;

    String content;

    public EventDb() {
    }

    public EventDb(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDb eventDb = (EventDb) o;
        return Objects.equals(id, eventDb.id) && Objects.equals(type, eventDb.type) && Objects.equals(content, eventDb.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, content);
    }

    @Override
    public String toString() {
        return "EventDb{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
