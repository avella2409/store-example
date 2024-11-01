package com.avella.store.ProductInfo.client.controller;

import com.avella.shared.application.QueryDispatcher;
import com.avella.store.ProductInfo.application.query.CanPublishQuery;
import com.avella.store.ProductInfo.client.controller.request.PublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final QueryDispatcher queryDispatcher;

    public WebhookController(QueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
    }

    @PostMapping("/canPublish")
    public ResponseEntity<Void> canPublish(@RequestBody PublishRequest request) {
        log.info("CanPublish ? {}", request);
        if (queryDispatcher.dispatch(new CanPublishQuery(request.merchantId(), request.productId())))
            return ResponseEntity.ok().build();
        else return ResponseEntity.unprocessableEntity().build();
    }
}
