package com.avella.store.service;

import java.util.Optional;

public interface AuthService {

    String register();

    void login(String userId);

    Optional<String> accessToken(String userId);

    void deleteUser(String userId);
}
