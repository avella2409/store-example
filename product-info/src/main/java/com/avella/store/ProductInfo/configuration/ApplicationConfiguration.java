package com.avella.store.ProductInfo.configuration;

import com.avella.shared.application.*;
import com.avella.shared.application.decorator.LogHandlerDecorator;
import com.avella.shared.application.decorator.RetryNonBusinessErrorDecorator;
import com.avella.shared.application.decorator.TimeoutHandlerDecorator;
import com.avella.shared.configuration.CommandDispatcherBuilder;
import com.avella.shared.configuration.QueryDispatcherBuilder;
import com.avella.store.ProductInfo.application.command.CreateProductCommand;
import com.avella.store.ProductInfo.application.command.UpdateProductInfoCommand;
import com.avella.store.ProductInfo.application.command.handler.CreateProductHandler;
import com.avella.store.ProductInfo.application.command.handler.UpdateProductInfoHandler;
import com.avella.store.ProductInfo.application.query.CanPublishQuery;
import com.avella.store.ProductInfo.application.query.GetProductInfoQuery;
import com.avella.store.ProductInfo.application.query.dto.ProductInfoDto;
import com.avella.store.ProductInfo.configuration.decorator.MetricCountCommandHandlerDecorator;
import com.avella.store.ProductInfo.configuration.decorator.TracingCommandHandlerDecorator;
import com.avella.store.ProductInfo.domain.ProductRepository;
import com.avella.store.ProductInfo.domain.TextSafetyService;
import com.avella.store.ProductInfo.infrastructure.query.CanPublishQueryHandler;
import com.avella.store.ProductInfo.infrastructure.query.GetProductInfoQueryHandler;
import com.avella.store.ProductInfo.infrastructure.service.BannedWordTextSafetyService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.avella.shared.configuration.DecoratorUtils.withDecoratorsExecutedInOrder;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public QueryDispatcher queryDispatcher(QueryHandler<GetProductInfoQuery, ProductInfoDto> getProductInfo,
                                           QueryHandler<CanPublishQuery, Boolean> canPublish) {
        return QueryDispatcherBuilder.newDispatcher()
                .register(GetProductInfoQuery.class, getProductInfo)
                .register(CanPublishQuery.class, canPublish)
                .build();
    }

    @Bean
    public CommandDispatcher commandDispatcher(CommandHandler<UpdateProductInfoCommand> updateProductInfo,
                                               CommandHandler<CreateProductCommand> createProduct,
                                               ObservationRegistry obsRegistry,
                                               MeterRegistry meterRegistry,
                                               @Value("${spring.application.name}") String appName) {
        return CommandDispatcherBuilder.newDispatcher()
                .register(UpdateProductInfoCommand.class, withDecoratorsExecutedInOrder(updateProductInfo,
                        decorators("UpdateProductInfo", appName, obsRegistry, meterRegistry)))
                .register(CreateProductCommand.class, withDecoratorsExecutedInOrder(createProduct,
                        decorators("CreateProduct", appName, obsRegistry, meterRegistry)))
                .build();
    }

    @Bean
    public CommandHandler<CreateProductCommand> createProduct(ProductRepository productRepository) {
        return new CreateProductHandler(productRepository);
    }

    @Bean
    public QueryHandler<GetProductInfoQuery, ProductInfoDto> getProductInfo(MongoOperations ops) {
        return new GetProductInfoQueryHandler(ops);
    }

    @Bean
    public CommandHandler<UpdateProductInfoCommand> updateProductInfo(ProductRepository productRepository,
                                                                      TextSafetyService textSafetyService) {
        return new UpdateProductInfoHandler(productRepository, textSafetyService);
    }

    @Bean
    public QueryHandler<CanPublishQuery, Boolean> canPublish(MongoOperations ops) {
        return new CanPublishQueryHandler(ops);
    }

    @Bean
    public TextSafetyService textSafetyService() {
        Set<String> bannedWords = Set.of();
        return new BannedWordTextSafetyService(bannedWords);
    }

    private static <C extends Command> List<Function<CommandHandler<C>, CommandHandler<C>>> decorators(String name,
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
}
