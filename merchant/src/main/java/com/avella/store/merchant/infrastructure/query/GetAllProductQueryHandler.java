package com.avella.store.merchant.infrastructure.query;

import com.avella.shared.application.QueryHandler;
import com.avella.store.merchant.application.query.GetAllProductQuery;
import com.avella.store.merchant.application.query.dto.ProductStatusDto;
import com.avella.store.merchant.infrastructure.repository.JpaMerchantRepository;
import com.avella.store.merchant.infrastructure.repository.model.ProductsJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class GetAllProductQueryHandler implements QueryHandler<GetAllProductQuery, List<ProductStatusDto>> {

    private final JpaMerchantRepository jpaMerchantRepository;
    private final ObjectMapper objectMapper;

    public GetAllProductQueryHandler(JpaMerchantRepository jpaMerchantRepository, ObjectMapper objectMapper) {
        this.jpaMerchantRepository = jpaMerchantRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ProductStatusDto> handle(GetAllProductQuery getAllProductQuery) {
        return fromJson(jpaMerchantRepository.findById(getAllProductQuery.merchantId())
                .orElseThrow(() -> new RuntimeException("Invalid merchant id"))
                .getProducts())
                .products().stream()
                .map(p -> new ProductStatusDto(p.productId(), p.status()))
                .toList();
    }

    ProductsJson fromJson(String products) {
        try {
            return objectMapper.readValue(products, ProductsJson.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
