package com.avella.store.merchant.infrastructure.repository;

import com.avella.shared.domain.DomainEvent;
import com.avella.shared.domain.Entity;
import com.avella.store.merchant.domain.Event;
import com.avella.store.merchant.domain.Merchant;
import com.avella.store.merchant.domain.MerchantRepository;
import com.avella.store.merchant.domain.Product;
import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import com.avella.store.merchant.infrastructure.repository.model.MerchantDb;
import com.avella.store.merchant.infrastructure.repository.model.ProductJson;
import com.avella.store.merchant.infrastructure.repository.model.ProductsJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class PostgresMerchantRepository implements MerchantRepository {

    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";

    private final JpaMerchantRepository jpaMerchantRepository;
    private final JpaEventRepository jpaEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public PostgresMerchantRepository(JpaMerchantRepository jpaMerchantRepository,
                                      JpaEventRepository jpaEventRepository,
                                      ApplicationEventPublisher eventPublisher,
                                      ObjectMapper objectMapper) {
        this.jpaMerchantRepository = jpaMerchantRepository;
        this.jpaEventRepository = jpaEventRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Merchant> merchant(String id) {
        return merchantSnapshot(id).map(Merchant::restore);
    }

    @Override
    @Transactional
    public void save(Merchant merchant) {
        saveSnapshot(merchant.snapshot());
    }

    public Optional<Merchant.Snapshot> merchantSnapshot(String id) {
        return jpaMerchantRepository.findById(id)
                .map(merchant -> new Merchant.Snapshot(
                        new Entity.Snapshot(
                                merchant.getId(),
                                merchant.getLastUpdateTime(),
                                merchant.getCreationTime(),
                                merchant.getVersion(),
                                null
                        ),
                        fromJson(merchant.getProducts())
                                .products().stream()
                                .map(this::toProduct)
                                .toList()
                ));
    }

    private Product toProduct(ProductJson p) {
        return switch (p.status()) {
            case STATUS_ARCHIVED ->
                    new Product.Archived(p.productId(), localDateTime(p.creationTime()), localDateTime(p.archiveTime()));
            case STATUS_DRAFT -> new Product.Draft(p.productId(), localDateTime(p.creationTime()));
            case STATUS_PUBLISHED ->
                    new Product.Published(p.productId(), localDateTime(p.creationTime()), localDateTime(p.publishedTime()));
            default -> throw new IllegalStateException("Unexpected value: " + p.status());
        };
    }

    public void saveSnapshot(Merchant.Snapshot snapshot) {
        jpaMerchantRepository.save(merchantDb(snapshot));
        snapshot.entitySnapshot().eventsToDispatch()
                .forEach(this::saveEvent);
    }

    private MerchantDb merchantDb(Merchant.Snapshot snapshot) {
        var entitySnapshot = snapshot.entitySnapshot();
        return new MerchantDb(
                entitySnapshot.id(),
                LocalDateTime.now(ZoneOffset.UTC),
                entitySnapshot.creationTime(),
                entitySnapshot.version(),
                json(new ProductsJson(snapshot.products().stream()
                        .map(product -> switch (product) {
                            case Product.Archived p ->
                                    new ProductJson(p.productId(), time(p.creationTime()), STATUS_ARCHIVED, null, time(p.archiveTime()));
                            case Product.Draft p ->
                                    new ProductJson(p.productId(), time(p.creationTime()), STATUS_DRAFT, null, null);
                            case Product.Published p ->
                                    new ProductJson(p.productId(), time(p.creationTime()), STATUS_PUBLISHED, time(p.publishTime()), null);
                        })
                        .toList())
                )
        );
    }

    public void saveEvent(DomainEvent event) {
        var eventDb = new EventDb(
                switch ((Event) event) {
                    case Event.ProductArchived e -> "product_archived";
                    case Event.ProductCreated e -> "product_created";
                    case Event.ProductPublished e -> "product_published";
                    case Event.MerchantRegistered e -> "merchant_registered";
                },
                json(event)
        );
        jpaEventRepository.save(eventDb);
        eventPublisher.publishEvent(eventDb);
    }

    private ProductsJson fromJson(String products) {
        try {
            return objectMapper.readValue(products, ProductsJson.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String json(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private long time(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.UTC);
    }

    private LocalDateTime localDateTime(long time) {
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
    }
}
