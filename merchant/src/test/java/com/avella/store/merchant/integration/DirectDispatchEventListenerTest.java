package com.avella.store.merchant.integration;

import com.avella.store.merchant.configuration.AsyncExecutorConfiguration;
import com.avella.store.merchant.infrastructure.event.DirectDispatchEventListener;
import com.avella.store.merchant.infrastructure.repository.JpaEventRepository;
import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import com.avella.store.merchant.integration.shared.Waiter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED) // running in transaction by default with @DataJpaTest
@Import({AsyncExecutorConfiguration.class, DirectDispatchEventListener.class, EventConsumer.class})
@Tag("integration")
public class DirectDispatchEventListenerTest {

    @Autowired
    private DirectDispatchEventListener directDispatchEventListener;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EventConsumer eventConsumer;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JpaEventRepository jpaEventRepository;

    @Test
    void consumeAndRemoveEvent() {
        var eventDb = new EventDb("type", "content");
        eventDb.setId(1L);

        jpaEventRepository.save(eventDb);

        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(eventDb); // need to execute in transaction to trigger DirectDispatchEventConsumer
            return null;
        });

        Waiter.waitUntil(() -> !eventConsumer.consumed().isEmpty(), 500, 10000);
        assertTrue(eventDb == eventConsumer.consumed().getFirst());

        Waiter.waitUntil(() -> jpaEventRepository.findById(1L).isEmpty(), 500, 5000);
    }
}

@Service
class EventConsumer implements Consumer<EventDb> {

    private final List<EventDb> consumed = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void accept(EventDb eventDb) {
        consumed.add(eventDb);
    }

    public List<EventDb> consumed() {
        return consumed;
    }
}