package com.avella.store.merchant.integration;

import com.avella.store.merchant.infrastructure.event.CloudPubSubDispatch;
import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import com.avella.store.merchant.integration.shared.Gcloud;
import com.avella.store.merchant.integration.shared.Waiter;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
public class CloudPubSubDispatchTest {

    private final String topicId = "merchanttest";

    private SubscriptionAdminClient adminClient;
    private SubscriptionName subscriptionName;
    private List<PubsubMessage> messages;

    @BeforeEach
    void setup() throws IOException {
        adminClient = SubscriptionAdminClient.create();
        // SubscriptionId should start with a letter
        subscriptionName = SubscriptionName.of(Gcloud.PROJECT_ID, "CloudPubSubDispatchTest-" + UUID.randomUUID().toString());

        System.out.println("Creating subscription...");
        adminClient.createSubscription(
                subscriptionName,
                TopicName.of(Gcloud.PROJECT_ID, topicId),
                PushConfig.getDefaultInstance(),
                10
        );
        System.out.println("Created subscription: " + subscriptionName.getSubscription());

        messages = Collections.synchronizedList(new ArrayList<>());
        Subscriber subscriber = Subscriber.newBuilder(ProjectSubscriptionName.of(Gcloud.PROJECT_ID, subscriptionName.getSubscription()),
                        (MessageReceiver) (message, consumer) -> {
                            System.out.println("Received message: " + message.getData().toStringUtf8());
                            messages.add(message);
                            consumer.ack();
                        })
                .build();
        System.out.println("Start Async subscriber...");
        subscriber.startAsync().awaitRunning();
        System.out.println("Subscriber running");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Deleting subscription...");
        adminClient.deleteSubscription(subscriptionName);
        System.out.println("Subscription deleted");
        adminClient.close();
    }

    @Test
    void dispatchEvent() {
        var pubsub = new CloudPubSubDispatch(Gcloud.PROJECT_ID, topicId);

        pubsub.accept(new EventDb("cloud_pubsub_dispatch_test", "Some data"));

        Waiter.waitUntil(() -> !messages.isEmpty(), 500, 10000);

        assertEquals("Some data", messages.getFirst().getData().toStringUtf8());
        assertEquals("cloud_pubsub_dispatch_test", messages.getFirst().getAttributesMap().get("type"));
    }


}
