package com.avella.store.merchant.integration;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandDispatcher;
import com.avella.shared.application.QueryDispatcher;
import com.avella.shared.domain.DomainException;
import com.avella.store.merchant.application.command.ArchiveProductCommand;
import com.avella.store.merchant.application.command.CreateProductCommand;
import com.avella.store.merchant.application.command.PublishProductCommand;
import com.avella.store.merchant.application.query.GetAllProductQuery;
import com.avella.store.merchant.application.query.dto.ProductStatusDto;
import com.avella.store.merchant.client.controller.ProductController;
import com.avella.store.merchant.configuration.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Good documentation: https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/request-post-processors.html
@WebMvcTest(value = ProductController.class)
@Import(SecurityConfiguration.class)
@Tag("integration")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommandDispatcher commandDispatcher;

    @MockBean
    private QueryDispatcher queryDispatcher;

    @MockBean
    private Supplier<UUID> uuidGenerator;

    @MockBean
    private JwtDecoder jwtDecoder;

    private final String merchantId = "merchant1";
    private final String merchantToken = "merchant_token";

    @BeforeEach
    void setup() {
        when(jwtDecoder.decode(merchantToken))
                .thenReturn(Jwt.withTokenValue(merchantToken)
                        .header("alg", "none")
                        .claim("sub", merchantId)
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());
    }

    @Test
    void findAll() throws Exception {
        when(queryDispatcher.dispatch(new GetAllProductQuery(merchantId)))
                .thenReturn(List.of(
                        new ProductStatusDto("product1", "DRAFT"),
                        new ProductStatusDto("product2", "ARCHIVED")
                ));

        mockMvc.perform(get("/product/findAll")
                        .header("Authorization", "Bearer " + merchantToken))
                .andExpect(content().json("""
                        [
                            {
                                "id": "product1",
                                "status": "DRAFT"
                            },
                            {
                                "id": "product2",
                                "status": "ARCHIVED"
                            }
                        ]
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void create() throws Exception {

        var productId = UUID.randomUUID();

        when(uuidGenerator.get())
                .thenReturn(productId);

        mockMvc.perform(post("/product/create")
                        .header("Authorization", "Bearer " + merchantToken))
                .andExpect(content().string(productId.toString()))
                .andExpect(status().isOk());

        verify(commandDispatcher).dispatch(new CreateProductCommand(merchantId, productId.toString()));
    }

    @Test
    void publish() throws Exception {

        var publishingId = UUID.randomUUID();

        when(uuidGenerator.get())
                .thenReturn(publishingId);

        mockMvc.perform(post("/product/publish")
                        .header("Authorization", "Bearer " + merchantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": "product1"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(commandDispatcher).dispatch(new PublishProductCommand(merchantId, "product1", publishingId.toString()));
    }

    @Test
    void archive() throws Exception {

        mockMvc.perform(post("/product/archive")
                        .header("Authorization", "Bearer " + merchantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": "product1"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(commandDispatcher).dispatch(new ArchiveProductCommand(merchantId, "product1"));
    }

    @Test
    void applicationExceptionReturnStatus400() throws Exception {
        when(queryDispatcher.dispatch(new GetAllProductQuery(merchantId)))
                .thenThrow(new ApplicationException("Expected error"));

        mockMvc.perform(get("/product/findAll")
                        .header("Authorization", "Bearer " + merchantToken))
                .andExpect(jsonPath("$.detail", equalTo("Expected error")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void domainExceptionReturnStatus422() throws Exception {
        when(queryDispatcher.dispatch(new GetAllProductQuery(merchantId)))
                .thenThrow(new DomainException("Expected error"));

        mockMvc.perform(get("/product/findAll")
                        .header("Authorization", "Bearer " + merchantToken))
                .andExpect(jsonPath("$.detail", equalTo("Expected error")))
                .andExpect(status().isUnprocessableEntity());
    }
}
