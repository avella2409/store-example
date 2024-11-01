package com.avella.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class CloudPubSubListenerFactory implements EventListenerProviderFactory {

    private final static String id = "PubSubDispatcher";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new CloudPubSubListener(
                new ObjectMapper(),
                System.getenv("GCLOUD_PROJECT_ID"),
                System.getenv("GCLOUD_TOPIC_ID")
        );
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return id;
    }
}
