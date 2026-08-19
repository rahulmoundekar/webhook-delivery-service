package com.rahul.webhook.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class WebhookSignatureService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public String generateSignature(String payload, String secret) {

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);

            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);

            mac.init(keySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            return toHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate webhook signature", e);
        }
    }

    private String toHex(byte[] bytes) {

        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }
}