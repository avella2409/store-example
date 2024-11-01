package com.avella.keycloak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CloudPubSubListener implements EventListenerProvider {

    private final Publisher publisher;
    private final ObjectMapper objectMapper;

    public CloudPubSubListener(ObjectMapper objectMapper, String projectId, String topicId) {
        this.objectMapper = objectMapper;
        TopicName topicName = TopicName.of(projectId, topicId);

        try {
            this.publisher = Publisher.newBuilder(topicName).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEvent(Event event) {
        publisher.publish(PubsubMessage.newBuilder()
                .putAttributes("type", String.valueOf(event.getType()))
                .setData(ByteString.copyFromUtf8(json(event)))
                .build());
    }


    private String json(Event event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Not interested
    }

    @Override
    public void close() {
        try {
            publisher.shutdown();
            publisher.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
