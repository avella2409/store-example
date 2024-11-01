package com.avella.store.ProductInfo.integration;

import com.avella.shared.application.Command;
import com.avella.store.ProductInfo.application.command.CreateProductCommand;
import com.avella.store.ProductInfo.client.listener.MerchantListener;
import com.avella.store.ProductInfo.integration.shared.Gcloud;
import com.avella.store.ProductInfo.integration.shared.Waiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
public class MerchantListenerTest {

    private SubscriptionAdminClient adminClient;
    private SubscriptionName subscriptionName;
    private List<Command> dispatchedCommands;

    @BeforeEach
    void setup() throws IOException {
        adminClient = SubscriptionAdminClient.create();
        // SubscriptionId should start with a letter
        subscriptionName = SubscriptionName.of(Gcloud.PROJECT_ID, "MerchantListenerTest-" + UUID.randomUUID().toString());

        System.out.println("Creating subscription...");
        adminClient.createSubscription(
                subscriptionName,
                TopicName.of(Gcloud.PROJECT_ID, Gcloud.PUBSUB_TOPIC_ID),
                PushConfig.getDefaultInstance(),
                10
        );
        System.out.println("Created subscription: " + subscriptionName.getSubscription());

        dispatchedCommands = Collections.synchronizedList(new ArrayList<>());
        new MerchantListener(subscriptionName.getProject(), subscriptionName.getSubscription(),
                command -> {
                    System.out.println("Dispatched command: " + command);
                    dispatchedCommands.add(command);
                }, new ObjectMapper())
                .startListening();
    }

    @AfterEach
    void tearDown() {
        System.out.println("Deleting subscription...");
        adminClient.deleteSubscription(subscriptionName);
        System.out.println("Subscription deleted");
        adminClient.close();
    }

    @Test
    void listenEvent() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        var publisher = Publisher.newBuilder(TopicName.of(Gcloud.PROJECT_ID, Gcloud.PUBSUB_TOPIC_ID)).build();

        ApiFuture<String> future = publisher.publish(PubsubMessage.newBuilder()
                .putAttributes("type", "product_created")
                .setData(ByteString.copyFromUtf8("""
                        {
                            "merchantId": "merchant1",
                            "productId": "product1"
                        }
                        """))
                .build());

        future.get(10, TimeUnit.SECONDS);

        Waiter.waitUntil(() -> !dispatchedCommands.isEmpty(), 500, 10000);

        assertEquals(new CreateProductCommand("merchant1", "product1"), dispatchedCommands.getFirst());
    }
}