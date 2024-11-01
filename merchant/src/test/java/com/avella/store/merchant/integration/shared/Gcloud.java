package com.avella.store.merchant.integration.shared;

import java.util.Optional;

public class Gcloud {
    public static final String PROJECT_ID = Optional.ofNullable(System.getenv("GCLOUD_PROJECT_ID"))
            .orElseThrow(() -> new RuntimeException("Require env variable: GCLOUD_PROJECT_ID"));
    public static final String PUBSUB_TOPIC_ID = "merchanttest";
}
