package com.avella.store.ProductInfo.infrastructure.repository.model;

import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "productinfo") // no "-" in table name
public class ProductInfoDb {

    private ProductIdDb id;

    private LocalDateTime lastUpdateTime;
    private LocalDateTime creationTime;

    @Version
    private long version;

    private String name;
    private String description;

    public ProductInfoDb() {
    }

    public ProductInfoDb(ProductIdDb id, LocalDateTime lastUpdateTime, LocalDateTime creationTime, long version, String name, String description) {
        this.id = id;
        this.lastUpdateTime = lastUpdateTime;
        this.creationTime = creationTime;
        this.version = version;
        this.name = name;
        this.description = description;
    }

    public ProductIdDb getId() {
        return id;
    }

    public void setId(ProductIdDb id) {
        this.id = id;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ProductInfoDb{" +
                "id=" + id +
                ", lastUpdateTime=" + lastUpdateTime +
                ", creationTime=" + creationTime +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
