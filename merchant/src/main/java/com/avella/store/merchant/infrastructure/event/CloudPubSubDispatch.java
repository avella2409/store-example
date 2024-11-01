package com.avella.store.merchant.infrastructure.event;

import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class CloudPubSubDispatch implements Consumer<EventDb> {

    private static final Logger log = LoggerFactory.getLogger(CloudPubSubDispatch.class);

    private final Publisher publisher;

    public CloudPubSubDispatch(@Value("${gcloud.project.id}") String projectId,
                               @Value("${gcloud.pubsub.topic.id.self}") String topicId) {
        TopicName topicName = TopicName.of(projectId, topicId);

        try {
            this.publisher = Publisher.newBuilder(topicName).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(EventDb eventDb) {
        log.info("Publish to pubsub: {}", eventDb);
        try {
            ApiFuture<String> future = publisher.publish(PubsubMessage.newBuilder()
                    .putAttributes("type", eventDb.getType())
                    .setData(ByteString.copyFromUtf8(eventDb.getContent()))
                    .build());

            future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
