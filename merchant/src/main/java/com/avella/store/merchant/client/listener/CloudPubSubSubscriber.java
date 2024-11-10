package com.avella.store.merchant.client.listener;

import com.avella.shared.application.CommandDispatcher;
import com.avella.store.merchant.core.command.RegisterMerchantCommand;
import com.avella.store.merchant.client.listener.event.RegisterEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CloudPubSubSubscriber {

    private static final Logger log = LoggerFactory.getLogger(CloudPubSubSubscriber.class);
    private static final int PARALLEL_MESSAGE_PROCESSING_COUNT = 5;

    private final String projectId;
    private final String subscriptionId;
    private final CommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;

    public CloudPubSubSubscriber(@Value("${gcloud.project.id}") String projectId,
                                 @Value("${gcloud.pubsub.subscription.id.keycloak}") String subscriptionId,
                                 CommandDispatcher commandDispatcher,
                                 ObjectMapper objectMapper) {
        this.projectId = projectId;
        this.subscriptionId = subscriptionId;
        this.commandDispatcher = commandDispatcher;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        Subscriber subscriber = Subscriber.newBuilder(ProjectSubscriptionName.of(projectId, subscriptionId), receiver())
                .setExecutorProvider(InstantiatingExecutorProvider.newBuilder()
                        .setExecutorThreadCount(PARALLEL_MESSAGE_PROCESSING_COUNT)
                        .build())
                .build();

        log.info("Start keycloak subscriber...");

        subscriber.startAsync().awaitRunning();

        log.info("Keycloak subscriber running");
    }

    private MessageReceiver receiver() {
        return (message, consumer) -> {
            try {
                var data = message.getData().toStringUtf8();
                log.info("Keycloak message: {} => {}", message.getMessageId(), data);

                switch (message.getAttributesMap().get("type")) {
                    case "REGISTER" -> {
                        var registerEvent = objectMapper.readValue(data, RegisterEvent.class);
                        commandDispatcher.dispatch(new RegisterMerchantCommand(registerEvent.userId()));
                    }
                    default -> {
                    }
                }
                consumer.ack();
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage());
                consumer.nack();
                throw new RuntimeException(e);
            }
        };
    }
}
