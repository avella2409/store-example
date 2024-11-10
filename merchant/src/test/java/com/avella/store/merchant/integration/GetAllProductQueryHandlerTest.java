package com.avella.store.merchant.integration;

import com.avella.store.merchant.core.query.GetAllProductQuery;
import com.avella.store.merchant.core.query.dto.ProductStatusDto;
import com.avella.store.merchant.infrastructure.query.GetAllProductQueryHandler;
import com.avella.store.merchant.infrastructure.repository.JpaMerchantRepository;
import com.avella.store.merchant.infrastructure.repository.model.MerchantDb;
import com.avella.store.merchant.infrastructure.repository.model.ProductJson;
import com.avella.store.merchant.infrastructure.repository.model.ProductsJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Tag("integration")
public class GetAllProductQueryHandlerTest {

    @Autowired
    private JpaMerchantRepository jpaMerchantRepository;

    private GetAllProductQueryHandler handler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        handler = new GetAllProductQueryHandler(jpaMerchantRepository, objectMapper);
    }

    @Test
    void getAllProduct() throws JsonProcessingException {
        jpaMerchantRepository.save(new MerchantDb("merchant1",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        1,
                        objectMapper.writeValueAsString(new ProductsJson(List.of(
                                new ProductJson("product1", 0, "DRAFT", null, null),
                                new ProductJson("product2", 0, "ARCHIVED", null, null),
                                new ProductJson("product3", 0, "PUBLISHED", null, null)
                        )))
                )
        );

        List<ProductStatusDto> statuses = handler.handle(new GetAllProductQuery("merchant1"));

        assertEquals(
                Set.of(
                        new ProductStatusDto("product1", "DRAFT"),
                        new ProductStatusDto("product2", "ARCHIVED"),
                        new ProductStatusDto("product3", "PUBLISHED")
                ),
                new HashSet<>(statuses)
        );
    }
}
