package com.avella.store.merchant.infrastructure.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "merchant")
public class MerchantDb {

    @Id
    private String id;

    private LocalDateTime lastUpdateTime;
    private LocalDateTime creationTime;

    @Version
    private long version;

    private String products;

    public MerchantDb() {
    }

    public MerchantDb(String id, LocalDateTime lastUpdateTime, LocalDateTime creationTime, long version, String products) {
        this.id = id;
        this.lastUpdateTime = lastUpdateTime;
        this.creationTime = creationTime;
        this.version = version;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MerchantDb that = (MerchantDb) object;
        return version == that.version && Objects.equals(id, that.id) && Objects.equals(lastUpdateTime, that.lastUpdateTime) && Objects.equals(creationTime, that.creationTime) && Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastUpdateTime, creationTime, version, products);
    }

    @Override
    public String toString() {
        return "MerchantDb{" +
                "id='" + id + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", creationTime=" + creationTime +
                ", version=" + version +
                ", products='" + products + '\'' +
                '}';
    }
}