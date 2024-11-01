package com.avella.store.merchant.configuration;

import com.avella.shared.application.*;
import com.avella.shared.application.decorator.LogHandlerDecorator;
import com.avella.shared.application.decorator.RetryNonBusinessErrorDecorator;
import com.avella.shared.application.decorator.TimeoutHandlerDecorator;
import com.avella.shared.configuration.CommandDispatcherBuilder;
import com.avella.shared.configuration.QueryDispatcherBuilder;
import com.avella.store.merchant.application.command.ArchiveProductCommand;
import com.avella.store.merchant.application.command.CreateProductCommand;
import com.avella.store.merchant.application.command.PublishProductCommand;
import com.avella.store.merchant.application.command.RegisterMerchantCommand;
import com.avella.store.merchant.application.command.handler.ArchiveProductHandler;
import com.avella.store.merchant.application.command.handler.CreateProductHandler;
import com.avella.store.merchant.application.command.handler.PublishProductHandler;
import com.avella.store.merchant.application.command.handler.RegisterMerchantHandler;
import com.avella.store.merchant.application.query.GetAllProductQuery;
import com.avella.store.merchant.application.query.dto.ProductStatusDto;
import com.avella.store.merchant.application.service.TimeService;
import com.avella.store.merchant.configuration.decorator.MetricCountCommandHandlerDecorator;
import com.avella.store.merchant.configuration.decorator.TracingCommandHandlerDecorator;
import com.avella.store.merchant.domain.MerchantRepository;
import com.avella.store.merchant.domain.PublishingRulesEngine;
import com.avella.store.merchant.infrastructure.query.GetAllProductQueryHandler;
import com.avella.store.merchant.infrastructure.repository.JpaMerchantRepository;
import com.avella.store.merchant.infrastructure.service.WebhookPublishingRulesEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.avella.shared.configuration.DecoratorUtils.withDecoratorsExecutedInOrder;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public CommandDispatcher commandDispatcher(
            CommandHandler<CreateProductCommand> createProductHandler,
            CommandHandler<PublishProductCommand> publishProductHandler,
            CommandHandler<ArchiveProductCommand> archiveProductHandler,
            CommandHandler<RegisterMerchantCommand> registerMerchantHandler,
            ObservationRegistry obsRegistry,
            MeterRegistry meterRegistry,
            @Value("${spring.application.name}") String appName
    ) {
        return CommandDispatcherBuilder.newDispatcher()
                .register(CreateProductCommand.class, withDecoratorsExecutedInOrder(createProductHandler,
                        decorators("CreateProduct", appName, obsRegistry, meterRegistry)))
                .register(PublishProductCommand.class, withDecoratorsExecutedInOrder(publishProductHandler,
                        decorators("PublishProduct", appName, obsRegistry, meterRegistry)))
                .register(ArchiveProductCommand.class, withDecoratorsExecutedInOrder(archiveProductHandler,
                        decorators("ArchiveProduct", appName, obsRegistry, meterRegistry)))
                .register(RegisterMerchantCommand.class, withDecoratorsExecutedInOrder(registerMerchantHandler,
                        decorators("RegisterMerchant", appName, obsRegistry, meterRegistry)))
                .build();
    }

    @Bean
    public QueryDispatcher queryDispatcher(QueryHandler<GetAllProductQuery, List<ProductStatusDto>> getAllProduct) {
        return QueryDispatcherBuilder.newDispatcher()
                .register(GetAllProductQuery.class, getAllProduct)
                .build();
    }

    private <C extends Command> List<Function<CommandHandler<C>, CommandHandler<C>>> decorators(String name,
                                                                                                String appName,
                                                                                                ObservationRegistry obsRegistry,
                                                                                                MeterRegistry meterRegistry) {
        return List.of(
                handler -> new TracingCommandHandlerDecorator<>(handler, name, obsRegistry, appName),
                handler -> new MetricCountCommandHandlerDecorator<>(handler, name, meterRegistry),
                LogHandlerDecorator::new,
                handler -> new TimeoutHandlerDecorator<>(handler, 3000),
                handler -> new RetryNonBusinessErrorDecorator<>(handler, 2, 200)
        );
    }

    @Bean
    public CommandHandler<RegisterMerchantCommand> registerMerchantHandler(MerchantRepository merchantRepository) {
        return new RegisterMerchantHandler(merchantRepository);
    }

    @Bean
    public CommandHandler<CreateProductCommand> createProductHandler(MerchantRepository merchantRepository,
                                                                     TimeService timeService) {
        return new CreateProductHandler(merchantRepository, timeService);
    }

    @Bean
    public CommandHandler<PublishProductCommand> publishProductHandler(MerchantRepository merchantRepository,
                                                                       TimeService timeService,
                                                                       PublishingRulesEngine publishingRulesEngine) {
        return new PublishProductHandler(merchantRepository, timeService, publishingRulesEngine);
    }

    @Bean
    public CommandHandler<ArchiveProductCommand> archiveProductHandler(MerchantRepository merchantRepository,
                                                                       TimeService timeService) {
        return new ArchiveProductHandler(merchantRepository, timeService);
    }

    @Bean
    public QueryHandler<GetAllProductQuery, List<ProductStatusDto>> getAllProduct(JpaMerchantRepository merchantRepository,
                                                                                  ObjectMapper objectMapper) {
        return new GetAllProductQueryHandler(merchantRepository, objectMapper);
    }

    @Bean
    public PublishingRulesEngine publishingRulesEngine(@Value("${webhook.publishing.urls}") Set<String> urls,
                                                       RestTemplate restTemplate) {
        return new WebhookPublishingRulesEngine(urls, restTemplate, Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });
        return template;
    }

    @Bean
    public Supplier<UUID> uuidGenerator() {
        return UUID::randomUUID;
    }
}
