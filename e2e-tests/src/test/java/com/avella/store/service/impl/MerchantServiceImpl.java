package com.avella.store.service.impl;

import com.avella.store.service.MerchantService;
import com.avella.store.shared.ProductStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class MerchantServiceImpl implements MerchantService {

    private final HttpClient client = HttpClient.newHttpClient();

    private final String merchantUrl;
    private final ObjectMapper objectMapper;

    public MerchantServiceImpl(String merchantUrl, ObjectMapper objectMapper) {
        this.merchantUrl = merchantUrl;
        this.objectMapper = objectMapper;
    }


    @Override
    public String createProduct(String accessToken) {
        try {
            var request = HttpRequest.newBuilder(new URI(merchantUrl + "/product/create"))
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.noBody());

            var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
                throw new RuntimeException("Error creating product: " + response.statusCode() + " " + response.body());

            sleep(2000);

            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publishProduct(String accessToken, String productId) {
        try {
            var request = HttpRequest.newBuilder(new URI(merchantUrl + "/product/publish"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {"productId": %s}""".formatted(objectMapper.writeValueAsString(productId))));

            var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
                throw new RuntimeException("Error publishing product: " + response.statusCode() + " " + response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProductStatus> findAllProductStatus(String accessToken) {
        try {
            var request = HttpRequest.newBuilder(new URI(merchantUrl + "/product/findAll"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET();

            var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
                throw new RuntimeException("Error publishing product: " + response.statusCode() + " " + response.body());

            return objectMapper.readValue(response.body(), new TypeReference<List<ProductStatus>>() {
            });
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
