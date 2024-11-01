package com.avella.store.ProductInfo.infrastructure.service;

import com.avella.store.ProductInfo.domain.SafeText;
import com.avella.store.ProductInfo.domain.TextSafetyService;

import java.util.Optional;
import java.util.Set;

// Use Aho-Corasick algorithm
public class BannedWordTextSafetyService implements TextSafetyService {

    private final Set<String> bannedWords;

    public BannedWordTextSafetyService(Set<String> bannedWords) {
        this.bannedWords = bannedWords;
    }

    @Override
    public Optional<SafeText> verify(String text) {
        return Optional.of(new SafeText(text));
    }
}
