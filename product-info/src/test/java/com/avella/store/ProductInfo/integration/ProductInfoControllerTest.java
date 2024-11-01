package com.avella.store.ProductInfo.integration;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandDispatcher;
import com.avella.shared.application.QueryDispatcher;
import com.avella.shared.domain.DomainException;
import com.avella.store.ProductInfo.application.command.UpdateProductInfoCommand;
import com.avella.store.ProductInfo.application.query.GetProductInfoQuery;
import com.avella.store.ProductInfo.application.query.dto.ProductInfoDto;
import com.avella.store.ProductInfo.client.controller.ProductInfoController;
import com.avella.store.ProductInfo.configuration.SecurityConfiguration;
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

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Good documentation: https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/request-post-processors.html
@WebMvcTest(ProductInfoController.class)
@Import(SecurityConfiguration.class)
@Tag("integration")
public class ProductInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommandDispatcher commandDispatcher;

    @MockBean
    private QueryDispatcher queryDispatcher;

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
    void updateInfo() throws Exception {

        mockMvc.perform(post("/product/update/product1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + merchantToken)
                        .content("""
                                {
                                    "name": "name",
                                    "description": "description"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(commandDispatcher).dispatch(new UpdateProductInfoCommand(merchantId,
                "product1", "name", "description"));
    }

    @Test
    void productInfo() throws Exception {

        when(queryDispatcher.dispatch(new GetProductInfoQuery(merchantId, "product1")))
                .thenReturn(new ProductInfoDto("name", "description"));

        mockMvc.perform(get("/product/info/product1")
                        .header("Authorization", "Bearer " + merchantToken)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "name": "name",
                            "description": "description"
                        }
                        """));
    }

    @Test
    void applicationExceptionReturnStatus400() throws Exception {
        when(queryDispatcher.dispatch(new GetProductInfoQuery(merchantId, "product1")))
                .thenThrow(new ApplicationException("Expected error"));

        mockMvc.perform(get("/product/info/product1")
                        .header("Authorization", "Bearer " + merchantToken)
                )
                .andExpect(jsonPath("$.detail", equalTo("Expected error")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void domainExceptionReturnStatus422() throws Exception {
        when(queryDispatcher.dispatch(new GetProductInfoQuery(merchantId, "product1")))
                .thenThrow(new DomainException("Expected error"));

        mockMvc.perform(get("/product/info/product1")
                        .header("Authorization", "Bearer " + merchantToken)
                )
                .andExpect(jsonPath("$.detail", equalTo("Expected error")))
                .andExpect(status().isUnprocessableEntity());
    }
}