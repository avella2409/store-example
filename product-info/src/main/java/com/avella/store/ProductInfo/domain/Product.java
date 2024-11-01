package com.avella.store.ProductInfo.domain;

import com.avella.shared.domain.DomainException;
import com.avella.store.ProductInfo.domain.shared.Entity;

public class Product extends Entity<ProductId> {

    public static final int LIMIT_LENGTH_NAME = 200;
    public static final int LIMIT_LENGTH_DESCRIPTION = 1500;

    private String name;
    private String description;

    protected Product(ProductId productId) {
        super(productId);
        this.name = "";
        this.description = "";
    }

    public static Product create(ProductId productId) {
        return new Product(productId);
    }

    public void updateName(SafeText name) {
        if (name.text().length() <= LIMIT_LENGTH_NAME) this.name = name.text();
        else throw new DomainException("Name length limit exceeded");
    }

    public void updateDescription(SafeText description) {
        if (description.text().length() <= LIMIT_LENGTH_DESCRIPTION) this.description = description.text();
        else throw new DomainException("Description length limit exceeded");
    }

    private Product(Snapshot snapshot) {
        super(snapshot.entitySnapshot);
        this.name = snapshot.name;
        this.description = snapshot.description;
    }

    public static Product restore(Snapshot snapshot) {
        return new Product(snapshot);
    }

    public Snapshot snapshot() {
        return new Snapshot(entitySnapshot(), name, description);
    }

    public record Snapshot(Entity.Snapshot<ProductId> entitySnapshot, String name, String description) {
    }
}
