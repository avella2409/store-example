package com.avella.store.ProductInfo.infrastructure.repository;

import com.avella.store.ProductInfo.domain.Product;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.ProductRepository;
import com.avella.store.ProductInfo.domain.shared.Entity;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductIdDb;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductInfoDb;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class MongoProductRepository implements ProductRepository {

    private final MongoOperations ops;

    public MongoProductRepository(MongoOperations ops) {
        this.ops = ops;
    }

    @Override
    public void save(Product product) {
        var snapshot = product.snapshot();
        var entitySnapshot = snapshot.entitySnapshot();

        ops.save(new ProductInfoDb(
                new ProductIdDb(entitySnapshot.id().merchantId(), entitySnapshot.id().productId()),
                LocalDateTime.now(ZoneOffset.UTC),
                entitySnapshot.creationTime(),
                entitySnapshot.version(),
                snapshot.name(), snapshot.description())
        );
    }

    @Override
    public Optional<Product> product(ProductId productId) {
        return Optional.ofNullable(
                        ops.findById(new ProductIdDb(productId.merchantId(), productId.productId()), ProductInfoDb.class)
                )
                .map(p -> Product.restore(new Product.Snapshot(
                        new Entity.Snapshot<>(
                                new ProductId(p.getId().getMerchantId(), p.getId().getProductId()),
                                p.getLastUpdateTime(),
                                p.getCreationTime(),
                                p.getVersion()
                        ),
                        p.getName(),
                        p.getDescription()
                )));
    }
}
