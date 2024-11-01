package com.avella.store.ProductInfo.client.controller;

import com.avella.shared.application.CommandDispatcher;
import com.avella.shared.application.QueryDispatcher;
import com.avella.store.ProductInfo.application.command.UpdateProductInfoCommand;
import com.avella.store.ProductInfo.application.query.GetProductInfoQuery;
import com.avella.store.ProductInfo.client.controller.request.UpdateProductInfoRequest;
import com.avella.store.ProductInfo.client.controller.response.ProductInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/product")
public class ProductInfoController {

    private static final Logger log = LoggerFactory.getLogger(ProductInfoController.class);

    private final QueryDispatcher queryDispatcher;
    private final CommandDispatcher commandDispatcher;

    public ProductInfoController(QueryDispatcher queryDispatcher, CommandDispatcher commandDispatcher) {
        this.queryDispatcher = queryDispatcher;
        this.commandDispatcher = commandDispatcher;
    }

    @GetMapping("/info/{id}")
    public ProductInfoResponse getProductInfo(@PathVariable String id, Principal principal) {
        log.info("Request product info {}", id);
        var dto = queryDispatcher.dispatch(new GetProductInfoQuery(principal.getName(), id));
        return new ProductInfoResponse(dto.name(), dto.description());
    }

    @PostMapping("/update/{id}")
    public void updateProductInfo(@PathVariable String id, @RequestBody UpdateProductInfoRequest request, Principal principal) {
        log.info("Update product info: {}", request);
        commandDispatcher.dispatch(new UpdateProductInfoCommand(principal.getName(), id, request.name(), request.description()));
    }
}
