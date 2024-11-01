package com.avella.store.service.impl;

import com.avella.store.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class KeycloakAuthService implements AuthService {

    private final Map<String, UserData> users = new ConcurrentHashMap<>();
    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    private final HttpClient client = HttpClient.newHttpClient();

    private final String gcloudProjectId;
    private final String keycloakUrl;
    private final Keycloak adminKeycloak;
    private final String clientId;
    private final String realm;
    private final ObjectMapper objectMapper;

    private final Publisher publisher;

    public KeycloakAuthService(String gcloudProjectId,
                               String keycloakUrl, String clientId, String realm,
                               ObjectMapper objectMapper) {
        this.gcloudProjectId = gcloudProjectId;
        this.keycloakUrl = keycloakUrl;
        this.adminKeycloak = Keycloak.getInstance(
                keycloakUrl,
                "master",
                "admin",
                "admin",
                "admin-cli" // Important
        );
        this.clientId = clientId;
        this.realm = realm;
        this.objectMapper = objectMapper;
        try {
            this.publisher = Publisher.newBuilder(TopicName.of(gcloudProjectId, "keycloak")).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String register() {
        String username = nextUsername();
        String password = nextPassword();
        String email = nextEmail(username);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(username);
        userRepresentation.setCredentials(List.of(credential));
        userRepresentation.setEmail(email);
        userRepresentation.setFirstName("Test first name"); // Required to get token
        userRepresentation.setLastName("Test last name"); // Required to get token
        userRepresentation.setEnabled(true);

        var response = adminKeycloak.realm(realm).users().create(userRepresentation);
        verifySuccess(response, "User creation");

        String path = response.getLocation().getPath();
        String userId = path.substring(path.lastIndexOf("/") + 1);

        dispatchKeycloakRegisterEvent(userId);

        users.put(userId, new UserData(userId, username, password, email));

        sleep(2000);

        return userId;
    }

    private void dispatchKeycloakRegisterEvent(String userId) {
        try {
            var future = publisher.publish(PubsubMessage.newBuilder()
                    .putAttributes("type", "REGISTER")
                    .setData(ByteString.copyFromUtf8("""
                            {"userId": "%s"}""".formatted(userId)))
                    .build());
            future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void login(String userId) {
        System.out.println("Login user");
        if (!users.containsKey(userId)) throw new RuntimeException("User not registered");
        else try {
            var data = users.get(userId);
            var params = Map.of(
                    "grant_type", URLEncoder.encode("password", StandardCharsets.UTF_8),
                    "username", URLEncoder.encode(data.username, StandardCharsets.UTF_8),
                    "password", URLEncoder.encode(data.password, StandardCharsets.UTF_8),
                    "client_id", URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                    "client_secret", URLEncoder.encode("", StandardCharsets.UTF_8)
            );

            var request = HttpRequest.newBuilder()
                    .uri(new URI(String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, realm)))
                    .POST(HttpRequest.BodyPublishers.ofString(params.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .collect(Collectors.joining("&"))))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RuntimeException("Error logging in, expected status 200: " + response.statusCode());

            String accessToken = objectMapper.readTree(response.body()).get("access_token").asText();

            tokens.put(userId, accessToken);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<String> accessToken(String userId) {
        return tokens.containsKey(userId) ? Optional.of(tokens.get(userId)) : Optional.empty();
    }

    @Override
    public void deleteUser(String userId) {
        if (users.containsKey(userId)) {
            var response = adminKeycloak.realm(realm).users().delete(userId);

            verifySuccess(response, "User deletion");
        }
    }

    private String nextUsername() {
        return UUID.randomUUID().toString();
    }

    private String nextEmail(String username) {
        return username + "@gmail.com";
    }

    private String nextPassword() {
        return "SomeSecurePassword;;!";
    }

    private void verifySuccess(Response response, String errorMessage) {
        if (response.getStatus() >= 300) throw new RuntimeException("Operation error: " + errorMessage);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    record UserData(String userId, String username, String password, String email) {
    }
}
