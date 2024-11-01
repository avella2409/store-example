package com.avella.store.ProductInfo.infrastructure.query;

import com.avella.shared.application.QueryHandler;
import com.avella.store.ProductInfo.application.query.CanPublishQuery;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductIdDb;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductInfoDb;
import org.springframework.data.mongodb.core.MongoOperations;

public class CanPublishQueryHandler implements QueryHandler<CanPublishQuery, Boolean> {

    private final MongoOperations ops;

    public CanPublishQueryHandler(MongoOperations ops) {
        this.ops = ops;
    }

    @Override
    public Boolean handle(CanPublishQuery canPublishQuery) {

        var product = ops.findById(new ProductIdDb(canPublishQuery.merchantId(), canPublishQuery.productId()), ProductInfoDb.class);

        return product != null && canPublish(product);
    }

    private boolean canPublish(ProductInfoDb product) {
        return !product.getName().isBlank() && !product.getDescription().isBlank();
    }
}
