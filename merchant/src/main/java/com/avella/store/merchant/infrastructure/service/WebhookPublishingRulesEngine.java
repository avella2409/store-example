package com.avella.store.merchant.infrastructure.service;

import com.avella.store.merchant.domain.PublishingRulesEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class WebhookPublishingRulesEngine implements PublishingRulesEngine {

    private final Set<String> urls;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    public WebhookPublishingRulesEngine(Set<String> urls, RestTemplate restTemplate, ExecutorService executorService) {
        this.urls = urls;
        this.restTemplate = restTemplate;
        this.executorService = executorService;
    }

    @Override
    public boolean canPublish(String merchantId, String productId, String publishingId) {

        var responses = urls.stream()
                .map(url -> executorService.submit(() -> restTemplate.postForEntity(
                        url,
                        new PublishRequest(merchantId, productId, publishingId),
                        Void.class
                )))
                .toList();

        return responses.stream().allMatch(this::canPublish);
    }

    // do not timeout on future here. CommandHandler decorators should do it
    private boolean canPublish(Future<ResponseEntity<Void>> responseFuture) {
        try {
            var statusCode = responseFuture.get().getStatusCode();
            if (statusCode.is5xxServerError()) throw new RuntimeException("Cannot publish because of a server error");
            else return statusCode.is2xxSuccessful();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    record PublishRequest(String merchantId, String productId, String publishingId) {
    }
}
