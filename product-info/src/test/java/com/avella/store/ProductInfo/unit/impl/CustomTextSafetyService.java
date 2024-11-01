package com.avella.store.ProductInfo.unit.impl;

import com.avella.store.ProductInfo.domain.SafeText;
import com.avella.store.ProductInfo.domain.TextSafetyService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CustomTextSafetyService implements TextSafetyService {

    private Set<String> failOnWord = new HashSet<>();

    public void failOn(String text) {
        failOnWord.add(text);
    }

    @Override
    public Optional<SafeText> verify(String text) {
        return failOnWord.contains(text) ? Optional.empty() : Optional.of(new SafeText(text));
    }
}
