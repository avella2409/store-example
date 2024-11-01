package com.avella.store.ProductInfo.integration;


import com.avella.shared.application.QueryDispatcher;
import com.avella.store.ProductInfo.application.query.CanPublishQuery;
import com.avella.store.ProductInfo.client.controller.WebhookController;
import com.avella.store.ProductInfo.configuration.SecurityConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Good documentation: https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/request-post-processors.html
@WebMvcTest(value = WebhookController.class)
@Import(SecurityConfiguration.class)
@Tag("integration")
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryDispatcher queryDispatcher;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void canPublish() throws Exception {
        when(queryDispatcher.dispatch(new CanPublishQuery("merchant1", "product1")))
                .thenReturn(true);

        mockMvc.perform(post("/webhook/canPublish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "merchantId": "merchant1",
                                    "productId": "product1",
                                    "publishingId": "somePublishingIdNotUsedYet"
                                }
                                """)
                )
                .andExpect(status().isOk());
    }

    @Test
    void cannotPublish() throws Exception {
        when(queryDispatcher.dispatch(new CanPublishQuery("merchant1", "product1")))
                .thenReturn(false);

        mockMvc.perform(post("/webhook/canPublish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "merchantId": "merchant1",
                                    "productId": "product1",
                                    "publishingId": "somePublishingIdNotUsedYet"
                                }
                                """)
                )
                .andExpect(status().isUnprocessableEntity());
    }
}