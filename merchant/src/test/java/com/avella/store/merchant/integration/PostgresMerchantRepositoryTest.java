package com.avella.store.merchant.integration;

import com.avella.shared.domain.Entity;
import com.avella.store.merchant.core.command.domain.Event;
import com.avella.store.merchant.core.command.domain.Merchant;
import com.avella.store.merchant.core.command.domain.Product;
import com.avella.store.merchant.infrastructure.repository.JpaEventRepository;
import com.avella.store.merchant.infrastructure.repository.JpaMerchantRepository;
import com.avella.store.merchant.infrastructure.repository.PostgresMerchantRepository;
import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import com.avella.store.merchant.integration.shared.PostgreContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {"spring.jpa.show-sql=true", "spring.jpa.properties.hibernate.format_sql=true"})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // running in transaction by default with @DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RecordApplicationEvents
@Tag("integration")
public class PostgresMerchantRepositoryTest extends PostgreContainer {

    // Align with Postgres TIMESTAMP precision (no nanoseconds)
    private final LocalDateTime now =
            LocalDateTime.of(2020, 5, 5, 0, 0, 0, 0);

    @Autowired
    private JpaMerchantRepository jpaMerchantRepository;

    @Autowired
    private JpaEventRepository jpaEventRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApplicationEvents applicationEvents;

    private PostgresMerchantRepository merchantRepository;

    @BeforeEach
    void setup() {
        jpaMerchantRepository.deleteAll();
        jpaEventRepository.deleteAll();

        var objectMapper = new ObjectMapper();
        merchantRepository = new PostgresMerchantRepository(
                jpaMerchantRepository,
                jpaEventRepository,
                eventPublisher,
                objectMapper
        );
    }

    @Test
    void saveAndRetrieve() {
        List<Product> products = List.of(
                new Product.Draft("p1", now),
                new Product.Archived("p2", now, now.plusDays(1)),
                new Product.Published("p3", now, now.plusDays(2))
        );
        merchantRepository.save(Merchant.restore(new Merchant.Snapshot(
                new Entity.Snapshot("merchant1", now, now, 0, List.of()),
                products
        )));

        assertEquals(products, merchantRepository.merchant("merchant1").get().snapshot().products());
    }

    @Test
    void dispatchAllDomainEvent() {
        merchantRepository.saveSnapshot(new Merchant.Snapshot(
                new Entity.Snapshot("merchant1", now, now, 0, List.of(
                        new Event.ProductCreated("merchant1", "p1"),
                        new Event.ProductArchived("merchant1", "p2"),
                        new Event.ProductPublished("merchant1", "p3", "publishingId1"),
                        new Event.MerchantRegistered("merchant1")
                )),
                List.of()
        ));

        var events = jpaEventRepository.findAll();
        assertEquals(4, events.size());
        hasEvent("product_archived", """
                {"merchantId":"merchant1","productId":"p2"}""", events);
        hasEvent("product_created", """
                {"merchantId":"merchant1","productId":"p1"}""", events);
        hasEvent("product_published", """
                {"merchantId":"merchant1","productId":"p3","publishingId":"publishingId1"}""", events);
        hasEvent("merchant_registered", """
                {"merchantId":"merchant1"}""", events);

        var springEvents = applicationEvents.stream(EventDb.class).toList();
        assertEquals(4, springEvents.size());
        assertEquals(
                events.stream().map(EventDb::getId).collect(Collectors.toSet()),
                springEvents.stream().map(EventDb::getId).collect(Collectors.toSet())
        );
    }

    private void hasEvent(String type, String jsonContent, List<EventDb> events) {
        var event = events.stream().filter(ev -> ev.getType().equals(type)).findFirst().get();
        assertEquals(jsonContent, event.getContent());
    }
}