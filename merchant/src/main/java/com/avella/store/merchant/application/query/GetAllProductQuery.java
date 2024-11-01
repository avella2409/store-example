package com.avella.store.merchant.application.query;

import com.avella.shared.application.Query;
import com.avella.store.merchant.application.query.dto.ProductStatusDto;

import java.util.List;

public record GetAllProductQuery(String merchantId) implements Query<List<ProductStatusDto>> {
}
