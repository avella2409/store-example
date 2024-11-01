package com.avella.store.merchant.integration;

import com.avella.store.merchant.infrastructure.service.WebhookPublishingRulesEngine;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Delay;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Testcontainers
@Tag("integration")
public class WebhookPublishingRulesEngineTest {

    @Container
    private static final MockServerContainer mockServerContainer = new MockServerContainer(
            DockerImageName.parse("mockserver/mockserver:5.15.0")
    );

    private static MockServerClient mockServerClient;

    // MockServer bug, can only create one instance of client: https://github.com/mock-server/mockserver/issues/1072
    @BeforeAll
    static void beforeAll() {
        mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort());
    }

    @AfterAll
    static void afterAll() {
        mockServerClient.close();
    }

    @BeforeEach
    void setup() {
        mockServerClient.reset();
    }

    @Test
    void canPublishWhenEveryServiceReturnSuccess() {
        var engine = new WebhookPublishingRulesEngine(Set.of(
                serviceUrl("service1"),
                serviceUrl("service2")
        ), restTemplate(), Executors.newVirtualThreadPerTaskExecutor());

        String requestBody = """
                {"merchantId":"merchant1","productId":"product1","publishingId":"publish1"}""";

        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/service1")
                        .withBody(requestBody))
                .respond(response()
                        .withStatusCode(200)
                );
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/service2")
                        .withBody(requestBody))
                .respond(response()
                        .withStatusCode(200)
                );

        assertTrue(engine.canPublish("merchant1", "product1", "publish1"));


    }

    @Test
    void cannotPublishWhenAtLeastOneServiceDidNotSucceed() {
        var engine = new WebhookPublishingRulesEngine(Set.of(
                serviceUrl("service1"),
                serviceUrl("service2")
        ), restTemplate(), Executors.newVirtualThreadPerTaskExecutor());

        String requestBody = """
                {"merchantId":"merchant1","productId":"product1","publishingId":"publish1"}""";

        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/service1")
                        .withBody(requestBody))
                .respond(response()
                        .withStatusCode(200)
                );
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/service2")
                        .withBody(requestBody))
                .respond(response()
                        .withStatusCode(422)
                );

        assertFalse(engine.canPublish("merchant1", "product1", "publish1"));
    }

    @Test
    void throwExceptionWhen5xxCode() {
        var engine = new WebhookPublishingRulesEngine(Set.of(serviceUrl("service1")),
                restTemplate(), Executors.newVirtualThreadPerTaskExecutor());

        String requestBody = """
                {"merchantId":"merchant1","productId":"product1","publishingId":"publish1"}""";

        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/service1")
                        .withBody(requestBody))
                .respond(response()
                        .withStatusCode(500)
                );

        var error = assertThrows(RuntimeException.class, () -> engine.canPublish("merchant1", "product1", "publish1"));

        assertEquals("Cannot publish because of a server error", error.getMessage());
    }

    @Test
    @Timeout(6)
    void requestAreExecutedInParallel() {
        var engine = new WebhookPublishingRulesEngine(Set.of(
                serviceUrl("service1"),
                serviceUrl("service2"),
                serviceUrl("service3"),
                serviceUrl("service4")
        ), restTemplate(), Executors.newVirtualThreadPerTaskExecutor());

        String requestBody = """
                {"merchantId":"merchant1","productId":"product1","publishingId":"publish1"}""";

        succeedWithDelay("service1", requestBody, 3);
        succeedWithDelay("service2", requestBody, 3);
        succeedWithDelay("service3", requestBody, 3);
        succeedWithDelay("service4", requestBody, 3);

        assertTrue(engine.canPublish("merchant1", "product1", "publish1"));
    }

    private void succeedWithDelay(String path, String body, int seconds) {
        mockServerClient
                .when(request().withMethod("POST").withPath("/" + path).withBody(body))
                .respond(response().withStatusCode(200).withDelay(Delay.seconds(seconds)));
    }

    private RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });
        return template;
    }

    private String serviceUrl(String path) {
        return String.format("http://%s:%s/%s", mockServerContainer.getHost(), mockServerContainer.getServerPort(), path);
    }
}
