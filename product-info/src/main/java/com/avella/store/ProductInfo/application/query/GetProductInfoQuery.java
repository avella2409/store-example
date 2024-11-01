package com.avella.store.ProductInfo.application.query;

import com.avella.shared.application.Query;
import com.avella.store.ProductInfo.application.query.dto.ProductInfoDto;

public record GetProductInfoQuery(String merchantId, String productId) implements Query<ProductInfoDto> {
}
