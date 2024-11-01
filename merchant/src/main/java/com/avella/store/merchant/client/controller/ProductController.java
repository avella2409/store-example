package com.avella.store.merchant.client.controller;

import com.avella.shared.application.CommandDispatcher;
import com.avella.shared.application.QueryDispatcher;
import com.avella.store.merchant.application.command.ArchiveProductCommand;
import com.avella.store.merchant.application.command.CreateProductCommand;
import com.avella.store.merchant.application.command.PublishProductCommand;
import com.avella.store.merchant.application.query.GetAllProductQuery;
import com.avella.store.merchant.application.query.dto.ProductStatusDto;
import com.avella.store.merchant.client.controller.request.ArchiveProductRequest;
import com.avella.store.merchant.client.controller.request.PublishProductRequest;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final CommandDispatcher commandDispatcher;
    private final QueryDispatcher queryDispatcher;
    private final Supplier<UUID> uuidGenerator;

    public ProductController(CommandDispatcher commandDispatcher,
                             QueryDispatcher queryDispatcher,
                             Supplier<UUID> uuidGenerator) {
        this.commandDispatcher = commandDispatcher;
        this.queryDispatcher = queryDispatcher;
        this.uuidGenerator = uuidGenerator;
    }

    @GetMapping("/findAll")
    public List<ProductStatusDto> findAll(Principal principal) {
        return queryDispatcher.dispatch(new GetAllProductQuery(principal.getName()));
    }

    @PostMapping("/create")
    public String create(Principal principal) {
        var productId = uuidGenerator.get().toString();
        commandDispatcher.dispatch(new CreateProductCommand(principal.getName(), productId));
        return productId;
    }

    @PostMapping("/publish")
    public void publish(@RequestBody PublishProductRequest request, Principal principal) {
        commandDispatcher.dispatch(new PublishProductCommand(principal.getName(), request.productId(),
                uuidGenerator.get().toString()));
    }

    @PostMapping("/archive")
    public void archive(@RequestBody ArchiveProductRequest request, Principal principal) {
        commandDispatcher.dispatch(new ArchiveProductCommand(principal.getName(), request.productId()));
    }
}
