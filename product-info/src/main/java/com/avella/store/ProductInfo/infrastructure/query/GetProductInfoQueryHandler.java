package com.avella.store.ProductInfo.infrastructure.query;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.QueryHandler;
import com.avella.store.ProductInfo.application.query.GetProductInfoQuery;
import com.avella.store.ProductInfo.application.query.dto.ProductInfoDto;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductIdDb;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductInfoDb;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.Optional;

public class GetProductInfoQueryHandler implements QueryHandler<GetProductInfoQuery, ProductInfoDto> {

    private final MongoOperations ops;

    public GetProductInfoQueryHandler(MongoOperations ops) {
        this.ops = ops;
    }

    @Override
    public ProductInfoDto handle(GetProductInfoQuery query) {

        return Optional.ofNullable(
                        ops.findById(new ProductIdDb(query.merchantId(), query.productId()), ProductInfoDb.class)
                )
                .map(p -> new ProductInfoDto(p.getName(), p.getDescription()))
                .orElseThrow(() -> new ApplicationException("Product not found"));
    }
}
