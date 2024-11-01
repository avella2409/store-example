package com.avella.store.shared;

import java.util.Optional;

public class Env {
    public static final String GCLOUD_PROJECT_ID = env("GCLOUD_PROJECT_ID");
    public static final String ENV_ID = env("ENV_ID");
    public static final String GATEWAY_URL = env("GATEWAY_URL");

    private static String env(String name) {
        return Optional.ofNullable(System.getenv(name))
                .orElseThrow(() -> new RuntimeException("Require env variable: " + name));
    }
}
