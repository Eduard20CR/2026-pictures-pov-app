package com.picturepov.api.event;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Base64;

@Component
class PublicCodeGenerator {

    private static final int CODE_BYTES = 16;

    private final SecureRandom random = new SecureRandom();

    String generate() {
        byte[] bytes = new byte[CODE_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}