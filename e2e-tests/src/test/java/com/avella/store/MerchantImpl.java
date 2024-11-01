package com.avella.store;

import com.avella.store.service.AuthService;
import com.avella.store.service.MerchantService;
import com.avella.store.service.ProductInfoService;

public class MerchantImpl implements Merchant {
    private String userId = "";

    private final AuthService authService;
    private final MerchantService merchantService;
    private final ProductInfoService productInfoService;

    public MerchantImpl(
            AuthService authService,
            MerchantService merchantService,
            ProductInfoService productInfoService) {
        this.authService = authService;
        this.merchantService = merchantService;
        this.productInfoService = productInfoService;
    }


    private String accessToken() {
        return authService.accessToken(userId).orElse("");
    }

    @Override
    public void register() {
        System.out.println("Register merchant");

        userId = authService.register();

        System.out.println("Registered merchant: " + userId);
    }

    @Override
    public void login() {
        System.out.println("Login user");

        authService.login(userId);

        System.out.println("Got access token: " + accessToken());
    }

    @Override
    public String createProduct() {
        System.out.println("Create Product");

        var productId = merchantService.createProduct(accessToken());

        System.out.println("Created product: " + productId);

        return productId;
    }

    @Override
    public void fillProductInfo(String productId, String productName, String productDescription) {
        System.out.println("Fill product info");

        productInfoService.update(accessToken(), productId, productName, productDescription);

        System.out.println("Filled product info");
    }

    @Override
    public void publishProduct(String productId) {
        System.out.println("Publish product");

        merchantService.publishProduct(accessToken(), productId);

        System.out.println("Published product");
    }

    @Override
    public String productStatus(String productId) {
        System.out.println("Get product status");

        var status = merchantService.findAllProductStatus(accessToken()).stream()
                .filter(s -> s.id().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product id not found"));

        System.out.println("Found product status: " + status);

        return status.status();
    }

    @Override
    public void deleteAllResources() {
        System.out.println("Delete all resources");

        authService.deleteUser(userId);

        System.out.println("All resources deleted");
    }
}
