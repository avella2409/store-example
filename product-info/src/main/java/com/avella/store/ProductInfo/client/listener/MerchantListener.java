package com.avella.store.ProductInfo.client.listener;

import com.avella.shared.application.CommandDispatcher;
import com.avella.store.ProductInfo.application.command.CreateProductCommand;
import com.avella.store.ProductInfo.client.listener.event.ProductCreated;
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
public class MerchantListener {

    private static final Logger log = LoggerFactory.getLogger(MerchantListener.class);
    private static final int PARALLEL_MESSAGE_PROCESSING_COUNT = 20;

    private final String projectId;
    private final String subscriptionId;
    private final CommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;

    public MerchantListener(@Value("${gcloud.project.id}") String projectId,
                            @Value("${gcloud.pubsub.subscription.id.merchant}") String subscriptionId,
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

        log.info("Start merchant listener...");

        subscriber.startAsync().awaitRunning();

        log.info("Merchant listener running");
    }

    private MessageReceiver receiver() {
        return (message, consumer) -> {
            try {
                var data = message.getData().toStringUtf8();
                log.info("Merchant message: {} => {}", message.getMessageId(), data);

                switch (message.getAttributesMap().get("type")) {
                    case "product_created" -> {
                        var productCreated = objectMapper.readValue(data, ProductCreated.class);
                        commandDispatcher.dispatch(new CreateProductCommand(productCreated.merchantId(), productCreated.productId()));
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
