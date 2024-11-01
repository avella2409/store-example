package com.avella.store.ProductInfo.domain;

import java.util.Optional;

public interface TextSafetyService {
    Optional<SafeText> verify(String text);
}
