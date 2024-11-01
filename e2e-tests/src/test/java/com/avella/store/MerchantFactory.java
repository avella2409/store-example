package com.avella.store;

import com.avella.store.service.AuthService;
import com.avella.store.service.MerchantService;
import com.avella.store.service.ProductInfoService;
import com.avella.store.service.impl.KeycloakAuthService;
import com.avella.store.service.impl.MerchantServiceImpl;
import com.avella.store.service.impl.ProductInfoServiceImpl;
import com.avella.store.shared.Env;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MerchantFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final AuthService authService =
            new KeycloakAuthService(Env.GCLOUD_PROJECT_ID, Env.GATEWAY_URL + "/auth", "store-client", "avella", objectMapper);

    private static final MerchantService merchantService =
            new MerchantServiceImpl(Env.GATEWAY_URL + "/" + Env.ENV_ID + "/merchant", objectMapper);

    private static final ProductInfoService productInfoService =
            new ProductInfoServiceImpl(Env.GATEWAY_URL + "/" + Env.ENV_ID + "/productinfo", objectMapper);

    public static Merchant create() {
        return new MerchantImpl(authService, merchantService, productInfoService);
    }
}
