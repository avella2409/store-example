package com.avella.store.ProductInfo.integration;

import com.avella.shared.application.ApplicationException;
import com.avella.store.ProductInfo.application.query.GetProductInfoQuery;
import com.avella.store.ProductInfo.application.query.dto.ProductInfoDto;
import com.avella.store.ProductInfo.infrastructure.query.GetProductInfoQueryHandler;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductIdDb;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductInfoDb;
import com.avella.store.ProductInfo.integration.shared.MongoContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
@Import(GetProductInfoQueryHandler.class)
@Tag("integration")
public class GetProductInfoQueryHandlerTest extends MongoContainer {

    @Autowired
    MongoOperations ops;

    @Autowired
    private GetProductInfoQueryHandler handler;

    @BeforeEach
    void setup() {
        ops.findAllAndRemove(new Query(), ProductInfoDb.class);
    }

    @Test
    void getInfo() {
        ops.save(new ProductInfoDb(
                new ProductIdDb("merchant1", "product1"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                0,
                "name",
                "description"
        ));

        var res = handler.handle(new GetProductInfoQuery("merchant1", "product1"));

        assertEquals(new ProductInfoDto("name", "description"), res);
    }

    @Test
    void errorWhenNotFound() {
        var error = assertThrows(ApplicationException.class, () -> handler.handle(new GetProductInfoQuery("merchant1", "product1")));

        assertEquals("Product not found", error.getMessage());
    }
}
