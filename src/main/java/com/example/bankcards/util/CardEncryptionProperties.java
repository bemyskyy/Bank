package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardEncryptionProperties {
    @Value("${card.encryption.key}")
    private String key;

    @Value("${card.encryption.algorithm:AES}")
    private String algorithm;

    public String getKey() {
        return key;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}