package com.avella.store.service.impl;

import com.avella.store.service.ProductInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ProductInfoServiceImpl implements ProductInfoService {

    private final HttpClient client = HttpClient.newHttpClient();

    private final String productInfoUrl;
    private final ObjectMapper objectMapper;

    public ProductInfoServiceImpl(String productInfoUrl, ObjectMapper objectMapper) {
        this.productInfoUrl = productInfoUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void update(String accessToken, String productId, String name, String description) {
        try {
            var request = HttpRequest.newBuilder(new URI(productInfoUrl + "/product/update/" +
                            URLEncoder.encode(productId, StandardCharsets.UTF_8)))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {"name": %s, "description": %s}""".formatted(
                            objectMapper.writeValueAsString(name),
                            objectMapper.writeValueAsString(description)
                    )));

            var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300)
                throw new RuntimeException("Error creating product: " + response.statusCode() + " " + response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
